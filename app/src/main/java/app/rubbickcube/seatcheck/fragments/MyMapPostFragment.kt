package app.rubbickcube.seatcheck.fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.AsyncTask
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.ObservableObject
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.SeatCheckUserActivity
import app.rubbickcube.seatcheck.activities.ActivitySeatLive
import app.rubbickcube.seatcheck.model.Availability
import app.rubbickcube.seatcheck.model.Invites
import app.rubbickcube.seatcheck.model.Post
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.pixplicity.easyprefs.library.Prefs
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyMapPostFragment : SupportMapFragment(), OnMapReadyCallback, Observer {

    private var markers: MutableList<Post>? = arrayListOf()
    private var currentMarkerList: MutableList<Post>? = arrayListOf()
    private var mMap: GoogleMap? = null

    val options = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap?) {

        mMap = map as GoogleMap
        map.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = false
        // mMap?.uiSettings?.setAllGesturesEnabled(false)
        mMap?.uiSettings?.setAllGesturesEnabled(false)
        ObservableObject.getInstance().addObserver(this)

        styleMap(R.raw.style, mMap!!)

        mMap?.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener { marker ->
            if (marker.title != null) {
                val _id = marker.title.replace("m", "")

                if (HawkUtils.getHawk("postList") != null) {

                    val postList = HawkUtils.getHawk("postList") as MutableList<Post>
                    for (i in postList.indices) {
                        if (postList[i].objectId.equals(_id)) {

                            val post = postList[i]
                            if (Prefs.getString("userId", "").equals(post?.user?.properties!!["ownerId"])) {
                                var intent: Intent? = null

                                if (Prefs.getBoolean("inMeeting", false)) {
//                                    intent =Intent(context, ActivitySeatLiveInMeeting::class.java)
//                                    intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                                    intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                                    intent.putExtra("seatcheck_location_name",post.resturantName)
//                                    intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                                    intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//                                    intent.putExtra("seatcheck_post_id",post.objectId)
//                                    intent.putExtra("seats",post.quantity.toInt())
//                                    intent.putExtra("extendedTime",post.extendedTime)
//                                    intent.putExtra("created",post.created)
//
//                                    intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
                                    AppClass.selectedPost = post
                                    startActivity(intent)

                                } else {
                                    intent = Intent(context, ActivitySeatLive::class.java)
//                                    intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                                    intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                                    intent.putExtra("seatcheck_location_name",post.resturantName)
//                                    intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                                    intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//                                    intent.putExtra("extendedTime",post.extendedTime)
//                                    intent.putExtra("created",post.created)
//
//                                    intent.putExtra("seatcheck_post_id",post.objectId)
//                                    intent.putExtra("seats",post.quantity.toInt())
//                                    intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
                                    AppClass.selectedPost = post
                                    startActivity(intent)
                                }

                            } else {
                                val intent = Intent(context, SeatCheckUserActivity::class.java)
//                                intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                                intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                                intent.putExtra("seatcheck_location_name",post.resturantName)
//                                intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                                intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//                                intent.putExtra("extendedTime",post.extendedTime)
//                                intent.putExtra("created",post.created)
//
//                                intent.putExtra("seatcheck_post_id",post.objectId)
//                                intent.putExtra("seats",post.quantity.toInt())
//
//                                intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
                                AppClass.selectedPost = post
                                startActivity(intent)
                            }
                        }
                    }
                }


                // findSeat(_id,"Fetching seat details...")
            }
            true
        })
    }

    override fun update(p0: Observable?, p1: Any?) {

        var zoomTo: Int? = null
        if (p1 is JSONObject) {

            val latlngObject = JSONObject(p1.toString())
            val currentLocation = LatLng(latlngObject.getDouble("lat"), latlngObject.getDouble("lng"))

            //mMap?.addMarker(MarkerOptions().position(currentLocation).title(latlngObject.getString("address")))
            val update = CameraUpdateFactory.newLatLngZoom(currentLocation, 16f)
            mMap?.moveCamera(update)

        } else if (p1 is Int) {
            if (p1 != null) {
                zoomCameraUpdate(AppClass.lat!!, AppClass.lng!!, p1)

            }
        }

    }

    fun zoomCameraUpdate(lat: Double, lng: Double, progress: Int) {
        val currentLocation = LatLng(lat, lng)
        //mMap?.addMarker(MarkerOptions().position(currentLocation).title(latlngObject.getString("address")))
        var zoomTo = 18 - progress
        //var zoomTo = 20 - progress

        val update = CameraUpdateFactory.newLatLngZoom(currentLocation, zoomTo.toFloat())
        mMap?.moveCamera(update)
    }

    fun plotMarkers(list: MutableList<Post>) {
        mMap?.clear()

        for (i in list!!.indices) {

            val currentLocation = LatLng(list[i].resturantLocationLatitude.toDouble(), list[i].resturantLocationLongitude.toDouble())
//            mMap?.addMarker(MarkerOptions().position(currentLocation).title(list[i].objectId))

            if (list[i].showOnMap) {
                var marker = R.drawable.new_marker_icon
                if (list[i].record.interestOption.equals("Drink on me")) {
                    marker =R.drawable.ic_drink
                } else if (list[i].record.interestOption.equals("Food on me")) {
                    marker =R.drawable.ic_food
                } else if (list[i].record.interestOption.equals("Appetizer on me")) {
                    marker =R.drawable.ic_appitizer
                } else {
                    marker = R.drawable.seat
                }
                Glide.with(context!!).asBitmap().load(marker).apply(options)
                        .into(object : SimpleTarget<Bitmap>(70, 99) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                //val bitmap = createCustomMarker(context!!,resource)
                                mMap?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(resource)).position(currentLocation).title(list[i].objectId))

                            }

                        })
