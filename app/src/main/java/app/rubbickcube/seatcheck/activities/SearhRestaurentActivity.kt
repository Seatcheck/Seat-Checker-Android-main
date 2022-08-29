package app.rubbickcube.seatcheck.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import app.rubbickcube.seatcheck.*
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.adapters.RestaurentLocationAdapter
import app.rubbickcube.seatcheck.fragments.MyMapFragment
import app.rubbickcube.seatcheck.model.PlaceDetails
import app.rubbickcube.seatcheck.model.Vicinity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*

import com.google.android.libraries.places.api.Places;
import com.loopj.android.http.AsyncHttpResponseHandler
import com.pixplicity.easyprefs.library.Prefs
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.activity_searh_restaurent.*
import kotlinx.android.synthetic.main.first_restaurent_list_item.*
import kotlinx.android.synthetic.main.searc_restaurent_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

class SearhRestaurentActivity : AppCompatActivity() , GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private var mLayout: SlidingUpPanelLayout? = null
    private var TAG: String? = "searchRestaurent"

    private var REQUEST_CHECK_SETTINGS = 1000
    private var googleApiClient: GoogleApiClient? = null
    private var mLastLocation: Location? = null
    private var request: LocationRequest? = null
    private var pd: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searh_restaurent)
        supportActionBar?.hide()

        initializeComponents()
    }

    private fun initializeComponents() {



        val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as MyMapFragment
        mapFragment.getMapAsync(mapFragment)

        setupLocationManager()

        mLayout = findViewById<View>(R.id.sliding_layout) as SlidingUpPanelLayout
        mLayout?.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                Log.i(TAG, "onPanelSlide, offset $slideOffset")
            }

            override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {
                Log.i(TAG, "onPanelStateChanged $newState")
            }
        })
        mLayout?.setFadeOnClickListener(View.OnClickListener { mLayout?.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED) })


        myLocation.setOnClickListener {

            val latlngObject = JSONObject()
            latlngObject.put("lat",mLastLocation?.latitude)
            latlngObject.put("lng",mLastLocation?.longitude)
            latlngObject.put("address","")

            val data = getStringAddress(mLastLocation?.latitude!!,mLastLocation?.longitude!!)?.split("@")
            val city = data?.get(0)
            val country = data?.get(1)
            val address = data?.get(2)
            location_text.setText(address)
            ObservableObject.getInstance().updateValue(latlngObject)

        }
