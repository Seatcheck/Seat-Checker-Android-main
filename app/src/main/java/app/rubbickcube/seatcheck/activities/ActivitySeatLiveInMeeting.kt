package app.rubbickcube.seatcheck.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.model.Invites
import app.rubbickcube.seatcheck.model.Post
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_seat_live_in_meeting.*
import kotlinx.android.synthetic.main.app_bar_layout_for_activity_seat.*
import java.util.*

class ActivitySeatLiveInMeeting : AppCompatActivity() {

    var backendlessUser: BackendlessUser = BackendlessUser()
    private var createdTwo: Date? = null
    private var post_id: String? = null
    var invites: Invites? = null
    var inMeetingPost: Post? = null


    val options = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_live_in_meeting)
        supportActionBar?.hide()
        //AppClass.appComponent?.inject(this)
        Hawk.init(this).build()

        Glide.with(this).load(R.drawable.bottom_curve).into(img_bottom_curve)


        if (Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        } else {
            return
        }
        setupComponents()
        setupListener()

        setupAppbar()
    }

    private lateinit var post: Post

    @SuppressLint("SetTextI18n")
    private fun setupComponents() {
        Backendless.initApp(this@ActivitySeatLiveInMeeting, resources.getString(R.string.appId), resources.getString(R.string.secretKey), "v1")

        if (HawkUtils.getHawk("inMeetingPost") != null) {
            inMeetingPost = HawkUtils.getHawk("inMeetingPost") as Post
        } else {
            Toast.makeText(this@ActivitySeatLiveInMeeting, "Something went wrong...", Toast.LENGTH_SHORT).show()
            return
        }


        for (i in AppClass.postList?.indices!!) {
            if (Prefs.getString("userId", "").equals(AppClass.postList!![i].user.userId)) {
                post = AppClass.postList!![i]
                seatcheck_name.text = post.user?.properties!!["name"].toString()
                seatcheck_location_name.text = post.resturantName
                seatcheck_location_desc.text = post.resturantAddress
                createdTwo = post.createdTwo
                Glide.with(this@ActivitySeatLiveInMeeting).load(post.user?.properties!!["profileImage"].toString()).apply(options).into(seatcheck_live_user_dp)

                post_id = post.objectId

            }
        }

        //  val mins = Utils.calculateRemainTimeinMinute(createdTwo!!)
        current_time_in_meeting.text = "now"
//        if(mins > 5) {
//            current_time_in_meeting.setText(mins.toString() +" min(s) ago")
//        }else {
//            current_time_in_meeting.setText("now")
//        }

        if (HawkUtils.getHawk("invites") != null) {
            invites = HawkUtils.getHawk("invites") as Invites
        }

        Glide.with(this).load(inMeetingPost?.invite?.receiver?.properties!!["profileImage"]).apply(options).into(me_meeting)
        Glide.with(this).load(inMeetingPost?.invite?.sender?.properties!!["profileImage"]).apply(options).into(him_meeting)

        txt_me_meeting.text = inMeetingPost?.invite?.receiver?.properties!!["name"].toString()
        txt_his_meeting.text = inMeetingPost?.invite?.sender?.properties!!["name"].toString()


        sharing_table_text.text = "You and " + inMeetingPost?.invite?.sender?.properties!!["name"].toString() + " are sharing a table now!"
        member_name.text = inMeetingPost?.invite?.receiver?.properties!!["name"].toString() + " & " +
                inMeetingPost?.invite?.sender?.properties!!["name"].toString()
        restaurant_name.text = inMeetingPost?.resturantName.toString()
        //changeSeatStatus(Prefs.getString("seatId",""),this)

        //changeStatusThroughMap()


    }


    private fun setupListener() {
        btn_end_meeting.setOnClickListener {


            showChoiceDialogForMeetingandCancel("MeetingEnd", "Do you want to end the Meeting?")
        }


        btn_cancel_seat_inmeeting.setOnClickListener {
            showChoiceDialogForMeetingandCancel("MeetingCancel", "Do you want to cancel Seatcheck?")

        }
    }


    private fun setupAppbar() {
        appbar_back.setOnClickListener {
            val intent = Intent(this@ActivitySeatLiveInMeeting, NeedASeatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // call this to finish the current activity
        }

        appbar_img_bell.setOnClickListener {
            val intent = Intent(this@ActivitySeatLiveInMeeting, ActivityShowInvites::class.java)
            startActivity(intent)
            finish()
        }

        appbar_img_home.setOnClickListener {
            val intent = Intent(this@ActivitySeatLiveInMeeting, NeedASeatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        appbar_img.setOnClickListener {
            val intent = Intent(this@ActivitySeatLiveInMeeting, ProfileActivity::class.java)
            startActivity(intent)
        }

        Glide.with(this@ActivitySeatLiveInMeeting).load(R.drawable.nd_chat_message_icon).apply(options).into(appbar_img)
        appbar_img.setOnClickListener {

            val intent = Intent(this@ActivitySeatLiveInMeeting, ChatActivity::class.java)
            intent.putExtra("sender_id", Prefs.getString("userId", ""))
            intent.putExtra("receiver_id", inMeetingPost?.invite?.sender?.objectId)
            intent.putExtra("chat_receiver_id", Prefs.getInt("chat_user_id", -1))
            intent.putExtra("chat_receiver_id_str", inMeetingPost?.invite?.sender?.objectId)
            intent.putExtra("user_dp", inMeetingPost?.invite?.sender?.properties!!["profileImage"].toString())
            intent.putExtra("fcmToken", inMeetingPost?.invite?.sender?.properties!!["fcmToken"].toString())
            intent.putExtra("name", inMeetingPost?.invite?.sender?.properties!!["name"].toString())
            startActivity(intent)
        }
    }


    private fun changeStatusThroughMap() {

//
//        Backendless.Persistence.of("Post").findById(Prefs.getString("seatId",""), object : AsyncCallback<Map<*, *>> {
//            override fun handleFault(fault: BackendlessFault?) {
//
//                Toast.makeText(this@ActivitySeatLiveInMeeting,fault?.message,Toast.LENGTH_LONG).show()
//
//            }
//
//            override fun handleResponse(response: Map<*, *>?) {
//
//
//                Toast.makeText(this@ActivitySeatLiveInMeeting,"Post find successfully",Toast.LENGTH_LONG).show()
//
//
//            }
//
//        })


//        Backendless.Persistence.of("Post").save(post, object : AsyncCallback<Map<*, *>> {
//            override fun handleResponse(response: Map<*, *>) {
//
//
//                pd.dismiss()
//                Toast.makeText(this@PostSeatActivity,"Seat Posted Successfully",Toast.LENGTH_LONG).show()
//
//                Prefs.remove("name")
//                Prefs.putBoolean("isLive",true)
//                Prefs.putString("seatId",response["objectId"].toString())
//                Prefs.putInt("userStatus",1)
//                val intent = Intent(this@PostSeatActivity, NeedASeatActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//                finish()
//
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

    private fun cancelSeatCheck() {


        val pd = Utils.SCProgressDialog(this@ActivitySeatLiveInMeeting, null, "Cancelling SeatCheck")
        pd.show()


//        var _post : Post? = null
//
//
//        for(i in AppClass.postList!!.indices) {
//
//            if(AppClass.postList!![i].objectId.equals(post_id)) {
//
//                _post = AppClass.postList!![i]
//            }
//        }

        inMeetingPost?.shouldGoLive = "yes"
        inMeetingPost?.extendedTime = Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()
        inMeetingPost?.createdTwo = Utils.getCreatedTwoTimeTempForSolvingiOSIssue(inMeetingPost?.time?.toInt()?.div(60)!!)
        inMeetingPost?.invite?.status = "cancelled"
        inMeetingPost?.record?.meetingStatus = "cancelled"


        Backendless.Persistence.save(inMeetingPost, object : AsyncCallback<Post> {
            override fun handleFault(fault: BackendlessFault?) {

                Utils.dismissProgressDialog(pd)

            }

            override fun handleResponse(response: Post?) {


                Utils.dismissProgressDialog(pd)
                Utils.sentNotification(this@ActivitySeatLiveInMeeting, "Meeting cancelled", inMeetingPost?.invite?.receiver?.properties!!["name"].toString() + " cancelled the meeting.", inMeetingPost?.invite?.sender?.properties!!["fcmToken"].toString(), "nothing")
                Prefs.putBoolean("inMeeting", false)
                Prefs.putInt("userStatus", 1)
                //Prefs.remove("seatId")
                //HawkUtils.deleteHawk()
                Utils.cancelAlarm(this@ActivitySeatLiveInMeeting)


                val intent = Intent(this@ActivitySeatLiveInMeeting, NeedASeatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()


            }

        })


        /*------------------------------------*/
//            Backendless.Persistence.of(Post::class.java).findById(post_id,object : AsyncCallback<Post> {
//
//                override fun handleFault(fault: BackendlessFault?) {
//                    pd.dismiss()
//
//                }
//
//                override fun handleResponse(response: Post?) {
//                    Thread.sleep(4000)
//
//                    response?.shouldGoLive = "yes"
//                    response?.invite?.status = "cancelled"
//
//
//                    Backendless.Persistence.of(Post::class.java).save(response,object : AsyncCallback<Post> {
//                        override fun handleFault(fault: BackendlessFault?) {
//
//                            pd.dismiss()
//
//                        }
//
//                        override fun handleResponse(response: Post?) {
//
//                            pd.dismiss()
//                            Utils.sentNotification(this@ActivitySeatLiveInMeeting,"SeatCheck Cancel",invites?.receiver?.properties!!["name"].toString() + " cancelled the meeting.",invites?.sender?.properties!!["fcmToken"].toString())
//                            Prefs.putBoolean("inMeeting",false)
//                            Prefs.putInt("userStatus",0)
//                            Prefs.remove("seatId")
//                            HawkUtils.deleteHawk()
//
//                            val intent = Intent(this@ActivitySeatLiveInMeeting, MainActivity::class.java)
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                            startActivity(intent)
//                            finish()
//                        }
//
//                    })
//                }
//
//
//            })


    }

    private fun changeStatusToEndMeeting() {

        val pd = Utils.SCProgressDialog(this@ActivitySeatLiveInMeeting, null, "Ending SeatCheck")
        pd.show()

        // val _invite = HawkUtils.getHawk("invites") as Invites

        inMeetingPost?.shouldGoLive = "no"
        inMeetingPost?.invite?.status = "ended"
        inMeetingPost?.record?.meetingStatus = "ended"


        Backendless.Data.save(inMeetingPost, object : AsyncCallback<Post> {
            override fun handleFault(fault: BackendlessFault?) {

                Utils.dismissProgressDialog(pd)
            }

            override fun handleResponse(response: Post?) {


                Utils.dismissProgressDialog(pd)
                Utils.sentNotification(this@ActivitySeatLiveInMeeting, "Meeting ended", inMeetingPost?.invite?.receiver?.properties!!["name"].toString() + " ended the meeting. Do you want rate other user?", inMeetingPost?.invite?.sender?.properties!!["fcmToken"].toString(), "OPEN_ACTIVITY_2")
//                Prefs.putBoolean("inMeeting",false)
//                Prefs.putInt("userStatus",0)
//                Prefs.remove("seatId")
//                Prefs.remove("isLive")
                Utils.cancelAlarm(this@ActivitySeatLiveInMeeting)
                val intent = Intent(this@ActivitySeatLiveInMeeting, ActivityEndMeeting::class.java)
                intent.putExtra("user", "owner")
                startActivity(intent)
                finish()


            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this@ActivitySeatLiveInMeeting, NeedASeatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish() // call this to finish the current activity
    }


    private fun showChoiceDialogForMeetingandCancel(type: String, message: String) {
        val builder = AlertDialog.Builder(this@ActivitySeatLiveInMeeting)
        builder.setMessage(message)
                .setTitle("Confirm!")

        builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                p0?.dismiss()
                if (type.equals("MeetingEnd")) {
                    changeStatusToEndMeeting()
                } else if (type.equals("MeetingCancel")) {
                    cancelSeatCheck()
                }

            }

        }).setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {

                p0?.dismiss()
            }

        })

        val dialog = builder.create()
        dialog.show()
    }

}
