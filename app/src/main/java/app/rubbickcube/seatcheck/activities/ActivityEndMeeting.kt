package app.rubbickcube.seatcheck.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.rubbickcube.seatcheck.R
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.MainActivity
import app.rubbickcube.seatcheck.model.Invites
import app.rubbickcube.seatcheck.model.Post
import app.rubbickcube.seatcheck.model.Reviews
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.kaopiz.kprogresshud.KProgressHUD
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_end_meeting.*
import kotlinx.android.synthetic.main.app_bar_layout_for_need_seat.*
import javax.inject.Inject


class ActivityEndMeeting : AppCompatActivity() {


    var backendlessUser : BackendlessUser = BackendlessUser()
        private var nunumberOfStars : Int? = 0
        private var messageTest = ""
    private var user = ""
    var inMeetingPost : Post? = null
    var invite : Invites?= null
    val options =  RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_meeting)
        supportActionBar?.hide()
       // AppClass.appComponent?.inject(this)
        Hawk.init(this).build()

        if(Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        }else {
            return
        }
        intializeComponents()

        setupAppBar()

       //user = intent.getStringExtra("user")


        if(!Prefs.getBoolean("isLogin",false)) {
            Toast.makeText(this@ActivityEndMeeting,"Your session is expired..",Toast.LENGTH_SHORT).show()
            finish()
        }


        if(Prefs.getBoolean("isLive",true)) {
            user = "owner"
        }else {
            user = "notOwner"
        }



        Glide.with(this).load(backendlessUser.properties["profileImage"]).apply(options).into(me)


        if(user.equals("owner")) {
            end_meeting_name.text = "Tell us about your meeting with "+inMeetingPost?.invite?.sender?.properties!!["name"].toString() + "\n and rate your experience."
            Glide.with(this).load(inMeetingPost?.invite?.sender?.properties!!["profileImage"].toString()).apply(options).into(img_end_meeting_user)
        }else if(user.equals("notOwner")) {
            end_meeting_name.text = "Tell us about your meeting with "+inMeetingPost?.invite?.receiver?.properties!!["name"].toString() + "\n and rate your experience."
            Glide.with(this).load(inMeetingPost?.invite?.receiver?.properties!!["profileImage"].toString()).apply(options).into(img_end_meeting_user)

        }


    }


    fun intializeComponents() {


        if(HawkUtils.getHawk("inMeetingPost") != null) {
            inMeetingPost = HawkUtils.getHawk("inMeetingPost") as Post
        }else {
            Toast.makeText(this@ActivityEndMeeting,"Something went wrong...",Toast.LENGTH_SHORT).show()
        }

        btn_post_review.setOnClickListener {



            nunumberOfStars = ratingBar.rating.toInt()
            messageTest = test_message.text.toString().trim()

            if(nunumberOfStars!! <= 0) {
                Toast.makeText(this@ActivityEndMeeting,"Please rate your meeting",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if(messageTest.isNullOrEmpty()) {
                Toast.makeText(this@ActivityEndMeeting,"Please type your message",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else {
                //Toast.makeText(this@ActivityEndMeeting,"daz",Toast.LENGTH_SHORT).show()
                postReview()
            }

        }


    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this, NeedASeatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish() // call this to finish the current activity
    }

    private fun postReview() {


        val pd = Utils.SCProgressDialog(this@ActivityEndMeeting,null,"Posting Review")
        pd.show()

        val review = Reviews()
        review.toUser = inMeetingPost?.invite?.sender
        review.fromUser = inMeetingPost?.invite?.receiver
        review.numberOfStars = nunumberOfStars?.toDouble()
        review.messageTest = messageTest




//        if(Prefs.getBoolean("isLive",false)) {
//
//        }else {
//            review.toUser = invite?.receiver
//
//        }


        //review.created = Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()
        Log.d("error","EndMeetinga")

        Handler().postDelayed({
            Backendless.Persistence.of(Reviews::class.java).save(review,object : AsyncCallback<Reviews> {


                override fun handleResponse(response: Reviews?) {

                    Utils.dismissProgressDialog(pd!!)                    //HawkUtils.deleteHawk()
                    Utils.sentNotification(this@ActivityEndMeeting,"SeatCheck User Review",inMeetingPost?.invite?.receiver?.properties!!["name"].toString() +" posted "+nunumberOfStars +" star review",inMeetingPost?.invite?.sender?.properties!!["fcmToken"].toString(),"nothing")
                    val intent = Intent(this@ActivityEndMeeting, NeedASeatActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }

                override fun handleFault(fault: BackendlessFault?) {
//                    Utils.dismissProgressDialog(pd!!)                    //HawkUtils.deleteHawk()
//                    Toast.makeText(this@ActivityEndMeeting,fault?.message,Toast.LENGTH_SHORT).show()

                    postReviewAgain(pd,inMeetingPost!!)

                }

            })

        },1500)




    }


    private fun postReviewAgain(pd : KProgressHUD, post : Post) {


        val review = Reviews()
        review.toUser = post.invite.sender

//        if(Prefs.getBoolean("isLive",false)) {
//        }else {
//            review.toUser = invite?.receiver
//        }

        review.numberOfStars = nunumberOfStars?.toDouble()
        review.messageTest = messageTest
        review.fromUser = post?.invite.receiver
        //review.created = Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()
        Log.d("error","EndMeetingasfa")

        Backendless.Persistence.of(Reviews::class.java).save(review,object : AsyncCallback<Reviews> {


            override fun handleResponse(response: Reviews?) {
                Utils.dismissProgressDialog(pd!!)
                //HawkUtils.deleteHawk()
                Utils.sentNotification(this@ActivityEndMeeting,"SeatCheck User Review",inMeetingPost?.invite?.receiver?.properties!!["name"].toString() +" posted "+nunumberOfStars +" star review",inMeetingPost?.invite?.sender?.properties!!["fcmToken"].toString(),"nothing")

                val intent = Intent(this@ActivityEndMeeting, NeedASeatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

            override fun handleFault(fault: BackendlessFault?) {
                Utils.dismissProgressDialog(pd!!)
                Utils.sentNotification(this@ActivityEndMeeting,"SeatCheck User Review",inMeetingPost?.invite?.receiver?.properties!!["name"].toString() +" posted "+nunumberOfStars +" star review",inMeetingPost?.invite?.sender?.properties!!["fcmToken"].toString(),"nothing")

                val intent = Intent(this@ActivityEndMeeting, NeedASeatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()


            }

        })


    }

    private fun setupAppBar() {

        appbar_title.setText("SeatCheck")
        appbar_img_bell_need_seat.visibility = View.INVISIBLE
        appbar_img_refresh_need_seat.setImageResource(R.drawable.home_ico)
        appbar_img_refresh_need_seat.setOnClickListener {
            //HawkUtils.deleteHawk()
            val intent = Intent(this@ActivityEndMeeting, NeedASeatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        Glide.with(this@ActivityEndMeeting).load(backendlessUser.properties["profileImage"].toString()).apply(options).into(appbar_img_need_seat)
        appbar_img_need_seat.setOnClickListener {

            val intent = Intent(this@ActivityEndMeeting, ProfileActivity::class.java)
            startActivity(intent)
        }

        appbar_back_need_seat.setImageResource(R.drawable.back_arrow)
        appbar_back_need_seat.setOnClickListener {
            // HawkUtils.deleteHawk()
            val intent = Intent(this@ActivityEndMeeting, NeedASeatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

    }
}