//
//        val t = findViewById<View>(R.id.name) as TextView
//        t.text = Html.fromHtml(getString(R.string.hello))
//        val f = findViewById<View>(R.id.follow) as Button
//        f.text = Html.fromHtml(getString(R.string.follow))
//        f.movementMethod = LinkMovementMethod.getInstance()
//        f.setOnClickListener {
//            val i = Intent(Intent.ACTION_VIEW)
//            i.data = Uri.parse("http://www.twitter.com/umanoapp")
//            startActivity(i)
//        }
    }


    private fun  setupLocationManager() {
        //buildGoogleApiClient();
        if (googleApiClient == null) {

            googleApiClient =  GoogleApiClient.Builder(this)
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

        request =  LocationRequest()
        request?.smallestDisplacement = 100f
        request?.fastestInterval = 500000
        request?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        request?.numUpdates = 3

        val builder =  LocationSettingsRequest.Builder()
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
                                this@SearhRestaurentActivity,
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
                    Toast.makeText(this@SearhRestaurentActivity, "Location enabled by user!", Toast.LENGTH_LONG).show()
                    // mRequestingLocationUpdates = true
                }
                Activity.RESULT_CANCELED -> {
                    // The user was asked to change settings, but chose not to
                    Toast.makeText(this@SearhRestaurentActivity, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show()
                    //mRequestingLocationUpdates = false
                }

                else -> {
                }
            }
        }
    }


    private fun setInitialLocation() {

        if (ActivityCompat.checkSelfPermission(this@SearhRestaurentActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@SearhRestaurentActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            val data = getStringAddress(mLastLocation?.latitude!!,mLastLocation?.longitude!!)?.split("@")
            val city = data?.get(0)
            val country = data?.get(1)
            val address = data?.get(2)


            val latlngObject = JSONObject()
            latlngObject.put("lat",mLastLocation?.latitude!!)
            latlngObject.put("lng",mLastLocation?.longitude!!)
            latlngObject.put("address",address)
            ObservableObject.getInstance().updateValue(latlngObject)


            name_details.text = mLastLocation?.latitude.toString() +" , "+mLastLocation?.longitude
            getBloodBanks(mLastLocation?.latitude!!.toString(),mLastLocation?.longitude!!.toString(),"804",resources.getString(R.string.google_maps_key))
            val adapter =  GooglePlacesAutocompleteAdapter(this@SearhRestaurentActivity, R.layout.autocompletelistitem);
            location_text.setAdapter(adapter);
            location_text.setOnItemClickListener { adapterView, view, i, l ->


               val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
               inputManager!!.hideSoftInputFromWindow(view.applicationWindowToken, 0)
               val str = adapterView.getItemAtPosition(i) as String
               val places = str.split("@")
               val place_id = places[1]

               location_text.setText("")
                location_text.hint = places[0]
               //getLatLng Method is not built-in method, find this method below
                getPlaceDetails(place_id)
           }

            //Toast.makeText(this@MainActivity,city, Toast.LENGTH_LONG).show()

            //fetchSalatDetails(city,"815135657a25c9eea03dc1bc6e6e20d6")
            //Prefs.putString(city+", "+country,getStringAddress(location.latitude,location.longitude))

            // val address = Utils.getAddress(this@MainActivity,location.latitude,location.longitude)
            ///Toast.makeText(this@MainActivity,getStringAddress(location.latitude,location.longitude), Toast.LENGTH_LONG).show()
            if(googleApiClient != null)
                googleApiClient?.disconnect()

        }
    }



    override fun onConnected(p0: Bundle?) {

        createLocationRequest()

    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Toast.makeText(this@SearhRestaurentActivity,"Google api client is not connected", Toast.LENGTH_LONG)

    }

    override fun onLocationChanged(p0: Location?) {

    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }

    private fun getStringAddress(_lat: Double ,_lng : Double) : String? {

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


    override fun onStart() {
        if(googleApiClient != null) {
            googleApiClient?.connect()

        }
        super.onStart()
    }

    override fun onStop() {
        if(googleApiClient != null) {
            googleApiClient?.disconnect()
        }
        super.onStop()
    }

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
//
//    fun getLatLangs(placeId: String) {
//        Places.GeoDataApi.getPlaceById(googleApiClient, placeId)
//                .setResultCallback { places ->
//                    if (places.status.isSuccess && places.count > 0) {
//                        val place = places.get(0)
//                        val latLng = place.latLng
//
//
//                        try {
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
//                }
//    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu, menu)
//        val searchItem = menu.findItem(R.id.search)
//        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
//        searchView.setSearchableInfo(
//                searchManager.getSearchableInfo(componentName))
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String): Boolean {
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String): Boolean {
//
//                //Type your search query
//                return true
//            }
//        })
//
//        val expandListener = object : MenuItemCompat.OnActionExpandListener {
//            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
//                return true
//            }
//
//            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
//
//                return true
//            }
//        }
//        MenuItemCompat.setOnActionExpandListener(searchItem, expandListener)
//        return super.onCreateOptionsMenu(menu)
//
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            android.R.id.home -> {
//
//                finish()
//
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    private fun getBloodBanks(lat: String, lng: String, radius: String,apikey: String) {


       val vicinityList = ArrayList<Vicinity>()
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$lat,$lng&radius=$radius&key=AIzaSyALMcA2D2f68EgBXDXYwMgV7hnbpKz_Mro"
       // val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$lat,$lng&radius=$radius&keyword=restaurant&key=$apikey"


        LifeController.get(url, null, object : AsyncHttpResponseHandler() {
            override fun onStart() {
                super.onStart()


            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {

                //ye server se response aya... is jsonobject me dala
                val response = LifeController.getResponseWithoutReplace(responseBody, this@SearhRestaurentActivity)

                Log.d("flags", response)
                try {
                    val `object` = JSONObject(response)
                    val results = `object`.getJSONArray("results")
                    for (i in 0 until results.length()) {

                        val obj = results.getJSONObject(i)
                        vicinityList.add(GsonUtils.fromJSON(obj, Vicinity::class.java))

                    }

                   // vicinityList.get(0).getGeometry().getLocation().getLat()


                    list.adapter = RestaurentLocationAdapter(this@SearhRestaurentActivity, vicinityList)

                    list.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->

                        Prefs.putString("name",vicinityList[i].name)
                        Prefs.putString("vicinity",vicinityList[i].vicinity)
                        Prefs.putString("restaurantLat",vicinityList[i].geometry.location.lat.toString())
                        Prefs.putString("restaurantLng",vicinityList[i].geometry.location.lng.toString())
                        getPhoneNumber(vicinityList[i].place_id)


                    }

                    // Log.d("vicinity",""+vicinityList.get(2).getGeometry().getLocation().getLat()+ " "+vicinityList.get(2).getGeometry().getLocation().getLng() + " " +vicinityList.get(2).getVicinity() + " " + vicinityList.get(2).getPlace_id() +" "+vicinityList.get(2).getTypes().get(0));
                    //    Toast.makeText(JSONParseActivity.this, ""+vicinityList.get(2).getGeometry().getLocation().getLat(), Toast.LENGTH_SHORT).show();

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
               // pd?.dismiss()

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {

               // pd?.dismiss()

                if (Utils.isConnectedOnline(this@SearhRestaurentActivity)) {


                    val json = LifeController.getResponse(responseBody, this@SearhRestaurentActivity)
                    if (json == "null") {
                        Utils.showAlertDialog(this@SearhRestaurentActivity,"....","Something went wrong")
                    }
                    Log.d("error", "Creating Pin $json")


                    try {
                        val `object` = JSONObject(json)

                        val msg = `object`.getString("message")
                        /* if(msg.equals("Token has expired")) {


                        Utils.TokenExpireDialog(RegisterAccount.this,"Token Expired","Your token has expired, click ok to refresh your token");
                    }else {
                        Utils.showAlertDialog(RegisterAccount.this, "Error", msg);
                    }*/
                        Utils.showAlertDialog(this@SearhRestaurentActivity, "Error", msg)


                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    //    Toast.makeText(RegisterAccount.this, ""+json, Toast.LENGTH_SHORT).show();


                } else {

                    Utils.showAlertDialog(this@SearhRestaurentActivity, "", "Internet Connection has failed")

                }

            }


            override fun onFinish() {
                super.onFinish()

            }
        })
    }


    private fun getPlaceDetails(placeId: String) {


        val placeDetailsList = ArrayList<PlaceDetails>()
        val url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=$placeId&key=AIzaSyALMcA2D2f68EgBXDXYwMgV7hnbpKz_Mro"

        LifeController.get(url, null, object : AsyncHttpResponseHandler() {

            override fun onStart() {
                super.onStart()


                //Toast.makeText(this@SearhRestaurentActivity, "Please Wait", Toast.LENGTH_SHORT).show()

                /* dialog = Utils.LifeProgressDialog(BloodBankActivity.this,"Fetching Blood Banks");
                dialog.show();
*/

            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {


                val response = LifeController.getResponseWithoutReplace(responseBody, this@SearhRestaurentActivity)
                try {
                    val `object` = JSONObject(response)
                    val results = `object`.getJSONObject("result")
                    placeDetailsList.add(GsonUtils.fromJSON(results, PlaceDetails::class.java))

                    /*  if(results.getJSONObject("opening_hours"). != null) {

                        JSONObject ohours = results.getJSONObject("opening_hours");
                        boolean isOpen = ohours.getBoolean("open_now");

                        Prefs.putBoolean("isBankOpen",isOpen);
                        if(Prefs.getBoolean("isBankOpen",false)) {

                            _bank_time.setText("Open Now");
                        }else {
                            _bank_time.setText("Close");
                        }
                    }else {

                        _ll_bank_time.setVisibility(View.GONE);
                    }*/


                    val latlngObject = JSONObject()
                    latlngObject.put("lat",placeDetailsList[0].geometry.location.lat)
                    latlngObject.put("lng",placeDetailsList[0].geometry.location.lng)
                    latlngObject.put("address","")
                    ObservableObject.getInstance().updateValue(latlngObject)





                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {

                //dialog.dismiss();

                if (Utils.isConnectedOnline(this@SearhRestaurentActivity)) {


                    val json = LifeController.getResponse(responseBody, this@SearhRestaurentActivity)
                    if (json == "null") {
                        Utils.showAlertDialog(this@SearhRestaurentActivity,"Connection Error","Please check your internet connection..")
                    }
                    Log.d("error", "Creating Pin $json")


                    try {
                        val `object` = JSONObject(json)

                        val msg = `object`.getString("message")
                        /* if(msg.equals("Token has expired")) {

                        Utils.TokenExpireDialog(RegisterAccount.this,"Token Expired","Your token has expired, click ok to refresh your token");
                    }else {
                        Utils.showAlertDialog(RegisterAccount.this, "Error", msg);
                    }*/
                        Utils.showAlertDialog(this@SearhRestaurentActivity, "Error", msg)


                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    //    Toast.makeText(RegisterAccount.this, ""+json, Toast.LENGTH_SHORT).show();

                } else {

                    Utils.showAlertDialog(this@SearhRestaurentActivity, "", "Internet Connection has failed")

                }
            }

            override fun onFinish() {
                super.onFinish()
                //dialog.dismiss();
            }
        })
    }


    private fun getPhoneNumber(placeId: String) {


        val placeDetailsList = ArrayList<PlaceDetails>()
        val url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=$placeId&key=AIzaSyALMcA2D2f68EgBXDXYwMgV7hnbpKz_Mro"

        LifeController.get(url, null, object : AsyncHttpResponseHandler() {

            override fun onStart() {
                super.onStart()


                //Toast.makeText(this@SearhRestaurentActivity, "Please Wait", Toast.LENGTH_SHORT).show()

                /* dialog = Utils.LifeProgressDialog(BloodBankActivity.this,"Fetching Blood Banks");
                dialog.show();
*/

            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {


                val response = LifeController.getResponseWithoutReplace(responseBody, this@SearhRestaurentActivity)
                try {
                    val `object` = JSONObject(response)
                    val results = `object`.getJSONObject("result")
                    placeDetailsList.add(GsonUtils.fromJSON(results, PlaceDetails::class.java))

                    /*  if(results.getJSONObject("opening_hours"). != null) {

                        JSONObject ohours = results.getJSONObject("opening_hours");
                        boolean isOpen = ohours.getBoolean("open_now");

                        Prefs.putBoolean("isBankOpen",isOpen);
                        if(Prefs.getBoolean("isBankOpen",false)) {

                            _bank_time.setText("Open Now");
                        }else {
                            _bank_time.setText("Close");
                        }
                    }else {

                        _ll_bank_time.setVisibility(View.GONE);
                    }*/


                    Prefs.putString("phone",placeDetailsList[0].international_phone_number)
                    finish()





                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {

                //dialog.dismiss();

                if (Utils.isConnectedOnline(this@SearhRestaurentActivity)) {


                    val json = LifeController.getResponse(responseBody, this@SearhRestaurentActivity)
                    if (json == "null") {
                        Utils.showAlertDialog(this@SearhRestaurentActivity,"Connection Error","Please check your internet connection..")
                    }
                    Log.d("error", "Creating Pin $json")


                    try {
                        val `object` = JSONObject(json)

                        val msg = `object`.getString("message")
                        /* if(msg.equals("Token has expired")) {

                        Utils.TokenExpireDialog(RegisterAccount.this,"Token Expired","Your token has expired, click ok to refresh your token");
                    }else {
                        Utils.showAlertDialog(RegisterAccount.this, "Error", msg);
                    }*/
                        Utils.showAlertDialog(this@SearhRestaurentActivity, "Error", msg)


                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    //    Toast.makeText(RegisterAccount.this, ""+json, Toast.LENGTH_SHORT).show();

                } else {

                    Utils.showAlertDialog(this@SearhRestaurentActivity, "", "Internet Connection has failed")

                }
            }

            override fun onFinish() {
                super.onFinish()
                //dialog.dismiss();
            }
        })
    }



//


}
