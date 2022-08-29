package app.rubbickcube.seatcheck

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.activities.*
import app.rubbickcube.seatcheck.model.Invites
import app.rubbickcube.seatcheck.model.Post
import app.rubbickcube.seatcheck.model.Reviews
import com.backendless.Backendless
import com.backendless.BackendlessCollection
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.BackendlessDataQuery
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_for_main_activity.*
import kotlinx.android.synthetic.main.nav_panel_layout.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    var backendlessUser: BackendlessUser = BackendlessUser()
    private var tempPostList: MutableList<Post>? = null
    private var changeRadiusPostList: MutableList<Post>? = null
    private var currentMarkerList: MutableList<Post>? = null

    private var REQUEST_CHECK_SETTINGS = 1000
    private var googleApiClient: GoogleApiClient? = null
    private var mLastLocation: Location? = null
    private var request: LocationRequest? = null
    var lat: Double? = null
    var lng: Double? = null

    val options = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //  AppClass.appComponent?.inject(this)
        supportActionBar?.hide()
        Hawk.init(this).build()


        if (Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        } else {
            return
        }
        Glide.with(this@MainActivity).load(R.drawable.nd_need_post_seat_background).into(img_nd_back_main)
        Glide.with(this@MainActivity).load(R.drawable.nd_ineed_seat).into(need_seat)
        Glide.with(this@MainActivity).load(R.drawable.nd_ihave_seat).into(post_seat)

        Backendless.initApp(this@MainActivity, resources.getString(R.string.appId), resources.getString(R.string.secretKey), "v1")
        getReviews()

        setupLocationManager()


//
//        if(Prefs.getBoolean("inMeeting",false) &&  Prefs.getInt("userStatus",0) == 2) {
//            checkInviteStatus()
//        }

        drawer_ico_for_nav.setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)
        }

        drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //drawerLayout?.visibility = View.GONE

        initializeComponents()

    }

    private fun initializeComponents() {
        setHeader()
        post_seat.setOnClickListener {
            if (AppClass.lat != null) {
                if (Prefs.getBoolean("isLive", false)) {
                    Utils.showAlertDialog(this@MainActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
                } else if (Prefs.getBoolean("inMeeting", false)) {
                    Utils.showAlertDialog(this@MainActivity, "Sorry!", "You can't post new seat while you are live or in a meeting");
                } else {
                    startActivity(Intent(this@MainActivity, PostSeatActivity::class.java))
                }
            } else {
                Toast.makeText(this@MainActivity, "Seatcheck unable to find your location. Please try again or restart app", Toast.LENGTH_SHORT).show()
            }
        }

        need_seat.setOnClickListener {

            startActivity(Intent(this@MainActivity, NeedASeatActivity::class.java))
            finish()
        }

        ll_profile.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
        }

        ll_help.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@MainActivity, ActivityHelp::class.java))
        }
        ll_setting.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }

        ll_chat.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@MainActivity, ChatListActivity::class.java))
        }

        ll_chats.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            startActivity(Intent(this@MainActivity, ChatActivity::class.java))
        }

        ll_logout.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            //  Backendless.UserService.logout();


            ///removeDeviceToken()
