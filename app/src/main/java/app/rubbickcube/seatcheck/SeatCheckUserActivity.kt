package app.rubbickcube.seatcheck

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.activities.*
import app.rubbickcube.seatcheck.adapters.InvitesAdapter
import app.rubbickcube.seatcheck.model.Invites
import app.rubbickcube.seatcheck.model.Post
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
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_seat_check_user.*
import kotlinx.android.synthetic.main.app_bar_layout_for_activity_seat.*
import kotlinx.android.synthetic.main.chose_seat_dialog.view.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SeatCheckUserActivity : AppCompatActivity() {

    private var createdTwo : Date? = null
    private var post_id : String? = null
    private var phone : String? = null
    private var created : Date? = null
    private var timer : CountDownTimer? = null
    private var seat : Int? = 0
    private var availableSeats : Int? = null
    private var inviteId : String? = null


    var backendlessUser : BackendlessUser = BackendlessUser()


    val options =  RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
    var selectedPost : Post? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_check_user)
        supportActionBar?.hide()
        //AppClass.appComponent?.inject(this)
        Glide.with(this).load(R.drawable.bottom_curve).into(img_bottom_curve)

        if(Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        }else {
            return
        }

        setupComponents()
        setupListener()
        setupAppbar()

    }

    private fun setupComponents() {
        Backendless.initApp(this@SeatCheckUserActivity,resources.getString(R.string.appId), resources.getString(R.string.secretKey),"v1")


        selectedPost = AppClass.selectedPost

        seatcheck_other_user_name.text = selectedPost?.user?.properties!!["name"].toString()
        seatcheck_location_name_user.text = selectedPost?.resturantName
        seatcheck_location_desc_user.text = selectedPost?.resturantAddress
        location_phone_user.text = selectedPost?.resturantPhone
        createdTwo = selectedPost?.createdTwo
        availableSeats = selectedPost?.quantity
        post_id = selectedPost?.objectId
        created = selectedPost?.created
        location_seat_user.text = availableSeats.toString()
        Glide.with(this@SeatCheckUserActivity).load(selectedPost?.user?.properties!!["profileImage"]).apply(options).into(seatcheck_live_user_dp)


        if(!Utils.compareTwoDates(createdTwo, Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()) ) {
            val seatExpireTask = SeatExpireTask(this@SeatCheckUserActivity)
            seatExpireTask.execute()
        }




        val millis =  Utils.getTimeDiff(createdTwo!!,Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse())
        findInvitesUserSQL(millis)

        //current_time.setText("now")

        //location_time.setReferenceTime(Utils.getTimeinLong(created!!))


//        val secs = (TimeUnit.MILLISECONDS.toSeconds(millis) % 60)
//        val mins =(TimeUnit.MILLISECONDS.toMinutes(millis) % 60)

    }


    private fun removeSeat(seatId : String?, message : String? ){
        val pd = Utils.SCProgressDialog(this@SeatCheckUserActivity,null,message!!)
        pd.show()

        Backendless.Data.of(Post::class.java).findById(seatId, object : AsyncCallback<Post> {
            override fun handleFault(fault: BackendlessFault?) {
//                Log.d("TAG",fault?.message)
//                Toast.makeText(this@SeatCheckUserActivity,"Something went wrong...", Toast.LENGTH_LONG).show()
                Utils.dismissProgressDialog(pd)
                if(fault?.message?.contains("Unable to resolve host “api.backendless.com”: No address associated with hostname")!!) {

                    Utils.showAlertDialogWithFinish(this@SeatCheckUserActivity,"Connectivity Error","It seems internet connection is down. Please connect your phone with network and try again")

                }else {
                    Toast.makeText(this@SeatCheckUserActivity,"Something went wrong..",Toast.LENGTH_LONG).show()

                }
            }
            override fun handleResponse(response: Post?) {
                Backendless.Data.of(Post::class.java).remove(response, object  : AsyncCallback<Long> {
                    override fun handleResponse(response: Long?) {
                        startActivity(Intent(this@SeatCheckUserActivity, NeedASeatActivity::class.java))
                        // Toast.makeText(this@SeatCheckUserActivity,"Seat successfully removed.", Toast.LENGTH_LONG).show()
                        Prefs.remove("isLive")
                        finish()
                        Utils.dismissProgressDialog(pd)
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        Toast.makeText(this@SeatCheckUserActivity,"Something went wrong...", Toast.LENGTH_LONG).show()
                        Utils.dismissProgressDialog(pd)
                    }

                })
            }
        })
    }

    private fun setupListener() {



        seatcheck_other_user_dp.setOnClickListener {


            if (selectedPost?.user?.objectId.equals(backendlessUser.objectId)) {
                AppClass.reviewForOwner = true
                startActivity(Intent(this@SeatCheckUserActivity, ProfileActivity::class.java))


            } else {
                AppClass.reviewForOwner = false
                val intent = Intent(this@SeatCheckUserActivity, OtherUserProfileActivity::class.java)
                intent.putExtra("otherUserProfileImage", selectedPost?.getUser()?.getProperties()?.get("profileImage").toString())
                intent.putExtra("otherUserId", selectedPost?.getUser()?.getObjectId())
                intent.putExtra("otherUserName",selectedPost?.getUser()?.getProperties()?.get("name").toString())
                startActivity(intent)
            }
        }

        btn_prty_off.setOnClickListener {

            showSeatPostDialog()
        }


        btn_remove_seat_user.setOnClickListener {

            finish()
        }

        btn_take_seat_text.setOnClickListener {



            if (Prefs.getBoolean("inMeeting",false)) {
                Utils.showAlertDialog(this@SeatCheckUserActivity,"In Meeting","You can't sent invitation while you are in meeting")

            } else if(Prefs.getBoolean("isLive",false)) {
                Utils.showAlertDialog(this@SeatCheckUserActivity,"You are Live","You can't sent invitation while you are Live")
            } else {
                if(btn_take_seat_text.text.toString().equals("Invite Sent")) {
//                    val intent = Intent(this@SeatCheckUserActivity, InviteSentActivity::class.java)
//                    startActivity(intent)
//                    finish()
                }
                else if(btn_take_seat_text.text.toString().equals("Take a Seat")) {

                    if(Utils.isConnectedOnline(this)) {
                        showSeatPostDialog()

                    }else {
                        Utils.showAlertDialog(this,"Opps","Internet is not connected.")

                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }


    inner class SeatExpireTask(private val mContext: Context) : AsyncTask<Void, Void, Long>() {


        val pd = Utils.SCProgressDialog(mContext,null,"Seat is expired..")
        override fun onPreExecute() {
            super.onPreExecute()
            pd.show()

        }
        override fun onPostExecute(result: Long?) {
            Utils.dismissProgressDialog(pd)
            val intent = Intent(mContext, NeedASeatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // call this to finish the current activity   pd.dismiss()


        }

        override fun doInBackground(vararg p0: Void?): Long {

            for(i in 1..3) {
                Thread.sleep(1000)
            }

            return 0
        }



    }

    fun setupAppbar() {
        appbar_back.setOnClickListener {
            finish()
        }

        appbar_img_home.setOnClickListener {
            val intent = Intent(this@SeatCheckUserActivity, NeedASeatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        Glide.with(this@SeatCheckUserActivity).load(backendlessUser.properties["profileImage"]).apply(options).into(appbar_img)


        appbar_img.setOnClickListener {

            AppClass.reviewForOwner = true

            startActivity(Intent(this@SeatCheckUserActivity, ProfileActivity::class.java))

        }
    }



    private fun showSeatPostDialog() {
        val builder = AlertDialog.Builder(this@SeatCheckUserActivity)
        val inflater = layoutInflater

        val view = inflater.inflate(R.layout.chose_seat_dialog, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.rounded_corner)

        dialog.show()

        view.btn_done_seats.setOnClickListener {

            if(seat!! > availableSeats!!) {

                Utils.showAlertDialog(this@SeatCheckUserActivity,"Sorry!", "Only $availableSeats seats Available")
            }else if(seat == 0)  {
                Utils.showAlertDialog(this@SeatCheckUserActivity,"Sorry!", "Please select how many seats you need")

            }
            else {
                dialog.dismiss()
                btn_take_seat_text.text = "Party of $seat"
                inviteUser(this@SeatCheckUserActivity)


            }
        }

        view.btn_seat_1.setOnClickListener {

            seat = 1
            view.btn_seat_1.setTextColor(resources.getColor(android.R.color.white))
            view.btn_seat_1.setBackgroundResource(R.drawable.circular_drawable_selected)

            view.btn_seat_2.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_2.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_3.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_3.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_4.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_4.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_5.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_5.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_6.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_6.setBackgroundResource(R.drawable.circular_drawable)

        }

        view.btn_seat_2.setOnClickListener {
            seat = 2
            view.btn_seat_1.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_1.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_2.setTextColor(resources.getColor(android.R.color.white))
            view.btn_seat_2.setBackgroundResource(R.drawable.circular_drawable_selected)

            view.btn_seat_3.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_3.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_4.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_4.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_5.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_5.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_6.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_6.setBackgroundResource(R.drawable.circular_drawable)

        }


        view.btn_seat_3.setOnClickListener {
            seat = 3
            view.btn_seat_1.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_1.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_3.setTextColor(resources.getColor(android.R.color.white))
            view.btn_seat_3.setBackgroundResource(R.drawable.circular_drawable_selected)

            view.btn_seat_2.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_2.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_4.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_4.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_5.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_5.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_6.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_6.setBackgroundResource(R.drawable.circular_drawable)

        }


        view.btn_seat_4.setOnClickListener {
            seat = 4
            view.btn_seat_1.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_1.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_4.setTextColor(resources.getColor(android.R.color.white))
            view.btn_seat_4.setBackgroundResource(R.drawable.circular_drawable_selected)

            view.btn_seat_2.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_2.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_3.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_3.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_5.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_5.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_6.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_6.setBackgroundResource(R.drawable.circular_drawable)

        }


        view.btn_seat_5.setOnClickListener {
            seat = 5
            view.btn_seat_1.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_1.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_5.setTextColor(resources.getColor(android.R.color.white))
            view.btn_seat_5.setBackgroundResource(R.drawable.circular_drawable_selected)

            view.btn_seat_2.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_2.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_4.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_4.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_3.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_3.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_6.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_6.setBackgroundResource(R.drawable.circular_drawable)

        }

        view.btn_seat_6.setOnClickListener {
            seat = 6
            view.btn_seat_1.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_1.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_6.setTextColor(resources.getColor(android.R.color.white))
            view.btn_seat_6.setBackgroundResource(R.drawable.circular_drawable_selected)

            view.btn_seat_2.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_2.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_4.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_4.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_5.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_5.setBackgroundResource(R.drawable.circular_drawable)

            view.btn_seat_3.setTextColor(resources.getColor(R.color.colorPrimary))
            view.btn_seat_3.setBackgroundResource(R.drawable.circular_drawable)


        }

        // manipulationSeatsButton(dialog_layout)


    }



    private fun inviteUser(mContext: Context) {


//        val invites = HashMap<String,Any>()
//        invites["sender"] = Backendless.UserService.CurrentUser()
//        invites["receiver"] = AppClass.selectedPost?.user!!
//        invites["status"] = "pending"
        val pd = Utils.SCProgressDialog(mContext,null,"Inviting "+selectedPost?.user?.properties!!["name"]+ "...")
        pd.show()

        val invites = Invites()
        invites.status = "pending"
        invites.sender = backendlessUser
        invites.receiver =AppClass.selectedPost?.user!!


        Backendless.Persistence.save(invites, object : AsyncCallback<Invites> {
            override fun handleResponse(response: Invites) {


                val message = backendlessUser.properties["name"].toString() + " requested to reserve your seat. "+seat + " guest are coming to share your table with you."
                Utils.sentNotification(this@SeatCheckUserActivity,"Seatcheck Invitation",message,AppClass.selectedPost?.user?.properties!!["fcmToken"].toString(),"OPEN_ACTIVITY_1")
                Utils.dismissProgressDialog(pd)

                inviteId = response.objectId
                Prefs.putString("inviteStatus","sent")
                updateTotalRequest()


            }

            override fun handleFault(fault: BackendlessFault) {
                Utils.dismissProgressDialog(pd)
                Toast.makeText(mContext,fault.message,Toast.LENGTH_LONG).show()

            }
        })
    }


    private fun updateTotalRequest() {

        val post = AppClass.selectedPost
        post?.record?.totalRequests = post?.record?.totalRequests?.plus(1)!!

        Backendless.Persistence.save(post,object : AsyncCallback<Post> {
            override fun handleFault(fault: BackendlessFault?) {
                Log.d("error",fault?.message)

            }


            override fun handleResponse(response: Post?) {


                Log.d("error",response?.record?.totalRequests.toString())
                val intent = Intent(this@SeatCheckUserActivity, InviteSentActivity::class.java)
                intent.putExtra("inviteId",inviteId)
                intent.putExtra("seatId",post_id)
                startActivity(intent)
                finish()

            }

        })
    }




    private fun findInvitesUserSQL(millis : Long) {
        //val currentObjectId = Utils.quote(backendlessUser`.objectId)
        val queryStatus = Utils.quote("pending")
        val whereClause = "status = $queryStatus"

        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        var invitesList : MutableList<Invites>?


        Backendless.Persistence.of(Invites::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Invites>> {

                    override fun handleResponse(foundInvites: BackendlessCollection<Invites>) {


                        invitesList = arrayListOf()
                        for(i in foundInvites.data.indices) {

                            if(foundInvites.data[i].receiver != null) {
                                if(foundInvites.data[i]!!.sender != null) {
                                    invitesList?.add(foundInvites.data[i])
                                }
                            }


                            for(j in invitesList!!.indices) {

                                val invite = invitesList!![j]
                                val user = HawkUtils.getHawk("BackendlessUser") as BackendlessUser
                                if(invite.sender.objectId.equals(user.objectId) && invite.receiver.objectId.equals(selectedPost?.user?.objectId)) {

                                    btn_take_seat_text.text = "Invite Sent"
                                }else {
                                    btn_take_seat_text.text = "Take a Seat"

                                }
                            }


                        }

                        if(millis <= 0) {

                            val seatExpireTask = SeatExpireTask(this@SeatCheckUserActivity)
                            seatExpireTask.execute()
                        }else {
                            timer =  object : CountDownTimer(millis, 1000) {

                                override fun onTick(millisUntilFinished: Long) {


                                    when {
                                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("9") -> c_mins_user.text = "09"
                                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("8") -> c_mins_user.text = "08"
                                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("7") -> c_mins_user.text = "07"
                                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("6") -> c_mins_user.text = "06"
                                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("5") -> c_mins_user.text = "05"
                                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("4") -> c_mins_user.text = "04"
                                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("3") -> c_mins_user.text = "03"
                                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("2") -> c_mins_user.text = "02"
                                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("1") -> c_mins_user.text = "01"
                                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("0") -> c_mins_user.text = "00"
                                        else -> c_mins_user.text = (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString()
                                    }

                                    when
                                    {

                                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("9") -> c_secs_user.text = "09"
                                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("8") -> c_secs_user.text = "08"
                                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("7") -> c_secs_user.text = "07"
                                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("6") -> c_secs_user.text = "06"
                                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("5") -> c_secs_user.text = "05"
                                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("4") -> c_secs_user.text = "04"
                                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("3") -> c_secs_user.text = "03"
                                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("2") -> c_secs_user.text = "02"
                                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("1") -> c_secs_user.text = "01"
                                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("0") -> c_secs_user.text = "00"
                                        else -> c_secs_user.text = (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString()
                                    }
                                }

                                override fun onFinish() {

                                    removeSeat(post_id,"Seat is expired...")
                                    val seatExpireTask = SeatExpireTask(this@SeatCheckUserActivity)
                                    seatExpireTask.execute()
                                }

                            }.start()
                        }
                        //  Utils.dismissProgressDialog(pd)

                    }

                    override fun handleFault(fault: BackendlessFault) {

                        // Utils.dismissProgressDialog(pd)
                        Log.d("error","checkingInvites : " +fault.message)

                    }
                })




    }


}
