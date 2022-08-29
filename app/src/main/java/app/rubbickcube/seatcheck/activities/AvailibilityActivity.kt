package app.rubbickcube.seatcheck.activities

import android.app.Activity
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.ObservableObject
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.adapters.NearbyPlacesAdapter
import app.rubbickcube.seatcheck.model.Availability
import app.rubbickcube.seatcheck.model.Post
import app.rubbickcube.seatcheck.model.Records
import com.aigestudio.wheelpicker.WheelPicker
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.geo.GeoPoint
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_availibility.*
import kotlinx.android.synthetic.main.activity_post_seat.*
import kotlinx.android.synthetic.main.activity_post_seat.btn_post_seat
import kotlinx.android.synthetic.main.activity_post_seat.interesting
import kotlinx.android.synthetic.main.activity_post_seat.location_desc
import kotlinx.android.synthetic.main.activity_post_seat.location_phone
import kotlinx.android.synthetic.main.activity_post_seat.restaurant_location
import kotlinx.android.synthetic.main.activity_post_seat.seat_time
import kotlinx.android.synthetic.main.activity_post_seat.txt_select_interesting
import kotlinx.android.synthetic.main.activity_post_seat.txt_select_seat
import kotlinx.android.synthetic.main.activity_post_seat.txt_select_time
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.bottom_interesting_sheet.*
import kotlinx.android.synthetic.main.bottom_seat_sheet.*
import kotlinx.android.synthetic.main.bottom_time_sheet.*
import kotlinx.android.synthetic.main.bottom_time_sheet.bottom_sheet_time
import kotlinx.android.synthetic.main.bottom_time_sheet.seat_count_time_cancel
import kotlinx.android.synthetic.main.bottom_time_sheet.seat_count_time_done
import kotlinx.android.synthetic.main.bottom_time_sheet.wheel_min
import kotlinx.android.synthetic.main.bottom_time_sheet_availibility.*
import kotlinx.android.synthetic.main.post_seat_confirm_dialog.view.*
import java.text.SimpleDateFormat
import java.util.*

class AvailibilityActivity : AppCompatActivity(), Observer {

    private val TAG = "AvailibilityActivity"
    var nearbyPlacesList: List<PlaceLikelihood>? = null
    var placesAdapter: NearbyPlacesAdapter? = null
    private lateinit var placesClient: PlacesClient
    var backendlessUser: BackendlessUser = BackendlessUser()
    val options = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
    private var sheetBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var sheetTimeBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var sheetInterestingBehavior: BottomSheetBehavior<LinearLayout>? = null