//                if(list[i].user.properties["profileImage"] != null) {
//
//                    //mMap?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.new_marker_icon)).position(currentLocation).title(list[i].objectId))
//
//                    Glide.with(context!!).asBitmap().load(R.drawable.new_marker_icon).apply(options)
//                            .into(object : SimpleTarget<Bitmap>(100,129) {
//                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                                    //val bitmap = createCustomMarker(context!!,resource)
//                                    mMap?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(resource)).position(currentLocation).title(list[i].objectId))
//
//                                }
//
//                            })
//                }else {
//                    val mBitmap = BitmapFactory.decodeResource(resources,R.drawable.avatar)
//                    mMap?.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(context!!,mBitmap))).position(currentLocation).title(list[i].objectId))
//
//                }
            }


        }
    }


    private fun getCuurentDateTime(): String {

        val timeInMillis = System.currentTimeMillis()
        val cal1 = Calendar.getInstance()
        cal1.timeInMillis = timeInMillis
        val dateFormat = SimpleDateFormat(
                "MM/dd/yyyy hh:mm:ss")


        return dateFormat.format(cal1.time)
    }


    fun createCustomMarker(context: Context, profileImage: Bitmap?): Bitmap {

        val marker = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.marker_layout, null)

        val markerImage = marker.findViewById(R.id.user_dp_marker) as CircleImageView


        markerImage.setImageBitmap(profileImage)

        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        marker.setLayoutParams(ViewGroup.LayoutParams(60, ViewGroup.LayoutParams.WRAP_CONTENT))
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)

        marker.buildDrawingCache()

        val bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        marker.draw(canvas)

        return bitmap
    }

    private fun drawMap(latLng: LatLng, positions: List<Post>, mMap: GoogleMap, markersList: MutableList<Marker>, radius: Int) {
        for (position in positions) {
            val marker = mMap?.addMarker(
                    MarkerOptions()
                            .position(LatLng(position.resturantLocationLatitude.toDouble(), position.resturantLocationLongitude.toDouble()))
                            .visible(false)) // Invisible for now
            markersList.add(marker)
        }

        for (marker in markersList) {
            if (SphericalUtil.computeDistanceBetween(latLng, marker.position) < radius) {
                marker.isVisible = true
            }
        }
    }

//       fun findSeat(seatId : String?, message : String? ){
//        val pd = Utils.SCProgressDialog(context!!,null,message!!)
//       pd.show()
//
//    Backendless.Data.of(Post::class.java).findById(seatId, object : AsyncCallback<Post>{
//        override fun handleFault(fault: BackendlessFault?) {
//            Log.d("TAG",fault?.message)
//            Utils.dismissProgressDialog(pd!!)
//
//            val seatExpireTask = SeatExpireTask(context!!)
//            seatExpireTask.execute()
//
//
//        }
//        override fun handleResponse(post: Post?) {
//            Utils.dismissProgressDialog(pd!!)
//
//
//            val backendlessUser = HawkUtils.getHawk("BackendlessUser") as BackendlessUser
//            if(backendlessUser.objectId.equals(post?.user?.properties!!["ownerId"])) {
//                var intent : Intent? = null
//
//                if(Prefs.getBoolean("inMeeting",false)){
//                    intent =Intent(context,ActivitySeatLiveInMeeting::class.java)
////                        intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                        intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                        intent.putExtra("seatcheck_location_name",post.resturantName)
////                        intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                        intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////                        intent.putExtra("seatcheck_post_id",post.objectId)
////                        intent.putExtra("seats",post.quantity.toInt())
////                        intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
////                        AppClass.selectedPost = post
//                    startActivity(intent)
//
//                }else {
//                    intent =Intent(context,ActivitySeatLive::class.java)
////                        intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                        intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                        intent.putExtra("seatcheck_location_name",post.resturantName)
////                        intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                        intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////
////                        intent.putExtra("seatcheck_post_id",post.objectId)
////                        intent.putExtra("seats",post.quantity.toInt())
////                        intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
//                    AppClass.selectedPost = post
//                    startActivity(intent)
//                }
//
//            }else {
//                val intent = Intent(context,SeatCheckUserActivity::class.java)
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
//                AppClass.selectedPost = post
//                startActivity(intent)
//            }
//
//        }
//    })
//}

    inner class SeatExpireTask(private val mContext: Context) : AsyncTask<Void, Void, Long>() {


        val pd = Utils.SCProgressDialog(mContext, null, "Seat is expired..")
        override fun onPreExecute() {
            super.onPreExecute()
            pd.show()

        }

        override fun onPostExecute(result: Long?) {
            Utils.dismissProgressDialog(pd!!)

            ObservableObject.getInstance().updateValue("Refresh")
        }

        override fun doInBackground(vararg p0: Void?): Long {

            for (i in 1..3) {
                Thread.sleep(1000)
            }

            return 0
        }

    }


    fun fetchTime(createdTwo: String): String {
        val date = SimpleDateFormat("MM/dd/yyyy hh:mm:ss").parse(createdTwo)
        return SimpleDateFormat("HH:mm:ss").format(date) // 9:00
    }

    fun clearMap() {
        mMap?.clear()
    }


    private fun styleMap(style: Int, mMap: GoogleMap) {

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.

            val success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context, style))

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MapsActivityRaw", "Can't find style.", e)
        }

    }



}

