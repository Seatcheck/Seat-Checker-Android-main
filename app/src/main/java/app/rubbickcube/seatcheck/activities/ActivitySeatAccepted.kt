package app.rubbickcube.seatcheck.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_seat_accepted.*
import kotlinx.android.synthetic.main.app_bar_layout_for_need_seat.*


class ActivitySeatAccepted : AppCompatActivity() {


    var backendlessUser: BackendlessUser = BackendlessUser()
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
        setContentView(R.layout.activity_seat_accepted)
        supportActionBar?.hide()
        // AppClass.appComponent?.inject(this)
        Hawk.init(this).build()

        setupAppBar()

        if (Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        } else {
            return
        }




        if (HawkUtils.getHawk("inMeetingPost") != null) {
            inMeetingPost = HawkUtils.getHawk("inMeetingPost") as Post
        } else {
            Toast.makeText(this@ActivitySeatAccepted, "Something went wrong...", Toast.LENGTH_SHORT).show()
            return
        }




        Prefs.putBoolean("inMeeting", true)
        Prefs.putInt("userStatus", 2)
        if (HawkUtils.getHawk("invites") != null) {
            invites = HawkUtils.getHawk("invites") as Invites
        } else {
            Toast.makeText(this@ActivitySeatAccepted, "Something went wrong", Toast.LENGTH_SHORT).show()
        }

        //invites = AppClass.invites

        Glide.with(this).load(inMeetingPost?.invite?.receiver?.properties!!["profileImage"]).apply(options).into(me)
        Glide.with(this).load(inMeetingPost?.invite?.sender?.properties!!["profileImage"]).apply(options).into(him)

        txt_me.text = inMeetingPost?.invite?.receiver?.properties!!["name"].toString()
        txt_his.text = inMeetingPost?.invite?.sender?.properties!!["name"].toString()

        member_name.text = inMeetingPost?.invite?.receiver?.properties!!["name"].toString() + " & " + inMeetingPost?.invite?.sender?.properties!!["name"].toString() + " are sharing \n a table now!"
        restaurant_name.text = inMeetingPost?.resturantName.toString()

        btn_end_meeting.setOnClickListener {
            showChoiceDialogForMeetingandCancel("MeetingEnd", "Do you want to end the Meeting?")
        }