    val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.PHONE_NUMBER, Place.Field.ADDRESS, Place.Field.LAT_LNG)


    private val AUTOCOMPLETE_REQUEST_CODE = 1111

    val PLACE_PICKER_REQUEST = 1
    private var seat: Int? = 0
    private var isRestaurantSelected: Boolean = false
    private var selectedHour: Int? = 0
    private var selectedMins: String? = ""
    private var postOnMap: String? = "yes"
    private var showOnMap: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_availibility)

        switch1.setOnClickListener {
            if (switch1.isChecked) {
                showOnMap = switch1.isChecked
            } else {
                showOnMap = switch1.isChecked
            }
        }

        ObservableObject.getInstance().addObserver(this)
        supportActionBar?.hide()
        //AppClass.appComponent?.inject(this)
        Hawk.init(this).build()


        if (Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        } else {
            return
        }


        Places.initialize(applicationContext, resources.getString(R.string.google_maps_key))
        placesClient = Places.createClient(applicationContext)

        setupinitialization()


    }

    private fun setupinitialization() {
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        sheetTimeBehavior = BottomSheetBehavior.from(bottom_sheet_time);
        sheetInterestingBehavior = BottomSheetBehavior.from(interesting_sheet);

        Backendless.initApp(this@AvailibilityActivity, resources.getString(R.string.appId), resources.getString(R.string.secretKey), "v1")
        sheetBehavior!!.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    sheetBehavior!!.setPeekHeight(0)
                }
            }

            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {}
        })

        restaurant_location.visibility = View.INVISIBLE
        txt_select_interesting.setOnClickListener {
            if (sheetTimeBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetTimeBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (sheetBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            sheetInterestingBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            sheetInterestingBehavior?.isDraggable = false
        }
        txt_select_seat.setOnClickListener {
            if (sheetTimeBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetTimeBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (sheetInterestingBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetInterestingBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            sheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            sheetBehavior?.isDraggable = false
        }
        interesting_cancel.setOnClickListener {
            if (sheetInterestingBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetInterestingBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            interesting_cancel.text = ""
        }
        interesting_done.setOnClickListener {
            if (sheetInterestingBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetInterestingBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            interesting.text = getList()!!.get(interesting_view.selectedItem)
        }
        txt_select_time.setOnClickListener {
            if (sheetBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (sheetInterestingBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetInterestingBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            sheetTimeBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            sheetTimeBehavior?.isDraggable = false
        }
        seat_count_time_cancel_av.setOnClickListener {
            if (sheetTimeBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetTimeBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            seat_time_av.text = ""

        }
        wheel_min_av.setCanLoop(false);
        seat_count_time_done_av.setOnClickListener {
            if (sheetTimeBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetTimeBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            seat_time_av.text = getListtime()!!.get(wheel_min_av.selectedItem)

        }



        loop_view.setInitPosition(0);
        loop_view.setCanLoop(false);
        loop_view.setItems(getList(starting = 1, limit = 6))

        interesting_view.setInitPosition(3);
        interesting_view.setCanLoop(false);
        interesting_view.setItems(getList())

       // wheel_min_av.setInitPosition(3);
       // wheel_min_av.setCanLoop(false);
        wheel_min_av.setItems(getListtime())

        btn_post_seat.setOnClickListener {
            if (seat_time_av.equals("") && seat_time_av!!.equals("Select")) {
                Utils.showAlertDialog(this, "Sorry!", "Please select time.")
                return@setOnClickListener
            } else if (interesting.equals("") && interesting!!.equals("Select")) {
                Utils.showAlertDialog(this, "Sorry!", "Please select time.")
                return@setOnClickListener
            } else {
                // showSeatPostDialog()
                if (Prefs.getBoolean("isLiveavailibility", false)) {
                    Utils.showAlertDialog(this, "Sorry!", "You can't post new seat while you are live or in a meeting");
                } else if (Prefs.getBoolean("inMeeting", false)) {
                    Utils.showAlertDialog(this, "Sorry!", "You can't post new seat while you are live or in a meeting");
                } else {
                    postSeat()
                }
            }
        }
        findViewById<ImageView>(R.id.appbar_back).setOnClickListener {
            finish()
        }

        if (!Prefs.getString("profileImage", "").isNullOrEmpty()) {
            Glide.with(this).load(backendlessUser.properties["profileImage"]).apply(options).into(appbar_img)
        }
    }


    fun getList(limit: Int, starting: Int): List<String>? {
        val list: ArrayList<String> = ArrayList()
        for (i in starting..limit) {
            list.add("$i")
        }
        return list
    }

    fun getListtime(): List<String>? {
        val list: ArrayList<String> = ArrayList()
        list.add("Few hours")
        list.add("Few days")
        list.add("Few weeks")
        return list
    }

    fun getList(): List<String>? {
        val list: ArrayList<String> = ArrayList()
        list.add("Get to know me!")
        list.add("Netflix & Chill")
        list.add("Hook me up for drink")
        list.add("Feed me some yum")
        list.add("nom nom on me")
        list.add("it's my treat")
        list.add("Drinks on me ")
        list.add("Down for fun")
        return list
    }


    private fun postSeat() {

        val availibility = Availability()
        availibility.user = backendlessUser
        availibility.interest = getList()!!.get(interesting_view.selectedItem)
        availibility.time = getListtime()!!.get(wheel_min_av.selectedItem)

        Log.d(TAG, "postSeat4: " + Prefs.getString("currentLat", ""))
        Log.d(TAG, "postSeat3: " + Prefs.getString("currentLng", ""))

        availibility.isIsAvailable = showOnMap
        availibility.latitude = Prefs.getString("currentLat", "")
        availibility.longitude = Prefs.getString("currentLng", "")
//        val geoPoint = GeoPoint(lat!!, lng!!)
//        backendlessUser.properties["location"] = geoPoint

        addAvailibility(availibility)
    }


    override fun onDestroy() {
        super.onDestroy()
        ObservableObject.getInstance().deleteObserver(this)
    }


    private fun showSeatPostDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater

        val dialog_layout = inflater.inflate(R.layout.post_seat_confirm_dialog, null)
        builder.setView(dialog_layout)
        val dialog = builder.create()
        dialog.getWindow()!!.setBackgroundDrawableResource(R.drawable.rounded_corner)

        dialog.show()

        dialog_layout.btn_yes.setOnClickListener {
            postSeat()
            dialog.dismiss()
        }
        dialog_layout.btn_no.setOnClickListener {


            if (Prefs.getBoolean("isLive", false)) {
                Utils.showAlertDialog(this@AvailibilityActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
            } else if (Prefs.getBoolean("inMeeting", false)) {
                Utils.showAlertDialog(this@AvailibilityActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
            } else {
                showOnMap = false
                postSeat()
                // grantCurrentUser(backendlessUser.objectId)
                dialog.dismiss()
            }

        }
        dialog_layout.btn_cancel.setOnClickListener {
            dialog.cancel()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE && data != null) {
                if (resultCode == Activity.RESULT_OK) {


                    val place = Autocomplete.getPlaceFromIntent(data)

                    if (AppClass.lat != null) {
                        if (inRange(AppClass.lat!!, AppClass.lng!!, place.latLng?.latitude!!, place.latLng?.longitude!!, 1609)) {

                            isRestaurantSelected = true

                            restaurant_location.visibility = View.VISIBLE
                            location_name.text = place.name
                            location_desc.text = place.address
                            location_phone.text = place.phoneNumber
                            Prefs.putString("restaurantLat", place.latLng?.latitude.toString())
                            Prefs.putString("restaurantLng", place.latLng?.longitude.toString())
                            Prefs.putString("name", place.name.toString())
                            Prefs.putString("vicinity", if (place.address != null) place.address.toString() else "")
                            Prefs.putString("phone", if (place.phoneNumber != null) place.phoneNumber.toString() else "")

                        } else {
                            Utils.showAlertDialog(this, "Sorry!", "Choose place under 1 mile")
                        }
                    } else {
                        Toast.makeText(this, "Your device location is null, please open your gps and restart app", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }

    }


    fun inRange(latCenter: Double, lngCenter: Double, latDest: Double, lngDest: Double, radius: Int): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(latCenter, lngCenter, latDest, lngDest, results)
        val distanceInMeters = results[0]
        val isWithinRange = distanceInMeters < radius

        return isWithinRange
    }


    private fun addSeatRecords(records: Records, post: Post) {


//        val pd = Utils.SCProgressDialog(this@PostSeatActivity, null, "Please wait...")
//        pd.show()
        gif_view.visibility = View.VISIBLE
        gif_view.play();

        Backendless.Persistence.of(Records::class.java).save(records, object : AsyncCallback<Records> {
            override fun handleFault(fault: BackendlessFault?) {
//                Utils.dismissProgressDialog(pd)
                gif_view.visibility = View.GONE
                Toast.makeText(this@AvailibilityActivity, fault?.message, Toast.LENGTH_LONG).show()
            }

            override fun handleResponse(response: Records?) {
//                Utils.dismissProgressDialog(pd)
                addPostRecord(post)
            }

        })


    }

    private fun addPostRecord(post: Post) {
        val pd = Utils.SCProgressDialog(this, null, "Posting Availibility...")
        pd.show()

        Backendless.Persistence.of(Post::class.java).save(post, object : AsyncCallback<Post> {
            override fun handleFault(fault: BackendlessFault?) {
                Utils.dismissProgressDialog(pd)
                Toast.makeText(this@AvailibilityActivity, fault?.message, Toast.LENGTH_LONG).show()
                //        gif_view.visibility=View.GONE

            }

            override fun handleResponse(response: Post?) {
                //           gif_view.visibility=View.GONE
                Utils.dismissProgressDialog(pd)
                // Toast.makeText(this@PostSeatActivity,"Seat Posted Successfully",Toast.LENGTH_LONG).show()

                Prefs.remove("name")
                Prefs.putBoolean("isLive", true)
                Prefs.putString("seatId", response?.objectId)
                Prefs.putInt("userStatus", 1)
                HawkUtils.putHawk("postedSeatLive", post)
                Prefs.putDouble("postedSeatLat", response?.resturantLocationLatitude?.toDouble()!!)
                Prefs.putDouble("postedSeatLng", response?.resturantLocationLongitude?.toDouble()!!)
                val intent = Intent(this@AvailibilityActivity, NeedASeatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                val times = response?.time

//                val millisss = times?.minus(300)?.times(1000)?.toLong()
//                //val futureInMillis = SystemClock.elapsedRealtime() + millisss!!
//                Utils.setAlarm(1000,this@PostSeatActivity,response.objectId)

//
                if (times > 300) {
                    val millisss = times?.minus(300)?.times(1000)?.toLong()
                    val futureInMillis = SystemClock.elapsedRealtime() + millisss!!
                    Utils.setAlarm(futureInMillis, this@AvailibilityActivity, response.objectId)

                }
            }

        })
    }


    override fun update(p0: Observable?, place: Any?) {


        if (place is Place) {
            if (AppClass.lat != null) {
                if (inRange(AppClass.lat!!, AppClass.lng!!, place.latLng?.latitude!!, place.latLng?.longitude!!, 1609)) {

                    isRestaurantSelected = true

                    restaurant_location.visibility = View.VISIBLE
                    location_name.text = place.name
                    location_desc.text = place.address
                    location_phone.text = place.phoneNumber
                    Prefs.putString("restaurantLat", place.latLng?.latitude.toString())
                    Prefs.putString("restaurantLng", place.latLng?.longitude.toString())
                    Prefs.putString("name", place.name.toString())
                    Prefs.putString("vicinity", if (place.address != null) place.address.toString() else "")
                    Prefs.putString("phone", if (place.phoneNumber != null) place.phoneNumber.toString() else "")

                } else {
                    Utils.showAlertDialog(this@AvailibilityActivity, "Sorry!", "Choose place under 1 mile")
                }
            } else {
                Toast.makeText(this@AvailibilityActivity, "Your device location is null, please open your gps and restart app", Toast.LENGTH_SHORT).show()
            }


        }


    }

    private fun addAvailibility(availibility: Availability) {
        val pd = Utils.SCProgressDialog(this, null, "Posting Availibility...")
        pd.show()
        Backendless.Persistence.of(Availability::class.java).save(availibility, object : AsyncCallback<Availability> {
            override fun handleFault(fault: BackendlessFault?) {
                Utils.dismissProgressDialog(pd)
                Toast.makeText(this@AvailibilityActivity, fault?.message, Toast.LENGTH_LONG).show()
            }

            override fun handleResponse(response: Availability?) {
                Utils.dismissProgressDialog(pd)
                Toast.makeText(this@AvailibilityActivity, "Successfully Updated", Toast.LENGTH_LONG).show()
            }
        })
    }

}