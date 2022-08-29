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
import android.widget.Toast
import app.rubbickcube.seatcheck.GooglePlacesAutocompleteAdapter
import app.rubbickcube.seatcheck.ObservableObject
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.fragments.MyMapFragment
import app.rubbickcube.seatcheck.fragments.MyMapPostFragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes

import com.google.android.libraries.places.api.Places;
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.first_restaurent_list_item.*
import kotlinx.android.synthetic.main.searc_restaurent_layout.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class ShowPinOnMapActivity : AppCompatActivity() , GoogleApiClient.ConnectionCallbacks,
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
        setContentView(R.layout.activity_show_pin_on_map)

        initializeComponents()
    }


    private fun initializeComponents() {



        val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as MyMapPostFragment
        mapFragment.getMapAsync(mapFragment)

        setupLocationManager()




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
                                this@ShowPinOnMapActivity,
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
                    Toast.makeText(this@ShowPinOnMapActivity, "Location enabled by user!", Toast.LENGTH_LONG).show()
                    // mRequestingLocationUpdates = true
                }
                Activity.RESULT_CANCELED -> {
                    // The user was asked to change settings, but chose not to
                    Toast.makeText(this@ShowPinOnMapActivity, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show()
                    //mRequestingLocationUpdates = false
                }

                else -> {
                }
            }
        }
    }


    private fun setInitialLocation() {

        if (ActivityCompat.checkSelfPermission(this@ShowPinOnMapActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@ShowPinOnMapActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        Toast.makeText(this@ShowPinOnMapActivity,"Google api client is not connected", Toast.LENGTH_LONG)

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
}