//            val logOutTask = LogOutTask(this@MainActivity)
//            logOutTask.execute()


        }

        ll_invite.setOnClickListener {
            drawerLayout?.closeDrawer(GravityCompat.START)
            //  Backendless.UserService.logout();

            Toast.makeText(this@MainActivity, "Its causing issue, trying to resolve.", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this@MainActivity,ActivityShowInvites::class.java))


        }
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
                                this@MainActivity,
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
                    Toast.makeText(this@MainActivity, "Location enabled by user!", Toast.LENGTH_LONG).show()
                    // mRequestingLocationUpdates = true
                }
                Activity.RESULT_CANCELED -> {
                    // The user was asked to change settings, but chose not to
                    finish()
                    Toast.makeText(this@MainActivity, "You can not use Seatcheck with our your current location.", Toast.LENGTH_LONG).show()
                    //mRequestingLocationUpdates = false
                }

                else -> {
                }
            }
        }
    }


    private fun setInitialLocation() {

        if (ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

            AppClass.lat = mLastLocation?.latitude
            AppClass.lng = mLastLocation?.longitude
            getUserSeats(backendlessUser.objectId)


//            AppClass.lat = 24.993515700000003
//            AppClass.lng = 67.0585369
//             AppClass.lat = 33.739552
//            AppClass.lng = 72.725388


//            name_details.text = mLastLocation?.latitude.toString() +" , "+mLastLocation?.longitude
//            getBloodBanks(mLastLocation?.latitude!!.toString(),mLastLocation?.longitude!!.toString(),"16093.44",resources.getString(R.string.google_maps_key))
//            val adapter =  GooglePlacesAutocompleteAdapter(this@SearhRestaurentActivity, R.layout.autocompletelistitem);
//            location_text.setAdapter(adapter);
//            location_text.setOnItemClickListener { adapterView, view, i, l ->
//
//
//                val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                inputManager!!.hideSoftInputFromWindow(view.applicationWindowToken, 0)
//                val str = adapterView.getItemAtPosition(i) as String
//                val places = str.split("@")
//                val place_id = places[1]
//
//                location_text.setText("")
//                location_text.hint = places[0]
//                //getLatLng Method is not built-in method, find this method below
//                getPlaceDetails(place_id)
//            }

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
        Toast.makeText(this@MainActivity, "Google api client is not connected", Toast.LENGTH_LONG)

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

    private fun setHeader() {


        if (!Prefs.getString("profileImage", "").isNullOrEmpty()) {
            Glide.with(this@MainActivity).load(Prefs.getString("profileImage", "")).apply(options).into(dp_drawer)
        }

        name_drawer.setText(backendlessUser.properties["name"].toString())

    }

    inner class LogOutTask(private val mContext: Context) : AsyncTask<Void, Void, Long>() {


        val pd = Utils.SCProgressDialog(mContext, null, "Please wait..")
        override fun onPreExecute() {
            super.onPreExecute()
            pd.show()

        }

        override fun onPostExecute(result: Long?) {
            Utils.dismissProgressDialog(pd)
            Prefs.remove("isLive")
            Prefs.remove("seatId")
            Prefs.remove("userStatus")
            Prefs.remove("isLogin")
            Prefs.putInt("rating", 0)
            HawkUtils.deleteHawk()
            Utils.cancelAlarm(this@MainActivity)
            val intent = Intent(mContext, SplashActivity::class.java)
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

//    private fun fetchPostViaModelForRadiusSearch(seekbar: Int,context : Context) {
//
//        val pd = Utils.SCProgressDialog(context!!,null,"Finding seats nearby you...")
//        pd.show()
//        Backendless.Persistence.of<Post>(Post::class.java).find(object : AsyncCallback<BackendlessCollection<Post>> {
//            override fun handleResponse(foundPost: BackendlessCollection<Post>) {
//
//                if(pd.isShowing) {
//                    pd.dismiss()
//                }
//
//
//
//
//
//                tempPostList = arrayListOf()
//                changeRadiusPostList = foundPost.data
//                val miles = 1609
//
//
//                for(i in foundPost.data!!.indices) {
//
//
//                    if (inRange(Prefs.getDouble("lat",0.0),Prefs.getDouble("lng",0.0),foundPost.data[i].resturantLocationLatitude.toDouble(),foundPost.data[i].resturantLocationLongitude.toDouble(),miles.times(seekbar))) {
//                        foundPost.data!![i].inRange = true
//                        tempPostList!!.add(foundPost.data[i])
//                    }else {
//                        if(Prefs.getString("userId","").equals(foundPost.data!![i].user.userId)) {
//
//                            removeUserSeat(foundPost.data!![i].objectId)
//                            Prefs.remove("isLive")
//                        }
//                    }
//                }
//
//
//                currentMarkerList = arrayListOf()
//                for(n in tempPostList!!.indices) {
//
//                    if(Utils.compareTime(Utils.converTimetoGMT(tempPostList!![n].createdTwo), Utils.getCurrentDateTimeofDevice())) {
////                        if(tempPostList!![n].shouldGoLive.equals("yes")) {
////
////                        }
//                        if(tempPostList!![n].inRange) {
//                            currentMarkerList?.add(tempPostList!![n])
//                            if(Prefs.getString("userId","").equals(currentMarkerList!![n].user.userId)) {
//                                Prefs.putBoolean("isLive",true)
//                            }
//                        }
//                    }
//                    else {
//                        if(Prefs.getString("userId","").equals(tempPostList!![n].user.userId)) {
//
//                            removeUserSeat(tempPostList!![n].objectId)
//                            Prefs.remove("isLive")
//                        }
//                    }
//
//                }
//
//                if(currentMarkerList!!.isEmpty()) {
//                    ObservableObject.getInstance().updateValue("listIsEmpty")
//                    Prefs.remove("isLive")
//                    no_pin_layout.visibility = View.VISIBLE
//                }else {
//
//                    no_pin_layout.visibility = View.GONE
//                    AppClass.postList = currentMarkerList
//                    ObservableObject.getInstance().updateValue("showList")
//
//
//                }
//
//            }
//
//            override fun handleFault(fault: BackendlessFault) {
//
//                pd.dismiss()
//            }
//        })
//    }


    private fun checkInviteStatus() {


        if (HawkUtils.getHawk("invites") != null) {
            val invite = HawkUtils.getHawk("invites") as Invites

            Backendless.Persistence.of(Invites::class.java).findById(invite.objectId, object : AsyncCallback<Invites> {
                override fun handleFault(fault: BackendlessFault?) {
                    Prefs.putBoolean("inMeeting", false)
                    Prefs.remove("seatId")

                    //Toast.makeText(this@MainActivity,fault?.message,Toast.LENGTH_SHORT).show()

                }

                override fun handleResponse(response: Invites?) {


                    if (response?.status.equals("cancelled")) {
                        Prefs.putBoolean("inMeeting", false)
                        Prefs.remove("seatId")

                    } else if (response?.status.equals("ended")) {
                        Prefs.putBoolean("inMeeting", false)
                        Prefs.remove("seatId")
                    } else if (response?.status.equals("declined")) {
                        Prefs.putBoolean("inMeeting", false)
                        Prefs.remove("seatId")
                    } else if (response?.status.equals("accepted")) {
                        Prefs.putBoolean("inMeeting", true)
                        Prefs.putInt("userStatus", 2)
                    }
                }

            })
        }

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


                    }

                    override fun handleFault(fault: BackendlessFault) {


                        if (fault?.message?.contains("Unable to resolve host “api.backendless.com”: No address associated with hostname")!!) {

                            Utils.showAlertDialogWithFinish(this@MainActivity, "Connectivity Error", "It seems internet connection is down. Please connect your phone with network and try again")

                        } else {
                            Toast.makeText(this@MainActivity, "Something went wrong..", Toast.LENGTH_LONG).show()

                        }


                    }
                })
    }


    fun quote(s: String): String {
        return StringBuilder()
                .append('\'')
                .append(s)
                .append('\'')
                .toString()
    }


    private fun getUserSeats(userId: String) {


        val currentObjectId = Utils.quote(userId)
        val whereClause = "User.objectId = $currentObjectId"
        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        Backendless.Persistence.of(Post::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Post>> {

                    override fun handleResponse(foundPost: BackendlessCollection<Post>) {


                        tempPostList = arrayListOf()
                        changeRadiusPostList = foundPost.data
                        currentMarkerList = arrayListOf()
                        AppClass.allPost = foundPost.data
                        val miles = 1609

                        currentMarkerList!!.clear()

                        for (i in foundPost.data!!.indices) {
                            if (inRange(lat!!, lng!!, foundPost.data[i].resturantLocationLatitude.toDouble(), foundPost.data[i].resturantLocationLongitude.toDouble(), miles.times(1))) {

                                if (Utils.compareTwoDates(foundPost.data!![i].createdTwo, Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse())) {
                                    foundPost.data!![i].inRange = true
                                    tempPostList!!.add(foundPost.data[i])
                                }

                            }
                        }

                        for (n in tempPostList!!.indices) {
                            if (tempPostList!![n].inRange) {

                                currentMarkerList?.add(tempPostList!![n])
                                if (Prefs.getString("userId", "").equals(tempPostList!![n].user.userId)) {

                                    //  Prefs.putString("seatId",tempPostList!![n].objectId)
                                    Prefs.putBoolean("isLive", true)
                                }
                            }
                        }


                        AppClass.postList = currentMarkerList
                        HawkUtils.putHawk("postList", currentMarkerList)

                    }

                    override fun handleFault(fault: BackendlessFault) {


                    }
                })
    }


    private fun inRange(latCenter: Double, lngCenter: Double, latDest: Double, lngDest: Double, radius: Int): Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(latCenter, lngCenter, latDest, lngDest, results)
        val distanceInMeters = results[0]
        val isWithinRange = distanceInMeters < radius

        return isWithinRange
    }

    override fun onStart() {
        super.onStart()
    }


}
