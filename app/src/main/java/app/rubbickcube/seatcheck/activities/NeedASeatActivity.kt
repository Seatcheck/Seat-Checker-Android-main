package app.rubbickcube.seatcheck.activities

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import app.rubbickcube.seatcheck.adapters.NeedASeatPagerAdapter
import com.backendless.Backendless
import com.backendless.exceptions.BackendlessFault
import com.backendless.BackendlessCollection
import com.backendless.async.callback.AsyncCallback
import com.google.android.material.tabs.TabLayout
import android.graphics.PorterDuff
import android.location.*
import android.location.Location
import android.os.AsyncTask
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import app.invision.morse.api.ApiUtils
import app.rubbickcube.seatcheck.*
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.adapters.InvitesAdapter
import app.rubbickcube.seatcheck.api.EndPointsConstants
import app.rubbickcube.seatcheck.model.*
import app.rubbickcube.seatcheck.services.AlarmBroadcastReceiver
import app.rubbickcube.seatcheck.services.LocationService
import com.backendless.BackendlessUser
import com.backendless.geo.GeoPoint
import com.backendless.persistence.BackendlessDataQuery
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes

import com.google.android.libraries.places.api.Places;
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.iid.FirebaseInstanceId
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_show_invites.*
import kotlinx.android.synthetic.main.app_bar_layout_for_need_seat.*
import kotlinx.android.synthetic.main.layout_for_main_activity.*
import kotlinx.android.synthetic.main.nav_panel_layout.*
import kotlinx.android.synthetic.main.need_seat_layou.*
import kotlinx.android.synthetic.main.no_pins_layot_on_map.*

import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class NeedASeatActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, Observer {

    var backendlessUser: BackendlessUser = BackendlessUser()

    val options = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)


    override fun update(p0: Observable?, p1: Any?) {


        if (p1!!.equals("listIsEmpty")) {
            sliding_tabs_main.getTabAt(1)?.select()
        } else if (p1!!.equals("requested to reserve your seat.")) {

            runOnUiThread {
                if (AppClass.inviteCounter > 0) {
                    invite_counter.visibility = View.VISIBLE
                    invite_counter.text = AppClass.inviteCounter.toString()
                } else {
                    invite_counter.visibility = View.GONE
                }
            }


        } else if (p1.equals("Refresh")) {
            fetchPostViaModelForRadiusSearch(seekbarInt!!, this@NeedASeatActivity)
        }
    }


    public   val TAG = "NeedASeatActivity"
    var hasSeatLiveNo = false
    private var postList: MutableList<Post>? = null
    private var currentPosttList: MutableList<Post>? = null
    private var tempPostList: MutableList<Post>? = null
    private var changeRadiusPostList: MutableList<Post>? = null
    private var currentMarkerList: MutableList<Post>? = null
    private var seekbarInt: Int? = 1
    private val mSeekbarchange = Handler()
    private var REQUEST_CHECK_SETTINGS = 1000
    private var googleApiClient: GoogleApiClient? = null
    private var mLastLocation: Location? = null
    private var request: LocationRequest? = null
    private var pd: ProgressDialog? = null
    var lat: Double? = null
    var lng: Double? = null
    var sequence: TapTargetSequence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_need_aseat)
        Hawk.init(this).build()
        //AppClass.appComponent?.inject(this)
        if (Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        } else {
            return
        }
        getAvailibility()
        ObservableObject.getInstance().addObserver(this)
        Backendless.initApp(this@NeedASeatActivity, resources.getString(R.string.appId), resources.getString(R.string.secretKey), "v1")
        progressBar.setCircleBackgroundEnabled(true)
        progressBar.setColorSchemeResources(R.color.colorPrimary)
        progressBar.visibility = View.INVISIBLE

        this.lat = AppClass.lat
        this.lng = AppClass.lng
        setupLocationManager()
        viewpager_main.adapter = NeedASeatPagerAdapter(this, supportFragmentManager)
        sliding_tabs_main.setupWithViewPager(viewpager_main)
        viewpager_main.currentItem = 1

//        if(viewpager_main.currentItem .equals(1)){
//            appbar_img_map.setImageResource(R.drawable.location_orange);
//            appbar_img_list.setImageResource(R.drawable.list_grey);
//        }else{
//            appbar_img_map.setImageResource(R.drawable.location_grey);
//            appbar_img_list.setImageResource(R.drawable.list_orange);
//        }
        setupTabIcons()

        viewpager_main.isClickable = false