        btn_cancel_seatcheck.setOnClickListener {
            showChoiceDialogForMeetingandCancel("MeetingCancel", "Do you want to cancel the Meeting?")


        }
    }

    private fun cancelSeatCheck() {


        val pd = Utils.SCProgressDialog(this@ActivitySeatAccepted, null, "Cancelling SeatCheck")
        pd.show()


//        var _post : Post? = null
//
//
//        for(i in AppClass.postList!!.indices) {
//
//            if(AppClass.postList!![i].objectId.equals(Prefs.getString("seatId",""))) {
//
//                _post = AppClass.postList!![i]
//            }
//        }

        inMeetingPost?.shouldGoLive = "yes"
        inMeetingPost?.extendedTime = Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()
        inMeetingPost?.createdTwo = Utils.getCreatedTwoTimeTempForSolvingiOSIssue(inMeetingPost?.time?.toInt()?.div(60)!!)
        inMeetingPost?.invite?.status = "cancelled"
        inMeetingPost?.record?.meetingStatus = "cancelled"



        Backendless.Data.save(inMeetingPost, object : AsyncCallback<Post> {

            override fun handleFault(fault: BackendlessFault?) {
                Utils.dismissProgressDialog(pd!!)
            }

            override fun handleResponse(response: Post?) {

                Utils.dismissProgressDialog(pd!!)
                Utils.sentNotification(this@ActivitySeatAccepted, "Meeting cancelled", inMeetingPost?.invite?.sender?.properties!!["name"].toString() + " cancelled the meeting.", inMeetingPost?.invite?.receiver?.properties!!["fcmToken"].toString(), "nothing")
                Prefs.putBoolean("inMeeting", false)
                Prefs.putInt("userStatus", 0)
                Utils.cancelAlarm(this@ActivitySeatAccepted)
                //Prefs.remove("seatId")
                // HawkUtils.deleteHawk()


                val intent = Intent(this@ActivitySeatAccepted, NeedASeatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()


            }

        })

//        Backendless.Persistence.of(Post::class.java).findById(Prefs.getString("seatId",""),object : AsyncCallback<Post> {
//
//            override fun handleFault(fault: BackendlessFault?) {
//                pd.dismiss()
//
//            }
//
//            override fun handleResponse(response: Post?) {
//                Thread.sleep(4000)
//
//                response?.shouldGoLive = "yes"
//                response?.invite?.status = "cancelled"
//
//
//                Backendless.Persistence.of(Post::class.java).save(response,object : AsyncCallback<Post> {
//                    override fun handleFault(fault: BackendlessFault?) {
//
//                        pd.dismiss()
//
//                    }
//
//                    override fun handleResponse(response: Post?) {
//
//                        pd.dismiss()
//                        Utils.sentNotification(this@ActivitySeatAccepted,"SeatCheck Cancel",invites?.sender?.properties!!["name"].toString() + " cancelled the meeting.",invites?.receiver?.properties!!["fcmToken"].toString())
//                        Prefs.putBoolean("inMeeting",false)
//                        Prefs.putInt("userStatus",0)
//                        Prefs.remove("seatId")
//                        HawkUtils.deleteHawk()
//                        val intent = Intent(this@ActivitySeatAccepted, NeedASeatActivity::class.java)
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                        startActivity(intent)
//                        finish()
//                    }
//
//                })
//            }
//
//
//        })


    }

    private fun changeStatusToEndMeeting() {


        val pd = Utils.SCProgressDialog(this@ActivitySeatAccepted, null, "Ending SeatCheck")
        pd.show()


        inMeetingPost?.shouldGoLive = "no"
        inMeetingPost?.invite?.status = "ended"
        inMeetingPost?.record?.meetingStatus = "ended"

        //invites?.status = "ended"
        Backendless.Data.save(inMeetingPost, object : AsyncCallback<Post> {
            override fun handleFault(fault: BackendlessFault?) {
                Utils.dismissProgressDialog(pd!!)
            }

            override fun handleResponse(response: Post?) {
                Utils.dismissProgressDialog(pd!!)
                Utils.sentNotification(this@ActivitySeatAccepted, "Meeting ended", inMeetingPost?.invite?.sender?.properties!!["name"].toString() + " " + "ended the meeting. Do you want rate other user?", inMeetingPost?.invite?.receiver?.properties!!["fcmToken"].toString(), "OPEN_ACTIVITY_2")
                Prefs.putBoolean("inMeeting", false)
                Prefs.putInt("userStatus", 0)
                Utils.cancelAlarm(this@ActivitySeatAccepted)
                val intent = Intent(this@ActivitySeatAccepted, ActivityEndMeeting::class.java)
                intent.putExtra("user", "notOwner")
                startActivity(intent)
                finish()

//                Utils.sentNotification(this@ActivitySeatAccepted,"SeatCheck Meeting End",invites?.sender?.properties!!["name"].toString() + " ended the meeting.",invites?.receiver?.properties!!["fcmToken"].toString())
//                Prefs.putBoolean("inMeeting",false)
//                Prefs.putInt("userStatus",0)
//                Prefs.remove("seatId")
//                val intent = Intent(this@ActivitySeatAccepted, ActivityEndMeeting::class.java)
//                intent.putExtra("user","notOwner")
//                startActivity(intent)
//                finish()


            }

        })

        /*--------------------------------------*/

//        Backendless.Persistence.of(Post::class.java).findById(Prefs.getString("seatId",""),object : AsyncCallback<Post> {
//
//            override fun handleFault(fault: BackendlessFault?) {
//                pd.dismiss()
//
//            }
//
//            override fun handleResponse(response: Post?) {
//                Thread.sleep(4000)
//
//                response?.invite?.status = "ended"
//
//
//                Backendless.Persistence.of(Post::class.java).save(response,object : AsyncCallback<Post> {
//                    override fun handleFault(fault: BackendlessFault?) {
//
//                        pd.dismiss()
//
//                    }
//
//                    override fun handleResponse(response: Post?) {
//
//                        pd.dismiss()
//                        Utils.sentNotification(this@ActivitySeatAccepted,"SeatCheck Meeting End",invites?.sender?.properties!!["name"].toString() + " ended the meeting.",invites?.receiver?.properties!!["fcmToken"].toString())
//                        Prefs.putBoolean("inMeeting",false)
//                        Prefs.putInt("userStatus",0)
//                        Prefs.remove("seatId")
//                        val intent = Intent(this@ActivitySeatAccepted, ActivityEndMeeting::class.java)
//                        startActivity(intent)
//                        finish()
//                    }
//
//                })
//            }
//
//
//        })

    }

    fun setupAppBar() {

        appbar_title.setText("Seat Check")
        //appbar_img_bell_need_seat.visibility = View.INVISIBLE
        appbar_img_bell_need_seat.setOnClickListener {
            val intent = Intent(this@ActivitySeatAccepted, ActivityShowInvites::class.java)
            startActivity(intent)

            appbar_back_need_find.setOnClickListener {
                val intent = Intent(this@ActivitySeatAccepted, PeopleAroundMeActivitiy::class.java)
                startActivity(intent)
            }
            finish()
        }


        appbar_img_refresh_need_seat.setImageResource(R.drawable.home_ico)
        appbar_img_refresh_need_seat.setOnClickListener {

            val intent = Intent(this@ActivitySeatAccepted, NeedASeatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // call this to finish the current activity
        }

        Glide.with(this@ActivitySeatAccepted).load(backendlessUser.properties["profileImage"].toString()).apply(options).into(appbar_img_need_seat)
        appbar_img_need_seat.setOnClickListener {

            val intent = Intent(this@ActivitySeatAccepted, ProfileActivity::class.java)
            startActivity(intent)
        }

        appbar_back_need_seat.setOnClickListener {
            finish()
        }

        appbar_img_chat.setOnClickListener {

            val intent = Intent(this@ActivitySeatAccepted, ChatActivity::class.java)
            intent.putExtra("sender_id", Prefs.getString("userId", ""))
            intent.putExtra("receiver_id", inMeetingPost?.invite?.receiver?.objectId)
//            intent.putExtra("chat_receiver_id", Prefs.getInt("chat_user_id", -1))
            intent.putExtra("chat_receiver_id", Prefs.getInt("chat_user_id", -1))
            intent.putExtra("chat_receiver_id_str", inMeetingPost?.invite?.receiver?.objectId)
            intent.putExtra("chat_receiver_id", inMeetingPost?.invite?.receiver?.userId)
            intent.putExtra("user_dp", inMeetingPost?.invite?.receiver?.properties!!["profileImage"].toString())
            intent.putExtra("fcmToken", inMeetingPost?.invite?.receiver?.properties!!["fcmToken"].toString())
            intent.putExtra("name", inMeetingPost?.invite?.receiver?.properties!!["name"].toString())
            startActivity(intent)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this@ActivitySeatAccepted, NeedASeatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish() // call this to finish the current activity
    }

    private fun showChoiceDialogForMeetingandCancel(type: String, message: String) {
        val builder = AlertDialog.Builder(this@ActivitySeatAccepted)
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

    private fun callEndMeetingNew() {

        val pd = Utils.SCProgressDialog(this@ActivitySeatAccepted, null, "Ending SeatCheck")
        pd.show()

        val _invite = HawkUtils.getHawk("invites") as Invites
        _invite.status = "ended"


        Backendless.Data.save(_invite, object : AsyncCallback<Invites> {
            override fun handleFault(fault: BackendlessFault?) {

                Utils.dismissProgressDialog(pd!!)
            }

            override fun handleResponse(response: Invites?) {
                Utils.dismissProgressDialog(pd!!)
                Utils.sentNotification(this@ActivitySeatAccepted, "SeatCheck Meeting End", inMeetingPost?.invite?.sender?.properties!!["name"].toString() + " " + "ended the meeting.", invites?.receiver?.properties!!["fcmToken"].toString(), "OPEN_ACTIVITY_2")
                //Prefs.putBoolean("inMeeting",false)
                //Prefs.putInt("userStatus",0)
                Utils.cancelAlarm(this@ActivitySeatAccepted)


                val intent = Intent(this@ActivitySeatAccepted, ActivityEndMeeting::class.java)
                intent.putExtra("user", "notOwner")
                startActivity(intent)
                finish()


            }
        })
    }

}
