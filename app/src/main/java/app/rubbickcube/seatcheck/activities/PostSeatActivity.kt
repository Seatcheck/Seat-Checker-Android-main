package app.rubbickcube.seatcheck.activities

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.ObservableObject
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.adapters.NearbyPlacesAdapter
import app.rubbickcube.seatcheck.model.Post
import app.rubbickcube.seatcheck.model.Records
import com.aigestudio.wheelpicker.WheelPicker
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
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
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_post_seat.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.bottom_interesting_sheet.*
import kotlinx.android.synthetic.main.bottom_seat_sheet.*
import kotlinx.android.synthetic.main.bottom_time_sheet.*
import kotlinx.android.synthetic.main.post_seat_confirm_dialog.view.*
import java.text.SimpleDateFormat
import java.util.*


class PostSeatActivity : AppCompatActivity(), Observer {


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
    private var selectedMins: Int? = 0
    private var postOnMap: String? = "yes"
    private var showOnMap: Boolean? = true

   // var   sw: Switch? = findViewById(R.id.mapswitch)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_seat)

       mapswitch.setOnCheckedChangeListener({ _ , isChecked ->
           showOnMap = isChecked
//           val message = if (isChecked) "Switch1:ON" else "Switch1:OFF"

       })

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
        setupHourSpinner()
        setupMinsSpinner()
        getCurrentPlaces()
    }

    private fun setupinitialization() {
        sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        sheetTimeBehavior = BottomSheetBehavior.from(bottom_sheet_time);
        sheetInterestingBehavior = BottomSheetBehavior.from(interesting_sheet);

        Backendless.initApp(this@PostSeatActivity, resources.getString(R.string.appId), resources.getString(R.string.secretKey), "v1")
        sheetBehavior!!.addBottomSheetCallback(object : BottomSheetCallback() {
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
        }
        txt_select_seat.setOnClickListener {
            if (sheetTimeBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetTimeBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (sheetInterestingBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetInterestingBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            sheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
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
        }
        seat_count_time_cancel.setOnClickListener {
            if (sheetTimeBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetTimeBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            seat_time.text = ""
        }
        seat_count_time_done.setOnClickListener {
            if (sheetTimeBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetTimeBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            if (selectedHour == 0 && selectedMins == 0) {
                seat_time.text = ""
            } else {
                var time = "" +  selectedMins + " Minutes"
                seat_time.text = time
            }
        }
        seat_count_cancel.setOnClickListener {
            if (sheetBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            seat = 0
            seat_count.text = ""
        }
        seat_count_done.setOnClickListener {
            if (sheetBehavior!!.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            var value = getList(starting = 1, limit = 6)?.get(loop_view.selectedItem)
            seat = value!!.toInt()
            if (value.equals("1")) {
                value = value + " Seat"
            } else {
                value = value + " Seats"
            }
            seat_count.text = value
        }

        loop_view.setInitPosition(0);
        loop_view.setCanLoop(false);
        loop_view.setItems(getList(starting = 1, limit = 6))

        interesting_view.setInitPosition(3);
        interesting_view.setCanLoop(false);
        interesting_view.setItems(getList())

        txt_select_rest.setOnClickListener {


            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, resources.getString(R.string.google_maps_key))
            }

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this@PostSeatActivity)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)


//
//            val builder = PlacePicker.IntentBuilder()
//
//            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST)
//            //Utils.startActivityWithAnimation(this, SearhRestaurentActivity::class.java,findViewById(R.id.txt_select_rest),false,R.color.app_orange)
        }

        btn_post_seat.setOnClickListener {


            if (seat == 0) {
                Toast.makeText(this@PostSeatActivity, "Please select a seat", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if (!isRestaurantSelected) {
                Toast.makeText(this@PostSeatActivity, "Please select restaurant", Toast.LENGTH_LONG).show()
                return@setOnClickListener

            } else if (selectedHour == 1 && selectedMins!! > 1) {

                Utils.showAlertDialog(this@PostSeatActivity, "Sorry!", "The maximum time allowed is one hour.")

            } else if (selectedHour == 0 && selectedMins!! == 0) {

                Utils.showAlertDialog(this@PostSeatActivity, "Sorry!", "Please select seat time.")

            } else {
               // showSeatPostDialog()
                if (Prefs.getBoolean("isLive", false)) {
                    Utils.showAlertDialog(this@PostSeatActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
                } else if (Prefs.getBoolean("inMeeting", false)) {
                    Utils.showAlertDialog(this@PostSeatActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
                } else {
                    postSeat()
                 }

            }
        }
        appbar_back.setOnClickListener {

            finish()
        }

        if (!Prefs.getString("profileImage", "").isNullOrEmpty()) {
            Glide.with(this@PostSeatActivity).load(backendlessUser.properties["profileImage"]).apply(options).into(appbar_img)
        }
    }


    private fun setupHourSpinner() {

        wheel_hour.data = getList(limit = 1, starting = 0)

        wheel_hour.setOnItemSelectedListener(object : WheelPicker.OnItemSelectedListener {
            override fun onItemSelected(picker: WheelPicker?, data: Any?, position: Int) {
                selectedHour = getList(limit = 1, starting = 0)!![position].toInt()
            }

        })
    }


    fun getList(limit: Int, starting: Int): List<String>? {
        val list: ArrayList<String> = ArrayList()
        for (i in starting..limit) {
            list.add("$i")
        }
        return list
    }

    fun getListformin(limit: Int, starting: Int): List<String>? {
        val list: ArrayList<String> = ArrayList()
        for (i in starting..limit) {
            list.add("$i min")
        }
        return list
    }

    fun getList(): List<String>? {
        val list: ArrayList<String> = ArrayList()
        list.add("Drink on me")
        list.add("Food on me")
        list.add("Appetizer on me")
        list.add("No offer")
        return list
    }

    private fun setupMinsSpinner() {


        wheel_min.data = getListformin(starting = 1, limit = 60)
        wheel_min.setOnItemSelectedListener(object : WheelPicker.OnItemSelectedListener {
            override fun onItemSelected(picker: WheelPicker?, data: Any?, position: Int) {
                selectedMins = position + 1// (getList(starting = 1, limit = 60)!![position] + "Min").toInt()

            }

        })
    }


    private fun postSeat() {

        //addTime(selectedHour!!,selectedMins)

        //val post = HashMap<String,Any>()


        val record = Records()
        record.user = backendlessUser
        record.seats = seat!!.toInt()
        record.interestOption = getList()!!.get(interesting_view.selectedItem)
        record.resturantName = Prefs.getString("name", "")
        record.resturantAddress = Prefs.getString("vicinity", "")
        record.resturantPhone = Prefs.getString("phone", "")
        record.totalRequests = 0
        record.meetingStatus = "not reserved"

        val post = Post()
        post.user = backendlessUser
        post.quantity = seat!!.toInt()
        if (selectedHour == 1) {
            selectedMins = 59
            post.time = selectedMins?.times(60)!!

        } else {
            post.time = selectedMins?.times(60)!!

        }
        post.extendedTime = Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()
        post.createdTwo = Utils.getCreatedTwoTimeTempForSolvingiOSIssue(selectedMins!!)
        post.resturantName = Prefs.getString("name", "")
        post.resturantAddress = Prefs.getString("vicinity", "")
        post.resturantPhone = Prefs.getString("phone", "")
        post.resturantLocationLatitude = Prefs.getString("restaurantLat", "")
        post.resturantLocationLongitude = Prefs.getString("restaurantLng", "")
        post.shouldGoLive = postOnMap
        post.showOnMap = showOnMap!!
        post.created = Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()
        post.record = record
        post.interestOption = getList()!!.get(interesting_view.selectedItem);


        addSeatRecords(record, post)


        /*Old method for posting seat*/

//        post["User"] = Backendless.UserService.CurrentUser()
//        post["Quantity"] = seat!!
//        post["time"] = selectedMins?.times(60)!!
//        post["extendedTime"] =Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()
//        Prefs.putString("extendedTime",Utils.fetchCurrentTime())
//        post["createdTwo"] = Utils.getCreatedTwoTimeTempForSolvingiOSIssue(selectedMins!!)
//        Prefs.putString("createdTwo",Utils.getCreatedTwoTime(selectedMins!!))
//        post["resturantName"] = Prefs.getString("name","")
//        post["resturantAddress"] = Prefs.getString("vicinity","")
//        post["resturantPhone"] = Prefs.getString("phone","")
//        post["resturantLocationLatitude"] = Prefs.getString("restaurantLat","")
//        post["resturantLocationLongitude"] = Prefs.getString("restaurantLng","")
//        post["shouldGoLive"] = postOnMap!!
//        post["showOnMap"] = showOnMap!!
//
//
//        val pd = Utils.SCProgressDialog(this@PostSeatActivity,null,"Posting seat(s)...")
//        pd.show()
//        Backendless.Persistence.of("Post").save(post, object : AsyncCallback<Map<*, *>> {
//            override fun handleResponse(response: Map<*, *>) {
//
//
//                pd.dismiss()
//               // Toast.makeText(this@PostSeatActivity,"Seat Posted Successfully",Toast.LENGTH_LONG).show()
//
//                Prefs.remove("name")
//                Prefs.putBoolean("isLive",true)
//                Prefs.putString("seatId",response["objectId"].toString())
//                Prefs.putInt("userStatus",1)
//                val intent = Intent(this@PostSeatActivity, NeedASeatActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//                finish()
//                 val times = response["time"] as Int
//
//                if(times > 300) {
//                    val millisss = times.minus(300).times(1000).toLong()
//                    val futureInMillis = SystemClock.elapsedRealtime() + millisss
//                    Utils.setAlarm(futureInMillis,this@PostSeatActivity,response["objectId"].toString())
//                }
//
//            }
//
//            override fun handleFault(fault: BackendlessFault) {
//                pd.dismiss()
//                Toast.makeText(this@PostSeatActivity,fault.message,Toast.LENGTH_LONG).show()
//
//            }
//        })
    }


    private fun addTime(hour: Int?, mins: Int? = 0): String {

        val calendar = Calendar.getInstance()
        val dt = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
        System.out.println("Original = " + calendar.time)

        // Substract 2 hour from the current time
        //  calendar.add(Calendar.HOUR, hour!!)

        // Add 30 minutes to the calendar time
        calendar.add(Calendar.MINUTE, 50)

        // Add 300 seconds to the calendar time
        System.out.println("Updated  = " + calendar.time)

        //val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().time)


        return dt.format(calendar.time)
    }

    override fun onDestroy() {
        super.onDestroy()
        ObservableObject.getInstance().deleteObserver(this)
    }


    private fun showSeatPostDialog() {
        val builder = AlertDialog.Builder(this@PostSeatActivity)
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
                Utils.showAlertDialog(this@PostSeatActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
            } else if (Prefs.getBoolean("inMeeting", false)) {
                Utils.showAlertDialog(this@PostSeatActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
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
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE && data != null) {
                if (resultCode == Activity.RESULT_OK) {


                    val place = Autocomplete.getPlaceFromIntent(data)


//                isRestaurantSelected = true
//                restaurant_location.visibility = View.VISIBLE
//                location_name.text = place.name
//                location_desc.text = place.address
//                location_phone.text = place.phoneNumber
//                Prefs.putString("restaurantLat", place.latLng.latitude.toString())
//                Prefs.putString("restaurantLng", place.latLng.longitude.toString())
//                Prefs.putString("name", place.name.toString())
//                Prefs.putString("vicinity", place.address.toString())
//                Prefs.putString("phone", place.phoneNumber.toString())
//


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
                            Utils.showAlertDialog(this@PostSeatActivity, "Sorry!", "Choose place under 1 mile")
                        }
                    } else {
                        Toast.makeText(this@PostSeatActivity, "Your device location is null, please open your gps and restart app", Toast.LENGTH_SHORT).show()
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


    fun cvtToGmt(date: Date): Date {
        var tz = TimeZone.getDefault()
        var ret = Date(date.getTime() - tz.getRawOffset())
        if (tz.inDaylightTime(ret)) {
            val dstDate = Date(ret.getTime() - tz.getDSTSavings())

            // check to make sure we have not crossed back into standard time
            // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
            if (tz.inDaylightTime(dstDate)) {
                ret = dstDate;
            }
        }

        return ret

    }


    private fun addSeatRecords(records: Records, post: Post) {


//        val pd = Utils.SCProgressDialog(this@PostSeatActivity, null, "Please wait...")
//        pd.show()
        gif_view.visibility=View.VISIBLE
        gif_view.play();

        Backendless.Persistence.of(Records::class.java).save(records, object : AsyncCallback<Records> {
            override fun handleFault(fault: BackendlessFault?) {
//                Utils.dismissProgressDialog(pd)
                gif_view.visibility=View.GONE
                Toast.makeText(this@PostSeatActivity, fault?.message, Toast.LENGTH_LONG).show()
            }

            override fun handleResponse(response: Records?) {
//                Utils.dismissProgressDialog(pd)
                addPostRecord(post)
            }

        })
    }

    private fun addPostRecord(post: Post) {
//        val pd = Utils.SCProgressDialog(this@PostSeatActivity, null, "Posting Seat...")
//        pd.show()

        Backendless.Persistence.of(Post::class.java).save(post, object : AsyncCallback<Post> {
            override fun handleFault(fault: BackendlessFault?) {
//                Utils.dismissProgressDialog(pd)
                Toast.makeText(this@PostSeatActivity, fault?.message, Toast.LENGTH_LONG).show()
                gif_view.visibility=View.GONE

            }

            override fun handleResponse(response: Post?) {
                gif_view.visibility=View.GONE
//                Utils.dismissProgressDialog(pd)
                // Toast.makeText(this@PostSeatActivity,"Seat Posted Successfully",Toast.LENGTH_LONG).show()
                Prefs.remove("name")
                Prefs.putBoolean("isLive", true)
                Prefs.putString("seatId", response?.objectId)
                Prefs.putInt("userStatus", 1)
                HawkUtils.putHawk("postedSeatLive", post)
                Prefs.putDouble("postedSeatLat", response?.resturantLocationLatitude?.toDouble()!!)
                Prefs.putDouble("postedSeatLng", response?.resturantLocationLongitude?.toDouble()!!)
                val intent = Intent(this@PostSeatActivity, NeedASeatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                val times = response?.time

//                val millisss = times?.minus(300)?.times(1000)?.toLong()
//                //val futureInMillis = SystemClock.elapsedRealtime() + millisss!!
//                Utils.setAlarm(1000,this@PostSeatActivity,response.objectId)

                if (times > 300) {
                    val millisss = times?.minus(300)?.times(1000)?.toLong()
                    val futureInMillis = SystemClock.elapsedRealtime() + millisss!!
                    Utils.setAlarm(futureInMillis, this@PostSeatActivity, response.objectId)

                }
            }

        })
    }

//    private fun grantCurrentUser(userId: String) {
//
//
//        Backendless.UserService.findById(userId, object : AsyncCallback<BackendlessUser> {
//            override fun handleFault(fault: BackendlessFault?) {
//
//
//            }
//
//            override fun handleResponse(response: BackendlessUser?) {
//
//                HawkUtils.putHawk("BackendlessUser",response)
//                postSeat(response!!)
//
//
//            }
//
//        })
//
//    }


    private fun getCurrentPlaces() {

        nearbyPlacesList = arrayListOf()

        val placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG)


// Use the builder to create a FindCurrentPlaceRequest.
        val request =
                FindCurrentPlaceRequest.newInstance(placeFields)
        val placeResponse = placesClient.findCurrentPlace(request)
        placeResponse.addOnCompleteListener {
            it
            if (it.isSuccessful) {

                nearbyPlacesList = it.result?.placeLikelihoods
                placesAdapter = NearbyPlacesAdapter(this@PostSeatActivity, nearbyPlacesList!!)
                nearby_paces_rv.layoutManager = LinearLayoutManager(this@PostSeatActivity, LinearLayoutManager.HORIZONTAL, false)
                nearby_paces_rv.adapter = placesAdapter


            } else {
                val exception = it.exception
                if (exception is ApiException) {
                    val apiException = exception as ApiException
                    Log.e("place", "Place not found: " + apiException.statusCode)

                }
            }
        }


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
                    Utils.showAlertDialog(this@PostSeatActivity, "Sorry!", "Choose place under 1 mile")
                }
            } else {
                Toast.makeText(this@PostSeatActivity, "Your device location is null, please open your gps and restart app", Toast.LENGTH_SHORT).show()
            }


        }


    }


}