//            if(Utils.isConnectedOnline(this)) {
//                fetchPostViaModelForRadiusSearch(1,this@NeedASeatActivity)
//            }else {
//                Utils.showAlertDialog(this,"Opps","Internet is not connected.")
//
//            }


        Glide.with(this).load(R.drawable.profile).into(img_ll_profile)
        Glide.with(this).load(R.drawable.settings).into(img_ll_settings)
        Glide.with(this).load(R.drawable.explore_people).into(img_ll_nd_chat)
        Glide.with(this).load(R.drawable.logout).into(img_ll_logut)
        Glide.with(this).load(R.drawable.invite).into(img_ll_invite)
        Glide.with(this).load(R.drawable.contacts_icon).into(img_ll_nd_contacts)
        ll_profile.setOnClickListener {
            AppClass.reviewForOwner = true
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@NeedASeatActivity, ProfileActivity::class.java))
        }

        ll_setting.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@NeedASeatActivity, SettingsActivity::class.java))
        }
        ll_help.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@NeedASeatActivity, ActivityHelp::class.java))
        }
        ll_privacypolicy.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@NeedASeatActivity, PrivacyPolicyActivity::class.java))
        }
        ll_chats.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@NeedASeatActivity, ChatListActivity::class.java))
        }
        ll_contacts.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@NeedASeatActivity, ActivityContacts::class.java))
        }
        ll_logout.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            //  Backendless.UserService.logout();
            showRemoveSeatDialog()

        }

        ll_invite.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=app.rubbickcube.seatcheck")
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share To:"))
        }

        btn_refres_icon_need_seat.setOnClickListener {

            fetchPostViaModelForRadiusSearch(seekbarInt!!, this@NeedASeatActivity)

        }

        img_magnifier.setOnClickListener {
            startActivity(Intent(this@NeedASeatActivity, PeopleAroundMeActivitiy::class.java))

        }



        btn_post_seat_main.setOnClickListener {


//            if(Prefs.getBoolean("isLive",false)) {
//
//                Utils.showAlertDialog(this@NeedASeatActivity,"Sorry!","You can't post new seat while you are live or in a meeting");
//            }else if(Prefs.getBoolean("inMeeting",false) ) {
//                Utils.showAlertDialog(this@NeedASeatActivity,"Sorry!","You can't post new seat while you are live or in a meeting");
//            }
//            else {
//                startActivity(Intent(this@NeedASeatActivity,PostSeatActivity::class.java))
//
//            }

//            if(Prefs.getBoolean("isLive",false)) {
//                Utils.showAlertDialog(this@NeedASeatActivity,"Sorry!","You can't post new seat while you are live or in a meeting");
//            }else {
//
//                startActivity(Intent(this@NeedASeatActivity,PostSeatActivity::class.java))
//
//            }


            // on below line we are creating a new bottom sheet dialog.
            val dialog = BottomSheetDialog(this)

            // on below line we are inflating a layout file which we have created.
            val view = layoutInflater.inflate(R.layout.bottomsheetdialogavaliability, null)


            val btnpostseat = view.findViewById<TextView>(R.id.postnewseat)
            val btnavailibility = view.findViewById<TextView>(R.id.setavaliable)
            val btncancel = view.findViewById<TextView>(R.id.cancelbtn)

            btncancel.setOnClickListener {
                dialog.dismiss()
            }
            btnpostseat.setOnClickListener {
                dialog.dismiss()
                if (Prefs.getBoolean("isLive", false)) {

                    Utils.showAlertDialog(this@NeedASeatActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
                } else if (Prefs.getBoolean("inMeeting", false)) {
                    Utils.showAlertDialog(this@NeedASeatActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
                } else {
                    startActivity(Intent(this@NeedASeatActivity, PostSeatActivity::class.java))

                }
            }
            btnavailibility.setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(this@NeedASeatActivity, AvailibilityActivity::class.java))
            }
            dialog.setContentView(view)

            // on below line we are calling
            // a show method to display a dialog.
            dialog.show()

        }

        btn_post_seat_from_view.setOnClickListener {


            if (Prefs.getBoolean("isLive", false)) {

                Utils.showAlertDialog(this@NeedASeatActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
            } else if (Prefs.getBoolean("inMeeting", false)) {
                Utils.showAlertDialog(this@NeedASeatActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
            } else {
                startActivity(Intent(this@NeedASeatActivity, PostSeatActivity::class.java))

            }

//            if(Prefs.getBoolean("isLive",false)) {
//                Utils.showAlertDialog(this@NeedASeatActivity,"Sorry!","You can't post new seat while you are live or in a meeting");
//            }else {
//
//                startActivity(Intent(this@NeedASeatActivity,PostSeatActivity::class.java))
//
//            }
        }
    }

    private fun getAvailibility() {
        val whereClause = "isAvailable = true"
        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        Backendless.Persistence.of(Availability::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Availability>> {
                    override fun handleResponse(foundAvailibility: BackendlessCollection<Availability>) {
                        var Availabilitylist: MutableList<Availability>? = arrayListOf()
                        for (i in foundAvailibility.data.indices) {
                            if (foundAvailibility.data[i].isIsAvailable != null) {
                                val availibility = Availability()
                                availibility.objectId = foundAvailibility.data[i].objectId.toString()
                                availibility.isIsAvailable = foundAvailibility.data[i].isIsAvailable
                                availibility.interest = foundAvailibility.data[i].interest.toString()
                                availibility.time = foundAvailibility.data[i].time.toString()
                                availibility.longitude = foundAvailibility.data[i].longitude.toString()
                                availibility.latitude = foundAvailibility.data[i].latitude.toString()
                                Availabilitylist?.add(availibility)
                            }
                        }
                    }

                    override fun handleFault(fault: BackendlessFault) {

                    }
                })


    }
    private fun setupSeekLister() {

        seekbar_list_main.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {


                if (p1 != 0) {
                    range_text_main?.text = "" + p1
                    seekbarInt = p1
                    ObservableObject.getInstance().updateValue(p1)
                    mSeekbarchange.removeCallbacks(onSeebarChangeTimeout)
                    mSeekbarchange.postDelayed(onSeebarChangeTimeout, 600)

                } else {
                    range_text_main?.text = "" + p1

                }

            }

        })
