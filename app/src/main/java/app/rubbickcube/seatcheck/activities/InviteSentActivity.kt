package app.rubbickcube.seatcheck.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.ObservableObject
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.model.Invites
import app.rubbickcube.seatcheck.model.Post
import app.rubbickcube.seatcheck.services.LocationService
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
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_invite_sent.*
import kotlinx.android.synthetic.main.app_bar_layout_for_activity_seat.*
import kotlinx.android.synthetic.main.need_seat_layou.*
import java.util.*
import javax.inject.Inject

class InviteSentActivity : AppCompatActivity(), Observer {
    override fun update(o: Observable?, arg: Any?) {

        if(arg is String) {
            if(arg.equals("Request Accepted")) {
                findInvitation(intent.getStringExtra("inviteId"))
            }
        }

    }


    private var tempPostList : MutableList<Post>? = null
    private var changeRadiusPostList : MutableList<Post>? = null
    private var currentMarkerList : MutableList<Post>? = null

    var backendlessUser : BackendlessUser = BackendlessUser()
    val options =  RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite_sent)
        supportActionBar?.hide()
        Hawk.init(this).build()

        if(Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        }else {
            return
        }
        ObservableObject.getInstance().addObserver(this)
        //AppClass.appComponent?.inject(this)

        setupAppbar()

        text_invited_user.text = "Waiting for "+ AppClass.selectedPost?.user?.properties!!["name"].toString()+" to respond..."
        Glide.with(this@InviteSentActivity).load(AppClass.selectedPost?.user?.properties!!["profileImage"]).apply(options).into(img_invited_user)

        //findInvitation(intent.getStringExtra("inviteId"))

    }


    private fun findInvitation(inviteId : String? ){


        Backendless.Data.of(Invites::class.java).findById(inviteId, object : AsyncCallback<Invites> {
            override fun handleFault(fault: BackendlessFault?) {
                Log.d("TAG",fault?.message)
                Toast.makeText(this@InviteSentActivity,fault?.message,Toast.LENGTH_LONG).show()

            }
            override fun handleResponse(invites: Invites?) {

                if(invites?.status.equals("pending")) {


//                    Handler().postDelayed({
//                        findInvitation(inviteId)
//                    },3000)

                }else if(invites?.status.equals("accepted")) {
                    //AppClass.invites = invites

                    val post = AppClass.selectedPost
                    post?.invite = invites

                    HawkUtils.putHawk("inMeetingPost",post)

                    HawkUtils.putHawk("invites",invites)
                    Prefs.putString("inviteStatus","accepted")
                    Prefs.putString("seatId",AppClass.selectedPost?.objectId)

                    startActivity(Intent(this@InviteSentActivity, ActivitySeatAccepted::class.java))
                    finish()
                   //fetchPostViaModelForRadiusSearch(2,this@InviteSentActivity,invites!!)

                }else if(invites?.status.equals("declined")) {

                    //Toast.makeText(this@InviteSentActivity,AppClass.selectedPost?.user?.properties!!["name"].toString() +" has denied your request",Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        })
    }

    private fun setupAppbar() {

//        if(AppClass.inviteCounter > 0) {
//            invite_counter.visibility = View.VISIBLE
//            invite_counter.text = AppClass.inviteCounter.toString()
//        }else {
//            invite_counter.visibility = View.GONE
//        }
        appbar_back.setOnClickListener {
            finish()
        }

        appbar_img_bell.setOnClickListener {
            val intent = Intent(this@InviteSentActivity, ActivityShowInvites::class.java)
            startActivity(intent)
            finish()
        }

        appbar_img_home.setOnClickListener {
            val intent = Intent(this@InviteSentActivity, NeedASeatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }


            Glide.with(this@InviteSentActivity).load(backendlessUser.properties["profileImage"].toString()).apply(options).into(appbar_img)

    }

//    private fun fetchPostViaModelForRadiusSearch(seekbar: Int,context : Context) {
//
//         var tempPostList : MutableList<Post>? = null
//         var changeRadiusPostList : MutableList<Post>? = null
//         var currentMarkerList : MutableList<Post>? = null
//        progressBar.visibility = View.VISIBLE
//        Backendless.Persistence.of<Post>(Post::class.java).find(object : AsyncCallback<BackendlessCollection<Post>> {
//            override fun handleResponse(foundPost: BackendlessCollection<Post>) {
//
//                tempPostList = arrayListOf()
//                changeRadiusPostList = foundPost.data
//                currentMarkerList = arrayListOf()
//                AppClass.allPost = foundPost.data
//                val miles = 1609
//                currentMarkerList!!.clear()
//
//                for(i in foundPost.data!!.indices) {
//                    if(inRange (AppClass.lat!!,AppClass.lng!!,foundPost.data[i].resturantLocationLatitude.toDouble(),foundPost.data[i].resturantLocationLongitude.toDouble(),miles.times(seekbar))) {
//                        if(Utils.compareTwoDates(foundPost.data!![i].createdTwo, Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()) ) {
//                            foundPost.data!![i].inRange = true
//                            if(foundPost.data[i].objectId.equals(foundPost.data[i].objectId) &&  foundPost.data[i].shouldGoLive.equals("no")) {
//                                if(foundPost.data[i].invite != null) {
//                                    if(foundPost.data[i].invite.status.equals("accepted")) {
//                                        tempPostList!!.add(foundPost.data[i])
//                                    }
//                                }else {
//                                    tempPostList!!.add(foundPost.data[i])
//                                }
//                            }else {
//                                tempPostList!!.add(foundPost.data[i])
//
//                            }
//                        }else {
//                            removeUserSeat(foundPost.data[i])
//                        }
//                    }else {
//                        removeUserSeat(foundPost.data[i])
//                    }
//                }
//
//                //Second Loop starts here
//                //itterating all the seats which are lie in radius
//                for(n in tempPostList!!.indices) {
//                    if(tempPostList!![n].inRange) {
//
//                        currentMarkerList?.add(tempPostList!![n])
//
//
//                        if(Prefs.getString("userId","").equals(tempPostList!![n].user.userId)) {
//
//                            Prefs.putString("seatId",tempPostList!![n].objectId)
//                            Prefs.putBoolean("isLive",true)
//                            startService(Intent(this@NeedASeatActivity, LocationService::class.java))
//                            Prefs.putInt("userStatus",1)
//                            HawkUtils.putHawk("postedSeatLive",tempPostList!![n])
//                            if(tempPostList!![n].invite != null) {
//                                if(tempPostList!![n].invite.status.equals("accepted")) {
//                                    Prefs.putBoolean("inMeeting",true)
//                                    Prefs.putInt("userStatus",2)
//                                    Prefs.putBoolean("isLive",true)
//
//
//                                }else if(tempPostList!![n].invite.status.equals("declined") || tempPostList!![n].invite.status.equals("cancelled")) {
//                                    Prefs.putBoolean("inMeeting", false)
//                                    Prefs.putInt("userStatus", 1)
//                                    Prefs.putBoolean("isLive", true)
//                                    //removeInvitation(tempPostList!![n].invite.objectId)
//
//                                }else if((tempPostList!![n].invite.status.equals("ended"))){
//                                    Prefs.putBoolean("inMeeting",false)
//                                    Prefs.putInt("userStatus",1)
//                                    Prefs.putBoolean("isLive",false)
//                                    //removeInvitation(tempPostList!![n].invite.objectId)
//
//                                }
//                            }
//                        } else {
//
//
//                            if(tempPostList!![n].invite != null) {
//                                HawkUtils.putHawk("invites",tempPostList!![n].invite)
//                                if(tempPostList!![n].invite.status.equals("accepted")) {
//                                    Prefs.putBoolean("inMeeting",true)
//                                    Prefs.putInt("userStatus",2)
//                                }else if(tempPostList!![n].invite.status.equals("declined") || tempPostList!![n].invite.status.equals("cancelled")) {
//                                    Prefs.putBoolean("inMeeting", false)
//                                    //removeInvitation(tempPostList!![n].invite.objectId)
//                                }else if((tempPostList!![n].invite.status.equals("ended"))){
//                                    Prefs.putBoolean("inMeeting",false)
//                                    Prefs.putInt("userStatus",1)
//                                    //removeInvitation(tempPostList!![n].invite.objectId)
//                                }
//                            }
//                        }
//                    }
////                    else {
////                        if(Prefs.getString("seatId","").equals(tempPostList!![n].objectId)) {
////
////                            if(tempPostList!![n].shouldGoLive == "yes") {
////                                Prefs.remove("seatId")
////                               // removeUserSeat(tempPostList!![n].objectId)
////                                if(tempPostList!![n].invite != null) {
////                                    removeInvitation(tempPostList!![n].invite.objectId)
////                                }
////                            }
////
////                        }
////                    }
//                }
//
//                if(currentMarkerList!!.isEmpty()) {
//
//
//                    ObservableObject.getInstance().updateValue("listIsEmpty")
//                    no_pin_layout.visibility = View.VISIBLE
//                    sliding_tabs_main.visibility = View.GONE
//
//
//
////                    Prefs.remove("isLive")
////                    Prefs.remove("inMeeting")
////                    Prefs.remove("userStatus")
////                    Prefs.remove("inviteId")
////                    Prefs.remove("inviteStatus")
////                    Prefs.remove("seatId") }no_pin_layout.visibility = View.VISIBLE
//                }else {
//
//
//                    val llList : MutableList<Post>? = arrayListOf()
//
//                    AppClass.postList = currentMarkerList
//                    HawkUtils.putHawk("postList",currentMarkerList)
//                    ObservableObject.getInstance().updateValue("showList")
//
//                    for(i in currentMarkerList!!.indices) {
//
//                        if(currentMarkerList!![i].shouldGoLive.equals("yes")) {
//                            llList?.add(currentMarkerList!![i])
//                        }
//                    }
//
//                    if(llList!!.isEmpty()) {
//                        no_pin_layout.visibility = View.VISIBLE
//                        sliding_tabs_main.visibility = View.GONE
//
//                    }else {
//                        no_pin_layout.visibility = View.GONE
//                        sliding_tabs_main.visibility = View.VISIBLE
//
//
//                    }
//
//
////                    if(hasSeatLiveNo && currentMarkerList!!.size > 0) {
////                        no_pin_layout.visibility = View.GONE
////
////                    }
////                    else  {
////                        no_pin_layout.visibility = View.VISIBLE
////                    }
//
//
//
//
//                }
//                progressBar.visibility = View.INVISIBLE
//                setupAppBar()
//                ObservableObject.getInstance().updateValue(1)
//
//
//            }
//
//            override fun handleFault(fault: BackendlessFault) {
//                progressBar.visibility = View.INVISIBLE
//
//            }
//        })
//
//
//    }

    private fun fetchPostViaModelForRadiusSearch(seekbar: Int, context : Context,invites: Invites) {


        var tempPostList : MutableList<Post>? = null
        var changeRadiusPostList : MutableList<Post>? = null
        var currentMarkerList : MutableList<Post>? = null

        Backendless.Persistence.of<Post>(Post::class.java).find(object : AsyncCallback<BackendlessCollection<Post>> {
            override fun handleResponse(foundPost: BackendlessCollection<Post>) {


                tempPostList = arrayListOf()
                changeRadiusPostList = foundPost.data
                currentMarkerList = arrayListOf()
                AppClass.allPost = foundPost.data
                val miles = 1609

                currentMarkerList!!.clear()

                for(i in foundPost.data!!.indices) {
                    if(inRange (AppClass.lat!!,AppClass.lng!!,foundPost.data[i].resturantLocationLatitude.toDouble(),foundPost.data[i].resturantLocationLongitude.toDouble(),miles.times(seekbar))) {
                        if (Utils.compareTwoDates(foundPost.data!![i].createdTwo, Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse())) {
                            foundPost.data!![i].inRange = true
                            tempPostList!!.add(foundPost.data[i])
                        }
                    }
                }

                //Second Loop starts here
                //itterating all the seats which are lie in radius
                for(n in tempPostList!!.indices) {
                    if(tempPostList!![n].inRange) {
                        currentMarkerList?.add(tempPostList!![n])
                    }
                }

                if(currentMarkerList!!.isEmpty()) {


                }else {

                    AppClass.postList = currentMarkerList
                    HawkUtils.putHawk("postList",currentMarkerList)


                }



                HawkUtils.putHawk("invites",invites)
                Prefs.putString("inviteStatus","accepted")
                Prefs.putString("seatId",AppClass.selectedPost?.objectId)

                startActivity(Intent(this@InviteSentActivity, ActivitySeatAccepted::class.java))
                finish()

            }

            override fun handleFault(fault: BackendlessFault) {

                HawkUtils.putHawk("invites",invites)
                Prefs.putString("inviteStatus","accepted")
                Prefs.putString("seatId",AppClass.selectedPost?.objectId)

                startActivity(Intent(this@InviteSentActivity, ActivitySeatAccepted::class.java))
                finish()
            }
        })


    }
    private fun inRange(latCenter : Double, lngCenter : Double, latDest : Double, lngDest : Double, radius : Int) : Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(latCenter, lngCenter, latDest, lngDest, results)
        val distanceInMeters = results[0]
        val isWithinRange = distanceInMeters < radius

        return isWithinRange
    }

    override fun onDestroy() {
        super.onDestroy()
        ObservableObject.getInstance().deleteObserver(this)
    }


}
