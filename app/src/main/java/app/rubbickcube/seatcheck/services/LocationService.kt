package app.rubbickcube.seatcheck.services


import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import android.util.Log

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.pixplicity.easyprefs.library.Prefs

import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.SplashActivity
import app.rubbickcube.seatcheck.model.Post
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.orhanobut.hawk.Hawk

/***
 * This is background service class, which is handle user location time to time, using GPS or Network provider,
 * This class runs in background, and its start when the app start.
 */

class LocationService : Service() {
    private var mLocationManager: LocationManager? = null

    internal var mLocationListeners = arrayOf(LocationListener(LocationManager.GPS_PROVIDER), LocationListener(LocationManager.NETWORK_PROVIDER))


    inner class LocationListener(provider: String) : android.location.LocationListener {
        internal var mLastLocation: Location

        init {
            Log.e(TAG, "LocationListener $provider")
            mLastLocation = Location(provider)

        }

        //Whenever the device detects new location, this method will trigger..
        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location")



            if(location != null) {
                mLastLocation.set(location)
                if(AppClass.lat !== null) {
                    val latPost =  Prefs.getDouble("postedSeatLat",AppClass.lat!!)
                    val lngPost =  Prefs.getDouble("postedSeatLng",AppClass.lat!!)
                    val latLngPost = LatLng(latPost!!, lngPost!!)
                    val latLngUser = LatLng(mLastLocation.latitude, mLastLocation.longitude)
                    val distance = SphericalUtil.computeDistanceBetween(latLngUser, latLngPost)

                    if (distance > 1000) {

                        if(Prefs.getBoolean("isLive",false)) {
                            if(HawkUtils.getHawk("postedSeatLive") != null) {
                                val seat = HawkUtils.getHawk("postedSeatLive") as Post
                                removeSeat(seat)
                            }
                        }

                    }
                }
            }

        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $provider")
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.e(TAG, "onStartCommand")
        Hawk.init(this).build()
        //Toast.makeText(this,"Service started",Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        //        _pin = intent.getStringExtra("pin");


        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")

        initializeLocationManager()
        try {
            mLocationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                    mLocationListeners[1])
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

        try {
            mLocationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                    mLocationListeners[0])
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.message)
        }

    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
        if (mLocationManager != null) {
            for (i in mLocationListeners.indices) {
                try {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }
                    mLocationManager!!.removeUpdates(mLocationListeners[i])
                    //geoFire.removeLocation(_pin);

                } catch (ex: Exception) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex)
                }

            }
        }
    }

    private fun initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager")
        if (mLocationManager == null) {
            mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

    }


    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        private val TAG = "FENCE-APP-SERVICE"
        private val LOCATION_INTERVAL = 1000 //Every one second, userlocation requested by the syste,
        private val LOCATION_DISTANCE = 100f //location request will trigger after user move the distance of 100Meter
    }


    private fun fireSeatRemoveNotification() {


        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                999,
                Notification.Builder(this).apply {
                    setSmallIcon(R.drawable.app_icon)
                    setContentTitle("Seat Removed!")
                    setContentText("Your seat has been removed since you moved.")
                    setWhen(System.currentTimeMillis())
                    setPriority(Notification.PRIORITY_HIGH)
                    setAutoCancel(true)
                    setDefaults(Notification.DEFAULT_SOUND)
                    setContentIntent(PendingIntent.getActivity(this@LocationService, 0, Intent(this@LocationService,SplashActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                }.build()
        )
//
//
//         val notifyID = 1;
//         val mBuilder =
//                 NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.app_icon)
//                        .setContentTitle("Android Notification")
//                        .setContentText("This is simple notification without action.")
//                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) as NotificationCompat.Builder
//
//        val mNotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        //Issuing Notification
//        mNotificationManager.notify(notifyID, mBuilder.build());

    }

    private fun removeSeat(post : Post) {

        Backendless.Persistence.of(Post::class.java).remove(post,object : AsyncCallback<Long> {
            override fun handleFault(fault: BackendlessFault?) {
                Log.d("TAG",fault?.message)
            }

            override fun handleResponse(response: Long?) {

                if(AppClass.appIsInForground) {

                    AppClass.message = "Your seat has been removed since you moved"
                    AppClass.title = ""
                    AppClass.notificationFor = "expired"
                    val intent =  Intent()
                    intent.action = "app.rubbickcube.seatcheck.SHOW_DIALOG"

                   //fireSeatRemoveNotification()
                    sendBroadcast(intent)
                }else {
                    fireSeatRemoveNotification()
                }

                Prefs.putBoolean("isLive",false)
                Prefs.putInt("userStatus",0)
                HawkUtils.deleteHawk("postedSeatLive")
                Prefs.remove("postedSeatLat")
                Prefs.remove("postedSeatLng")
                Utils.cancelAlarm(this@LocationService)
                stopSelf()

            }

        })
    }


}