//        seekbar_list_main?.setOnSeekbarChangeListener({ value ->
//            range_text_main?.text = ""+value.toInt()
//            // setRadiusm(Prefs.getDouble("lat",0.0),Prefs.getDouble("lng",0.0),value.toInt())
//
//
//            seekbarInt = value.toInt()
//            this.lat = lat
//            this.lng = lng
//            ObservableObject.getInstance().updateValue(seekbarInt)
//
//            mSeekbarchange.removeCallbacks(onSeebarChangeTimeout);
//            mSeekbarchange.postDelayed(onSeebarChangeTimeout, 600)
//
//        })
    }


    private fun setupTabIcons() {
        sliding_tabs_main.getTabAt(0)?.setIcon(R.drawable.list_grey)
        sliding_tabs_main.getTabAt(1)?.setIcon(R.drawable.location_grey)


        sliding_tabs_main.getTabAt(0)?.getIcon()?.setColorFilter(Color.parseColor("#D7533F"), PorterDuff.Mode.SRC_IN)
        sliding_tabs_main.getTabAt(1)?.getIcon()?.setColorFilter(Color.parseColor("#a8a8a8"), PorterDuff.Mode.SRC_IN)

        sliding_tabs_main.getTabAt(0)?.getIcon()?.setColorFilter(Color.parseColor("#D7533F"), PorterDuff.Mode.SRC_IN)
        sliding_tabs_main.getTabAt(1)?.getIcon()?.setColorFilter(Color.parseColor("#a8a8a8"), PorterDuff.Mode.SRC_IN)


        sliding_tabs_main.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.icon!!.setColorFilter(Color.parseColor("#D7533F"), PorterDuff.Mode.SRC_IN)

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.icon!!.setColorFilter(Color.parseColor("#a8a8a8"), PorterDuff.Mode.SRC_IN)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }


    private fun setupLocationManager() {
        //buildGoogleApiClient();
        if (googleApiClient == null) {

            googleApiClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    //.addApi(Places.GEO_DATA_API)
                    //.addApi(Places.PLACE_DETECTION_API)
                    .build()
            //mGoogleApiClient = new GoogleApiClient.Builder(this);
        }
        googleApiClient?.connect()

    }

    private fun createLocationRequest() {

        request = LocationRequest()
        request?.smallestDisplacement = 100f
        request?.fastestInterval = 500000
        request?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        request?.numUpdates = 3

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(request!!)
        builder.setAlwaysShow(true)

        val result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        builder.build())


        result.setResultCallback { result ->
            val status = result.status
            when (status.statusCode) {

                LocationSettingsStatusCodes.SUCCESS -> setInitialLocation()

                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        status.startResolutionForResult(
                                this@NeedASeatActivity,
                                REQUEST_CHECK_SETTINGS)
                    } catch (e: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }// Location settings are not satisfied. However, we have no way
            // to fix the settings so we won't show the dialog.
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> {
                    setInitialLocation()
                    Toast.makeText(this@NeedASeatActivity, "Location enabled by user!", Toast.LENGTH_LONG).show()
                    // mRequestingLocationUpdates = true
                }
                Activity.RESULT_CANCELED -> {
                    // The user was asked to change settings, but chose not to
                    finish()
                    Toast.makeText(this@NeedASeatActivity, "You can not use Seatcheck with our your current location", Toast.LENGTH_LONG).show()
                    //mRequestingLocationUpdates = false
                }

                else -> {
                }
            }
        }
    }


    private fun setInitialLocation() {

        if (ActivityCompat.checkSelfPermission(this@NeedASeatActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@NeedASeatActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request) { location ->


            mLastLocation = location
            this.lat = mLastLocation?.latitude
            this.lng = mLastLocation?.longitude

           Prefs.putString("currentLat",mLastLocation?.latitude.toString())
            Prefs.putString("currentLng",mLastLocation?.longitude.toString())

            AppClass.lat = mLastLocation?.latitude
            AppClass.lng = mLastLocation?.longitude



            refreshToken()
            setSeatCheckUser()



            setupSeekLister()
            fetchPostViaModelForRadiusSearch(1, this@NeedASeatActivity)

            if (googleApiClient != null)
                googleApiClient?.disconnect()

        }
    }


    override fun onConnected(p0: Bundle?) {

        createLocationRequest()

    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this@NeedASeatActivity, "Google api client is not connected", Toast.LENGTH_LONG)

    }

    override fun onLocationChanged(p0: Location?) {

    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }

    private fun getStringAddress(_lat: Double, _lng: Double): String? {

        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>

        try {
            addresses = geocoder.getFromLocation(_lat, _lng, 1)
            val address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            val city = addresses[0].locality
            val state = addresses[0].adminArea
            val country = addresses[0].countryCode
            val postalCode = addresses[0].postalCode
            val knownName = addresses[0].featureName
            Log.d("address", address)
            if (city != null) {
                Log.d("city", city)
            }

//             if(city  == null)
//             {
//                return country;
//             }

            //  return address + "," + city;
            return "$city@$country@$address"

            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (e: IOException) {
            e.printStackTrace()

        }



        return null
    }


//    override fun onStart() {
//        if(googleApiClient != null) {
//            googleApiClient?.connect()
//
//        }
//        super.onStart()
//    }
//
//    override fun onStop() {
//        if(googleApiClient != null) {
//            googleApiClient?.disconnect()
//        }
//        super.onStop()
//    }

//    fun getLatLang(placeId: String) {
//        Places.GeoDataApi.getPlaceById(googleApiClient, placeId)
//                .setResultCallback { places ->
//                    if (places.status.isSuccess && places.count > 0) {
//                        val place = places.get(0)
//                        val latLng = place.latLng
//                        try {
//
//                            val latlngObject = JSONObject()
//                            latlngObject.put("lat",latLng.latitude)
//                            latlngObject.put("lng",latLng.longitude)
//                            latlngObject.put("address","")
//                            ObservableObject.getInstance().updateValue(latlngObject)
//
//                        } catch (ex: Exception) {
//
//                            ex.printStackTrace()
//                            Log.e("MapException", ex.message)
//
//                        }
//
//                        Log.i("place", "Place found: " + place.name)
//                    } else {
//                        Log.e("place", "Place not found")
//                    }
//                    places.release()
//
//                }
//    }


//    override fun onBackPressed() {
//        super.onBackPressed()
//
//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//        finish() // call this to finish the current activity
//   val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(intent)
//        finish() // call this to finish the current activity
//    }


    //This is rececently  code with should go live no
//    private fun fetchPostViaModelForRadiusSearch(seekbar: Int,context : Context) {
//
//        Prefs.putBoolean("isLive",false)
//        Prefs.putBoolean("inMeeting",false)
//        Prefs.putInt("userStatus",0)
//
//
//        progressBar.visibility = View.VISIBLE
////        val pd = Utils.SCProgressDialog(context!!,null,"Finding seats nearby you...")
////        pd.show()
//        Backendless.Persistence.of<Post>(Post::class.java).find(object : AsyncCallback<BackendlessCollection<Post>> {
//            override fun handleResponse(foundPost: BackendlessCollection<Post>) {
//
//
//                tempPostList = arrayListOf()
//                changeRadiusPostList = foundPost.data
//                currentMarkerList = arrayListOf()
//                AppClass.allPost = foundPost.data
//                val miles = 1609
//
//                for(i in foundPost.data!!.indices) {
//
//
//                    if (inRange(lat!!,lng!!,foundPost.data[i].resturantLocationLatitude.toDouble(),foundPost.data[i].resturantLocationLongitude.toDouble(),miles.times(seekbar))) {
//                        foundPost.data!![i].inRange = true
//                        tempPostList!!.add(foundPost.data[i])
//                        if(tempPostList!![i].invite != null) {
//                            if(tempPostList!![i].shouldGoLive.equals("no") && tempPostList!![i].invite.status.equals("ended") ) {
//                                removeUserSeat(tempPostList!![i].objectId)
//                                removeInvitation(tempPostList!![i].invite.objectId)
//                            }else if(tempPostList!![i].shouldGoLive.equals("no") && tempPostList!![i].invite.status.equals("declined")) {
//                                removeUserSeat(tempPostList!![i].objectId)
//                                removeInvitation(tempPostList!![i].invite.objectId)
//
//                            }
//                        }
//                    }
//                    //This is just recently uncommented... need to comment again if thing goes wrong
////                  else {
////                        if(Prefs.getString("userId","").equals(foundPost.data!![i].user.userId)) {
////
////                            removeUserSeat(foundPost.data!![i].objectId)
////                            removeInvitation(Prefs.getString("inviteId",""))
////                            Prefs.remove("isLive")
////                            Prefs.remove("inMeeting")
////                            Prefs.remove("seatId")
////                            Prefs.remove("userStatus")
////                           // HawkUtils.deleteHawk()
////                        }
////                    }
//                }
//
//
//                //itterating all the seats which are lie in radius
//                for(n in tempPostList!!.indices) {
//
//                    //Comparing times..
//                    if(Utils.compareTime(Utils.converTimetoGMT(tempPostList!![n].createdTwo), Utils.getCurrentDateTimeofDevice())) {
//
//                        if(tempPostList!![n].shouldGoLive.equals("no") /*|| tempPostList!![n].shouldGoLive.equals("no")*/) {
//
//                            if(tempPostList!![n].inRange) {
//                                currentMarkerList?.add(tempPostList!![n])
//
//                                if(Prefs.getString("userId","").equals(tempPostList!![n].user.userId)) {
//                                    //Im now in my post
//                                    Prefs.putString("seatId",tempPostList!![n].objectId)
//                                    Prefs.putBoolean("isLive",true)
//                                    Prefs.putInt("userStatus",1)
//                                    if(tempPostList!![n].invite != null) {
//                                        if(tempPostList!![n].invite.status.equals("accepted")) {
//                                            Prefs.putBoolean("inMeeting",true)
//                                            Prefs.putInt("userStatus",2)
//                                            Prefs.putBoolean("isLive",true)
//
//                                        }else if(tempPostList!![n].invite.status.equals("denied")) {
//                                            Prefs.putBoolean("inMeeting",false)
//                                            Prefs.putInt("userStatus",1)
//                                            Prefs.putBoolean("isLive",true)
//
//                                        }else if((tempPostList!![n].invite.status.equals("cancelled"))){
//                                            Prefs.putBoolean("inMeeting",false)
//                                            Prefs.putInt("userStatus",1)
//                                            Prefs.putBoolean("isLive",true)
//
//                                        }else if((tempPostList!![n].invite.status.equals("ended"))){
//                                            Prefs.putBoolean("inMeeting",false)
//                                            Prefs.putInt("userStatus",1)
//                                            Prefs.putBoolean("isLive",false)
//
//                                        }
//                                    }
//// else {
////                                        Prefs.putString("postedUserId",Prefs.getString("userId",""))
////                                        Prefs.putBoolean("isLive",true)
////                                        Prefs.putInt("userStatus",1)
////                                    }
////                                    Prefs.putBoolean("isLive",true)
////                                    Prefs.putInt("userStatus",1)
//
//                               }
//                                else {
//
//                                    if(tempPostList!![n].invite != null) {
//                                        HawkUtils.putHawk("invites",tempPostList!![n].invite)
//
//                                        if(tempPostList!![n].invite.status.equals("accepted")) {
//                                            Prefs.putBoolean("inMeeting",true)
//                                            Prefs.putInt("userStatus",2)
//
//                                        }else if(tempPostList!![n].invite.status.equals("denied")) {
//                                            Prefs.putBoolean("inMeeting",false)
//
//                                        }else if((tempPostList!![n].invite.status.equals("cancelled"))){
//                                            Prefs.putBoolean("inMeeting",false)
//
//                                        }else if((tempPostList!![n].invite.status.equals("ended"))){
//                                            Prefs.putBoolean("inMeeting",false)
//                                            Prefs.putInt("userStatus",1)
//
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    else {
//                        if(Prefs.getString("seatId","").equals(tempPostList!![n].objectId)) {
//
//                            Prefs.remove("seatId")
//                            removeUserSeat(tempPostList!![n].objectId)
//                                if(tempPostList!![n].invite != null) {
//                                    removeInvitation(tempPostList!![n].invite.objectId)
//                                }
//
//
//                        }
//                    }
//                }
//
//
//                if(tempPostList!!.isEmpty()) {
//
//                    Prefs.remove("isLive")
//                    Prefs.remove("inMeeting")
//                    Prefs.remove("userStatus")
//                    Prefs.remove("inviteId")
//                    Prefs.remove("inviteStatus")
//                    Prefs.remove("seatId")
//                }
//
//
//
//                if(currentMarkerList!!.isEmpty()) {
//
//                    ObservableObject.getInstance().updateValue("listIsEmpty")
//
//                   // HawkUtils.deleteHawk()
//                    no_pin_layout.visibility = View.VISIBLE
//                }else {
//
//
////                    if(!hasSeatLiveNo) {
////                        no_pin_layout.visibility = View.GONE
////
////                        AppClass.postList = currentMarkerList
////                        HawkUtils.putHawk("postList",currentMarkerList)
////                        ObservableObject.getInstance().updateValue("showList")
////
////                    }
////                    else if(hasSeatLiveNo && !currentMarkerList?.isNotEmpty()!!) {
////                        no_pin_layout.visibility = View.GONE
////
////                        AppClass.postList = currentMarkerList
////                        HawkUtils.putHawk("postList",currentMarkerList)
////                        ObservableObject.getInstance().updateValue("showList")
////
////                    }else if(hasSeatLiveNo && currentMarkerList?.isEmpty()!!){
////
//////                        Prefs.remove("isLive")
//////                        Prefs.remove("inMeeting")
//////                        Prefs.remove("userStatus")
//////                        Prefs.remove("inviteId")
//////                        Prefs.remove("inviteStatus")
//////                        Prefs.remove("seatId")
////                        no_pin_layout.visibility = View.VISIBLE
//////                        AppClass.postList = currentMarkerList
//////                        HawkUtils.putHawk("postList",currentMarkerList)
//////                        ObservableObject.getInstance().updateValue("showList")
////
////
////                    }
//
//                    no_pin_layout.visibility = View.GONE
//                    AppClass.postList = currentMarkerList
//                    HawkUtils.putHawk("postList",currentMarkerList)
//                    ObservableObject.getInstance().updateValue("showList")
//
//                }
////                if(pd.isShowing) {
////                    pd.dismiss()
////                }
//                progressBar.visibility = View.INVISIBLE
//
//                setupAppBar()
//
//            }
//
//            override fun handleFault(fault: BackendlessFault) {
//
//                //pd.dismiss()
//
//                progressBar.visibility = View.INVISIBLE
//
//            }
//        })
//
//
//    }


    private fun fetchPostViaModelForRadiusSearch(seekbar: Int, context: Context) {


        val pd = Utils.SCProgressDialog(context!!, null, "fetching nearby seats...")
        pd.show()
        //progressBar.visibility = View.VISIBLE
        Backendless.Persistence.of<Post>(Post::class.java).find(object : AsyncCallback<BackendlessCollection<Post>> {
            override fun handleResponse(foundPost: BackendlessCollection<Post>) {

                tempPostList = arrayListOf()
                changeRadiusPostList = foundPost.data
                currentMarkerList = arrayListOf()
                AppClass.allPost = foundPost.data
                val miles = 1609
                Prefs.putBoolean("isLive", false)
                Prefs.putBoolean("inMeeting", false)
                Prefs.putInt("userStatus", 0)
                sliding_tabs_main.visibility = View.GONE


                HawkUtils.deleteHawk("postedSeatLive")
                stopService(Intent(this@NeedASeatActivity, LocationService::class.java))

                currentMarkerList!!.clear()

                for (i in foundPost.data!!.indices) {
                    if (foundPost.data[i].resturantLocationLatitude != null && foundPost.data[i].resturantLocationLongitude != null) {
                        if (inRange(lat!!, lng!!, foundPost.data[i].resturantLocationLatitude.toDouble(), foundPost.data[i].resturantLocationLongitude.toDouble(), miles.times(seekbar))) {
                            if (Utils.compareTwoDates(foundPost.data!![i].createdTwo, Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse())) {
                                foundPost.data!![i].inRange = true
                                if (foundPost.data[i].objectId.equals(foundPost.data[i].objectId) && foundPost.data[i].shouldGoLive.equals("no")) {
                                    if (foundPost.data[i].invite != null) {
                                        if (foundPost.data[i].invite.status.equals("accepted")) {
                                            tempPostList!!.add(foundPost.data[i])
                                        }
                                    } else {
                                        tempPostList!!.add(foundPost.data[i])
                                    }
                                } else {
                                    tempPostList!!.add(foundPost.data[i])

                                }
                            }  else {
                                removeUserSeat(foundPost.data[i])
                            }
                        } else {
                            removeUserSeat(foundPost.data[i])
                        }
                    }

                }

                //Second Loop starts here
                //itterating all the seats which are lie in radius
                for (n in tempPostList!!.indices) {
                    if (tempPostList!![n].inRange) {

                        currentMarkerList?.add(tempPostList!![n])


                        if (backendlessUser.objectId.equals(tempPostList!![n].user.userId)) {

                            Prefs.putString("seatId", tempPostList!![n].objectId)
                            Prefs.putBoolean("isLive", true)
                            startService(Intent(this@NeedASeatActivity, LocationService::class.java))
                            Prefs.putInt("userStatus", 1)
                            HawkUtils.putHawk("postedSeatLive", tempPostList!![n])
                            if (tempPostList!![n].invite != null) {
                                if (tempPostList!![n].invite.status.equals("accepted")) {
                                    Prefs.putBoolean("inMeeting", true)
                                    Prefs.putInt("userStatus", 2)
                                    Prefs.putBoolean("isLive", true)
                                    HawkUtils.putHawk("inMeetingPost", tempPostList!![n])

                                } else if (tempPostList!![n].invite.status.equals("declined") || tempPostList!![n].invite.status.equals("cancelled")) {
                                    Prefs.putBoolean("inMeeting", false)
                                    Prefs.putInt("userStatus", 1)
                                    Prefs.putBoolean("isLive", true)
                                    //removeInvitation(tempPostList!![n].invite.objectId)

                                } else if ((tempPostList!![n].invite.status.equals("ended"))) {
                                    Prefs.putBoolean("inMeeting", false)
                                    Prefs.putInt("userStatus", 1)
                                    Prefs.putBoolean("isLive", false)
                                    //removeInvitation(tempPostList!![n].invite.objectId)

                                }
                            }
                        } else {

                            if (tempPostList!![n].invite != null) {

                                if (tempPostList!![n].invite.sender.objectId.equals(backendlessUser.objectId) &&
                                        tempPostList!![n].invite.receiver.objectId.equals(tempPostList!![n].user.objectId)) {

                                    HawkUtils.putHawk("invites", tempPostList!![n].invite)
                                    if (tempPostList!![n].invite.status.equals("accepted")) {
                                        /*invite.sender.objectId.equals(user.objectId) && invite.receiver.objectId.equals(selectedPost?.user?.objectId*/
                                        HawkUtils.putHawk("inMeetingPost", tempPostList!![n])
                                        Prefs.putBoolean("inMeeting", true)
                                        Prefs.putInt("userStatus", 2)
                                        HawkUtils.putHawk("inMeetingPost", tempPostList!![n])
                                    } else if (tempPostList!![n].invite.status.equals("declined") || tempPostList!![n].invite.status.equals("cancelled")) {
                                        Prefs.putBoolean("inMeeting", false)
                                        //removeInvitation(tempPostList!![n].invite.objectId)
                                    } else if ((tempPostList!![n].invite.status.equals("ended"))) {
                                        Prefs.putBoolean("inMeeting", false)
                                        Prefs.putInt("userStatus", 1)
                                        //removeInvitation(tempPostList!![n].invite.objectId)
                                    }
                                }

                            }
                        }
                    }


//                    else {
//                        if(Prefs.getString("seatId","").equals(tempPostList!![n].objectId)) {
//
//                            if(tempPostList!![n].shouldGoLive == "yes") {
//                                Prefs.remove("seatId")
//                               // removeUserSeat(tempPostList!![n].objectId)
//                                if(tempPostList!![n].invite != null) {
//                                    removeInvitation(tempPostList!![n].invite.objectId)
//                                }
//                            }
//
//                        }
//                    }
                }

                if (currentMarkerList!!.isEmpty()) {


                    ObservableObject.getInstance().updateValue("listIsEmpty")
                    no_seat_layout_nd.visibility = View.VISIBLE
                    sliding_tabs_main.visibility = View.GONE


//                    Prefs.remove("isLive")
//                    Prefs.remove("inMeeting")
//                    Prefs.remove("userStatus")
//                    Prefs.remove("inviteId")
//                    Prefs.remove("inviteStatus")
//                    Prefs.remove("seatId") }no_pin_layout.visibility = View.VISIBLE
                } else {


                    val llList: MutableList<Post>? = arrayListOf()

                    AppClass.postList = currentMarkerList
                    HawkUtils.putHawk("postList", currentMarkerList)
                    ObservableObject.getInstance().updateValue("showList")

                    for (i in currentMarkerList!!.indices) {

                        if (currentMarkerList!![i].shouldGoLive.equals("yes")) {
                            llList?.add(currentMarkerList!![i])
                        }
                    }

                    if (llList!!.isEmpty()) {
                        no_seat_layout_nd.visibility = View.VISIBLE
                        sliding_tabs_main.visibility = View.GONE


                    } else {
                        no_seat_layout_nd.visibility = View.GONE
                        sliding_tabs_main.visibility = View.VISIBLE

                    }

//                    if(hasSeatLiveNo && currentMarkerList!!.size > 0) {
//                        no_pin_layout.visibility = View.GONE
//
//                    }
//                    else  {
//                        no_pin_layout.visibility = View.VISIBLE
//                    }

                }
                pd.dismiss()
                // progressBar.visibility = View.INVISIBLE
                setupAppBar()
                ObservableObject.getInstance().updateValue(1)


                if (!Prefs.getBoolean("introDone", false)) {
                    startActivity(Intent(this@NeedASeatActivity, AppIntroActivity::class.java))
                }

//                sequence = TapTargetSequence(this@NeedASeatActivity)
//                        .targets(
//
//                                TapTarget.forView(btn_post_seat_main,"Post a Seat!","If you have seats available post them by tapping the add button.")
//                                        .outerCircleColor(R.color.colorPrimaryDark)
//                                        .targetCircleColor(R.color.ef_grey)
//                                        .titleTextColor(android.R.color.white)
//                                        .transparentTarget(true),
//                                TapTarget.forView(appbar_back_need_seat,"More menus!","This will give you quick access to perform useful actions like People Around Me and Contacts")
//                                        .outerCircleColor(android.R.color.white)
//                                        .targetCircleColor(R.color.colorPrimaryDark)
//                                        .titleTextColor(android.R.color.black)
//                                        .transparentTarget(true),
//                                TapTarget.forView(appbar_img_bell_need_seat,"SeatCheck Invitation","You can access your invitation you received from other SeatCheck users. ")
//                                        .outerCircleColor(R.color.nd_light_pink_dark)
//                                        .targetCircleColor(R.color.colorPrimaryDark)
//                                        .titleTextColor(android.R.color.black)
//                                        .transparentTarget(true),
//                                TapTarget.forView(appbar_img_chat,"SeatCheck Chat!","You will see the users who chatted wit you.")
//                                        .outerCircleColor(R.color.colorPrimaryDark)
//                                        .targetCircleColor(R.color.ef_grey)
//                                        .titleTextColor(android.R.color.white)
//
//                                        .transparentTarget(true)
//                        ).listener(object : TapTargetSequence.Listener{
//                            override fun onSequenceCanceled() {
//
//                                showAgain()
//                            }
//
//                            override fun onSequenceFinish() {
//                                showAgain()
//                            }
//
//                        })
//
//                if(Prefs.getBoolean("showAgain",true)) {
//                    sequence?.start()
//                }
            }

            override fun handleFault(fault: BackendlessFault) {
                //progressBar.visibility = View.INVISIBLE
                pd.dismiss()


            }
        })


    }


    private fun removeUserSeat(post: Post) {


        if (post.user != null) {
            if (post.user.objectId.equals(backendlessUser.objectId)) {

                Backendless.Data.of(Post::class.java).remove(post, object : AsyncCallback<Long> {
                    override fun handleFault(fault: BackendlessFault?) {
                        Log.d("NeedSeatActivity", "User post not removed")
                    }

                    override fun handleResponse(response: Long?) {
                        Utils.cancelAlarm(this@NeedASeatActivity)
                        Log.d("NeedSeatActivity", "User post removed")
                        Prefs.remove("postedSeatLat")
                        Prefs.remove("postedSeatLng")
                        findAndDeleteInvites()
                    }
                })
            }

//            Backendless.Data.of(Post::class.java).findById(post.objectId, object : AsyncCallback<Post>{
//                override fun handleFault(fault: BackendlessFault?) {
//                    Log.d("TAG",fault?.message)
//
//
//                }
//                override fun handleResponse(response: Post?) {
//                    Backendless.Data.of(Post::class.java).remove(response, object  : AsyncCallback<Long> {
//                        override fun handleResponse(response: Long?) {
//                            Log.d("TAG","Seat Remove..")
//
//                        }
//
//                        override fun handleFault(fault: BackendlessFault?) {
//                            Log.d("TAG",fault?.message)
//
//                        }
//
//                    })
//                }
//            })
        }
//        else {
//            findAndDeleteInvites()
//        }
    }

    private fun inRange(latCenter: Double, lngCenter: Double, latDest: Double, lngDest: Double, radius: Int): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(latCenter, lngCenter, latDest, lngDest, results)
        val distanceInMeters = results[0]
        val isWithinRange = distanceInMeters < radius

        return isWithinRange
    }


    private val onSeebarChangeTimeout = Runnable {

        currentMarkerList?.clear()
        tempPostList?.clear()
        fetchthroughSeekbar(AppClass.allPost!!, seekbarInt!!)

//        mapFragment.zoomCameraUpdate(lat!!,lng!!,seekbarInt!!)
//        fetchPostViaModelForRadiusSearch(seekbarInt!!)
    }


    override fun onDestroy() {
        super.onDestroy()
        ObservableObject.getInstance().deleteObserver(this)
    }


    private fun setHeader() {

        Glide.with(this@NeedASeatActivity).load(backendlessUser.properties["profileImage"]).apply(options).into(dp_drawer)
        name_drawer.setText(backendlessUser.properties["name"].toString())
        appbar_img_chat.setOnClickListener {

            startActivity(Intent(this@NeedASeatActivity, ChatListActivity::class.java))
        }


    }

    private fun setupAppBar() {


        setHeader()
        if (Prefs.getBoolean("isLive", false) && Prefs.getInt("userStatus", 0) == 1) {
            appbar_img_live.visibility = View.VISIBLE

        } else if (Prefs.getBoolean("inMeeting", false) && Prefs.getInt("userStatus", 0) == 2) {
            appbar_img_live.visibility = View.VISIBLE
            appbar_img_live.setImageResource(R.drawable.ico_meeting)

        } else {
            appbar_img_live.visibility = View.GONE
        }


        appbar_img_live.setOnClickListener {


            if (Prefs.getBoolean("isLive", false) && Prefs.getInt("userStatus", 0) == 1) {
                findSeat(Prefs.getString("seatId", ""), "Fetching live details...", this@NeedASeatActivity)
            } else if (Prefs.getBoolean("inMeeting", false) && Prefs.getInt("userStatus", 0) == 2) {

                val invite = HawkUtils.getHawk("invites") as Invites
                if (Prefs.getString("userId", "").equals(invite.sender.properties!!["objectId"])) {
                    startActivity(Intent(this@NeedASeatActivity, ActivitySeatAccepted::class.java))
                } else {
                    findSeat(Prefs.getString("seatId", ""), "Fetching live details...", this@NeedASeatActivity)
                }

            }
        }

        appbar_img_bell_need_seat.setOnClickListener {

            startActivity(Intent(this@NeedASeatActivity, ActivityShowInvites::class.java))
        }
        appbar_back_need_find.setOnClickListener {

            startActivity(Intent(this@NeedASeatActivity, PeopleAroundMeActivitiy::class.java))
        }
//        appbar_back_need_seat.setOnClickListener {
//            finish()
//        }

        appbar_back_need_seat.setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)
        }


        Glide.with(this@NeedASeatActivity).load(backendlessUser.properties["profileImage"].toString()).apply(options).into(appbar_img_need_seat)

        appbar_img_refresh_need_seat.setOnClickListener {

            fetchPostViaModelForRadiusSearch(seekbarInt!!, this@NeedASeatActivity)
        }

        appbar_img_need_seat.setOnClickListener {
            startActivity(Intent(this@NeedASeatActivity, ProfileActivity::class.java))
        }
    }


    private fun removeInvitation(invitesId: String?) {


        Backendless.Data.of(Invites::class.java).findById(invitesId, object : AsyncCallback<Invites> {
            override fun handleFault(fault: BackendlessFault?) {
                Log.d("TAG", fault?.message)

            }

            override fun handleResponse(response: Invites?) {
                Backendless.Data.of(Invites::class.java).remove(response, object : AsyncCallback<Long> {
                    override fun handleResponse(response: Long?) {
                        Log.d("TAG", "Invites Delete")

                        // call this to finish the current activity   pd.dismiss()
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        Log.d("TAG", fault?.message)

                    }

                })
            }
        })
    }


    fun findSeat(seatId: String?, message: String?, context: Context) {


        val postList = HawkUtils.getHawk("postList") as MutableList<Post>
        for (i in postList.indices) {
            if (postList[i].objectId.equals(seatId)) {

                val post = postList[i]
                if (Prefs.getString("userId", "").equals(post?.user?.properties!!["ownerId"])) {
                    var intent: Intent? = null

                    if (Prefs.getBoolean("inMeeting", false)) {
                        intent = Intent(context, ActivitySeatLiveInMeeting::class.java)
//                        intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                        intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                        intent.putExtra("seatcheck_location_name",post.resturantName)
//                        intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                        intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//                        intent.putExtra("seatcheck_post_id",post.objectId)
//                        intent.putExtra("seats",post.quantity.toInt())
//                        intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
                        AppClass.selectedPost = post
                        startActivity(intent)

                    } else {
                        intent = Intent(context, ActivitySeatLive::class.java)
//                        intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                        intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                        intent.putExtra("seatcheck_location_name",post.resturantName)
//                        intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                        intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//
//                        intent.putExtra("seatcheck_post_id",post.objectId)
//                        intent.putExtra("seats",post.quantity.toInt())
//                        intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
                        AppClass.selectedPost = post
                        startActivity(intent)
                    }

                } else {
                    val intent = Intent(context, SeatCheckUserActivity::class.java)
//                    intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                    intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                    intent.putExtra("seatcheck_location_name",post.resturantName)
//                    intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                    intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//
//                    intent.putExtra("seatcheck_post_id",post.objectId)
//                    intent.putExtra("seats",post.quantity.toInt())
//
//                    intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
                    AppClass.selectedPost = post
                    startActivity(intent)
                }
            }
        }


        /*--------------===========*/


//        val pd = Utils.SCProgressDialog(context!!,null,message!!)
//        pd.show()
//
//        Backendless.Data.of(Post::class.java).findById(seatId, object : AsyncCallback<Post>{
//            override fun handleFault(fault: BackendlessFault?) {
//                Log.d("TAG",fault?.message)
//                Toast.makeText(context!!,fault?.message,Toast.LENGTH_LONG).show()
//                pd.dismiss()
//
//            }
//            override fun handleResponse(post: Post) {
//                pd.dismiss()
//
//
//                var intent : Intent? = null
//                AppClass.selectedPost = post
//                if(Prefs.getBoolean("isLive",false) &&  Prefs.getInt("userStatus",0) == 1) {
//
//                    intent =Intent(context,ActivitySeatLive::class.java)
//                    intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                    intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                    intent.putExtra("seatcheck_location_name",post.resturantName)
//                    intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                    intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//
//                    intent.putExtra("seatcheck_post_id",post.objectId)
//                    intent.putExtra("seats",post.quantity.toInt())
//                    intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
//
//                    startActivity(intent)
//
//                }
//                else if(Prefs.getBoolean("inMeeting",false) &&  Prefs.getInt("userStatus",0) == 2){
//
//
//                    if(Prefs.getString("userId","").equals(post?.user?.properties!!["ownerId"])) {
//
//                        startActivity(Intent(context, ActivitySeatLiveInMeeting::class.java))
//                    }else {
//                        startActivity(Intent(context, ActivitySeatAccepted::class.java))
//                    }
//                }


//                if(Prefs.getString("userId","").equals(post?.user?.properties!!["ownerId"])) {
//
//
////
////                        if(Prefs.getBoolean("inMeeting",false)){
////                        intent =Intent(context,ActivitySeatLiveInMeeting::class.java)
////                        intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                        intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                        intent.putExtra("seatcheck_location_name",post.resturantName)
////                        intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                        intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////                        intent.putExtra("seatcheck_post_id",post.objectId)
////                        intent.putExtra("seats",post.quantity.toInt())
////                        intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
////                        AppClass.selectedPost = post
////                        startActivity(intent)
////
////                    }else {
////                        intent =Intent(context,ActivitySeatLive::class.java)
////                        intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                        intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                        intent.putExtra("seatcheck_location_name",post.resturantName)
////                        intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                        intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////
////                        intent.putExtra("seatcheck_post_id",post.objectId)
////                        intent.putExtra("seats",post.quantity.toInt())
////                        intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
////                        AppClass.selectedPost = post
////                        startActivity(intent)
////                    }
////
////                }else {
////                    val intent = Intent(context,SeatCheckUserActivity::class.java)
////                    intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                    intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                    intent.putExtra("seatcheck_location_name",post.resturantName)
////                    intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                    intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////
////                    intent.putExtra("seatcheck_post_id",post.objectId)
////                    intent.putExtra("seats",post.quantity.toInt())
////
////                    intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
////                    AppClass.selectedPost = post
////                    startActivity(intent)
////                }
//
//            }
//        })
    }

    fun fetchTime(createdTwo: String): String {
        val date = SimpleDateFormat("MM/dd/yyyy hh:mm:ss").parse(createdTwo)
        return SimpleDateFormat("HH:mm:ss").format(date) // 9:00
    }

    inner class LogOutTask(private val mContext: Context) : AsyncTask<Void, Void, Long>() {


        // val pd = Utils.SCProgressDialog(mContext,null,"Please wait..")
        override fun onPreExecute() {
            super.onPreExecute()
            // pd.show()
            progressBar.visibility = View.VISIBLE


        }

        override fun onPostExecute(result: Long?) {
            //pd.dismiss()

            //Utils.dismissProgressDialog(pd!!)
            progressBar.visibility = View.INVISIBLE

            Prefs.remove("isLive")
            Prefs.remove("seatId")
            Prefs.remove("userStatus")
            Prefs.remove("isLogin")
            Prefs.remove("socialLogin")
            Prefs.putInt("rating", 0)
            Prefs.remove("introDone")
            HawkUtils.deleteHawk()
            Utils.cancelAlarm(this@NeedASeatActivity)
            val intent = Intent(mContext, NDLoginSingupWithActivitiy::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // call this to finish the current activity   pd.dismiss()


        }

        override fun doInBackground(vararg p0: Void?): Long {

            for (i in 1..2) {
                Thread.sleep(1000)
            }

            return 0
        }


    }


    private fun fetchthroughSeekbar(allPostList: MutableList<Post>, _miles: Int) {

        tempPostList = arrayListOf()
        currentMarkerList = arrayListOf()
        val miles = 1609

        for (i in allPostList!!.indices) {


            if (inRange(lat!!, lng!!, allPostList[i].resturantLocationLatitude.toDouble(), allPostList[i].resturantLocationLongitude.toDouble(), miles.times(_miles))) {
                allPostList!![i].inRange = true
                tempPostList!!.add(allPostList[i])

            }

        }


        //itterating all the seats which are lie in radius
        for (n in tempPostList!!.indices) {

            //Comparing times..
            if (Utils.compareTwoDates(tempPostList!![n].createdTwo, Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse())) {

                if (tempPostList!![n].shouldGoLive.equals("yes") || tempPostList!![n].shouldGoLive.equals("no")) {

                    if (tempPostList!![n].shouldGoLive.equals("no")) {
                        hasSeatLiveNo = true
                    }

                    if (tempPostList!![n].inRange) {
                        currentMarkerList?.add(tempPostList!![n])

                    }
                }
            }
        }

        AppClass.postList = currentMarkerList
        HawkUtils.putHawk("postList", currentMarkerList)
        ObservableObject.getInstance().updateValue("showList")


    }

    fun quote(s: String): String {
        return StringBuilder()
                .append('\'')
                .append(s)
                .append('\'')
                .toString()
    }


    private fun findInvitesUserSQL() {

        val currentObjectId = quote(backendlessUser.objectId)
        val queryStatus = quote("pending")
        val whereClause = "receiver.objectId = $currentObjectId AND status = $queryStatus"
        val asdfa = "toUser.objectId = $currentObjectId"

        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        Backendless.Persistence.of(Invites::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Invites>> {

                    override fun handleResponse(foundInvites: BackendlessCollection<Invites>) {
                        var invitesList: MutableList<Invites>? = arrayListOf()
                        for (i in foundInvites.data.indices) {

                            if (foundInvites.data[i].receiver != null) {
                                val invite = Invites()
                                invite.objectId = foundInvites.data[i].objectId.toString()
                                invite.status = foundInvites.data[i].objectId.toString()
                                invite.receiver = foundInvites.data[i].receiver as BackendlessUser
                                invite.sender = foundInvites.data[i].sender as BackendlessUser
                                // invite.requestCreated = foundInvites.data[i].requestCreated.toString()

                                invitesList?.add(invite)
                            }

                        }

                        AppClass.inviteCounter = invitesList?.size!!
                        if (AppClass.inviteCounter > 0) {
                            invite_counter.visibility = View.VISIBLE
                            invite_counter.text = AppClass.inviteCounter.toString()
                        } else {
                            invite_counter.visibility = View.GONE
                        }

                    }

                    override fun handleFault(fault: BackendlessFault) {

                    }
                })


    }

    override fun onResume() {
        super.onResume()

        //Need to uncomment following
        findInvitesUserSQL()

//
        if (googleApiClient != null) {
            setupLocationManager()
        }


//         val manager =  getSystemService( Context.LOCATION_SERVICE ) as LocationManager
//        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
//                setupLocationManager()
//        }else {
//
//        }

//        if(Utils.isConnectedOnline(this)) {
//            findInvitesUserSQL()
//        }else {
//            Utils.showAlertDialog(this,"Opps","Internet is not connected.")
//
//        }

        if (Prefs.getInt("rating", 0) > 0) {
            ratingBar_rating_stars.visibility = View.VISIBLE
            ratingBar_rating_stars.rating = Prefs.getInt("rating", 0).toFloat()
        } else {
            ratingBar_rating_stars.visibility = View.GONE
        }

//        if(JavaUtilSeatCheck.isLocationEnabled(this)) {
//            findInvitesUserSQL()
//
//            if(Prefs.getInt("rating",0) > 0) {
//                ratingBar_rating_stars.visibility = View.VISIBLE
//                ratingBar_rating_stars.rating = Prefs.getInt("rating",0).toFloat()
//            }else {
//                ratingBar_rating_stars.visibility = View.GONE
//            }
//        }else {
//            setupLocationManager()
//        }


    }


    private fun showRemoveSeatDialog() {
        val builder = AlertDialog.Builder(this@NeedASeatActivity)
        builder.setMessage("Do you want to Logout?")
                .setTitle("Confirm!")

        builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {

                p0?.dismiss()
                val logOutTask = LogOutTask(this@NeedASeatActivity)
                logOutTask.execute()

            }

        }).setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {

                p0?.dismiss()
            }

        })

        val dialog = builder.create()
        dialog.show()
    }


    private fun findAndDeleteInvites() {
        val currentObjectId = quote(backendlessUser.objectId)
        val whereClause = "receiver.objectId = $currentObjectId"
        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        Backendless.Persistence.of(Invites::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Invites>> {

                    override fun handleResponse(foundInvites: BackendlessCollection<Invites>) {

                        for (i in foundInvites.data.indices) {
                            removeInvitation(foundInvites.data[i].objectId)
                        }

                    }

                    override fun handleFault(fault: BackendlessFault) {

                        Log.d("error", "Invites delete error")


                    }
                })
    }


    private fun refreshToken() {


        Backendless.Data.of(Users::class.java).findById(backendlessUser.objectId, object : AsyncCallback<Users> {
            override fun handleFault(fault: BackendlessFault?) {

            }


            override fun handleResponse(response: Users?) {
                var token = FirebaseInstanceId.getInstance().token
                response?.fcmToken = token


            }


        })

    }


    private fun setSeatCheckUser() {

        val mService = ApiUtils.getSOService()
        val url = EndPointsConstants.SET_USER
        val tz = TimeZone.getDefault()
        // Toast.makeText(this@NeedASeatActivity,tz.id,Toast.LENGTH_LONG).show()

        mService.setSeatCheckChatUser(url,
                Utils.getSimpleTextBody(backendlessUser.properties["name"].toString()),
                Utils.getSimpleTextBody(backendlessUser.email),
                Utils.getSimpleTextBody(backendlessUser.objectId),
                Utils.getSimpleTextBody(backendlessUser.properties["profileImage"].toString()),
                Utils.getSimpleTextBody(backendlessUser.properties["status"].toString()),
                Utils.getSimpleTextBody(backendlessUser.properties["fcmToken"].toString()),
                Utils.getSimpleTextBody(lat.toString()),
                Utils.getSimpleTextBody(lng.toString()),
                Utils.getSimpleTextBody(tz.id)

        ).enqueue(object : Callback<SucessResponse> {
            override fun onFailure(call: Call<SucessResponse>?, t: Throwable?) {
                Toast.makeText(this@NeedASeatActivity, t?.message, Toast.LENGTH_LONG).show()

            }

            override fun onResponse(call: Call<SucessResponse>?, response: Response<SucessResponse>?) {

                if (response?.isSuccessful!!) {

                    if (response.body().success) {

                        Prefs.putBoolean("seatCheckChatUserSet", true)
                        Prefs.putInt("chat_user_id", response.body().chat_user_id)

                        if (response.body().visibility == 1) {
                            Prefs.putBoolean("visibility", false)
                        } else {
                            Prefs.putBoolean("visibility", true)
                        }
                        //Toast.makeText(this@NeedASeatActivity,response.body()?.message,Toast.LENGTH_LONG).show()
                    } else {
                        Prefs.putBoolean("seatCheckChatUserSet", false)
                        //Toast.makeText(this@NeedASeatActivity,response.body()?.message,Toast.LENGTH_LONG).show()

                    }
                } else {
                    Toast.makeText(this@NeedASeatActivity, response.message(), Toast.LENGTH_LONG).show()

                }
            }

        })

    }


    private fun getReviews() {
        val currentObjectId = quote(backendlessUser.objectId)
        val whereClause = "toUser.objectId = $currentObjectId"
        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        Backendless.Persistence.of(Reviews::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Reviews>> {

                    override fun handleResponse(foundContacts: BackendlessCollection<Reviews>) {

                        var sum = 0
                        for (i in foundContacts.data.indices) {

                            val star = foundContacts.data[i].numberOfStars
                            sum = sum.plus(star.toInt())
                        }

                        if (foundContacts.data.size != 0) {
                            val rating = sum.div(foundContacts.data.size)
                            Prefs.putInt("rating", rating)
                        } else {
                            Prefs.putInt("rating", 0)

                        }


                        if (Prefs.getInt("rating", 0) > 0) {
                            ratingBar_rating_stars.visibility = View.VISIBLE
                            ratingBar_rating_stars.rating = Prefs.getInt("rating", 0).toFloat()
                        } else {
                            ratingBar_rating_stars.visibility = View.GONE
                        }


                    }

                    override fun handleFault(fault: BackendlessFault) {


                        if (fault?.message?.contains("Unable to resolve host “api.backendless.com”: No address associated with hostname")!!) {

                            // Utils.showAlertDialogWithFinish(this@MainActivity,"Connectivity Error","It seems internet connection is down. Please connect your phone with network and try again")

                        } else {
                            // Toast.makeText(this@MainActivity,"Something went wrong..",Toast.LENGTH_LONG).show()

                        }

                    }
                })
    }

    private fun setGeoPoints(lat: Double?, lng: Double?) {


        val geoPoint = GeoPoint(lat!!, lng!!)
        backendlessUser.properties["location"] = geoPoint

        Backendless.Data.save(backendlessUser, object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {
                Toast.makeText(this@NeedASeatActivity, fault?.message, Toast.LENGTH_LONG).show()
            }

            override fun handleResponse(response: BackendlessUser?) {
                Toast.makeText(this@NeedASeatActivity, "GeoLocation Set Successfully!", Toast.LENGTH_LONG).show()
                Log.d("GeoPoint", "GeoPointSetSuccessfully")
            }

        })


    }

    private fun showAgain() {
        val builder = AlertDialog.Builder(this@NeedASeatActivity)
        builder.setMessage("Do you want to see again when app restarts ")
                .setCancelable(false)

        builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {

                p0?.dismiss()
                Prefs.putBoolean("showAgain", true)

            }

        }).setNegativeButton("No", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                Prefs.putBoolean("showAgain", false)

                p0?.dismiss()
            }

        })

        val dialog = builder.create()
        dialog.show()
    }


}

