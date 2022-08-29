package app.rubbickcube.seatcheck.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.ObservableObject
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.model.Invites
import app.rubbickcube.seatcheck.model.Post
import com.aigestudio.wheelpicker.WheelPicker
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
import kotlinx.android.synthetic.main.activity_seat_live.*
import kotlinx.android.synthetic.main.app_bar_layout_for_activity_seat.*
import kotlinx.android.synthetic.main.extend_time_dialog.view.*
import kotlinx.android.synthetic.main.show_notification_alert_dialog.*
import java.util.*
import java.util.concurrent.TimeUnit


class ActivitySeatLive : AppCompatActivity() , Observer {



    var backendlessUser : BackendlessUser = BackendlessUser()
    var pd : KProgressHUD? = null
    val options =  RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar_rectangle)
            .error(R.drawable.avatar_rectangle)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)


    override fun update(p0: Observable?, p1: Any?) {
        if(p1!! is String) {
            if(p1!!.equals("requested to reserve your seat.")) {
                    runOnUiThread {
                        if(AppClass.inviteCounter > 0) {
                            invite_counter.visibility = View.VISIBLE
                            invite_counter.text = AppClass.inviteCounter.toString()
                        }else {
                            invite_counter.visibility = View.GONE
                        }
                    }
            }else if(p1.equals("requestForExtendTime")) {
                btn_extend_time_green.visibility = View.VISIBLE
            }
        }
    }

    private var selectedHour : Int? = 0
    private var selectedMins : Int? = 0
    private var createdTwo : Date? = null
    private var created : Date? = null
    private var extendedTime : Date? = null
    private var post_id : String? = null
    private var timer : CountDownTimer? = null
    var selectedPost : Post? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_live)
        Hawk.init(this).build()


        Glide.with(this).load(R.drawable.bottom_curve).into(img_bottom_curve)
        if(Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        }else {
            return
        }

//        Glide.with(this).load(R.drawable.nd_restaurant_location).apply(options).into(img_restaurant_live)
        Glide.with(this).load(R.drawable.nd_time_duration).apply(options).into(img_now_seat)

        //AppClass.appComponent?.inject(this)
        supportActionBar?.hide()
        onNewIntent(intent)


        setupComponents()
        setupListener()
        setupAppbar()

    }

    private fun setupComponents() {
        Backendless.initApp(this@ActivitySeatLive, resources.getString(R.string.appId), resources.getString(R.string.secretKey), "v1")
        ObservableObject.getInstance().addObserver(this)

                    if(HawkUtils.getHawk("postedSeatLive") != null){
                        selectedPost = HawkUtils.getHawk("postedSeatLive") as Post
                    }else {
                        val seatExpireTask = SeatExpireTask(this@ActivitySeatLive)
                        seatExpireTask.execute()
                    }


                    seatcheck_name.text = selectedPost?.user?.properties!!["name"].toString()
                    seatcheck_meeting_status.text = selectedPost?.record!!.meetingStatus
                    seatcheck_interested.text = selectedPost?.record!!.interestOption
                    seatcheck_location_name.text = selectedPost?.resturantName
                    seatcheck_location_desc.text = selectedPost?.resturantAddress
                    createdTwo = selectedPost?.createdTwo
                    extendedTime = selectedPost?.extendedTime
                    post_id = selectedPost?.objectId
                    created = selectedPost?.created
       // Toast.makeText(this@ActivitySeatLive,created.toString()+"time",Toast.LENGTH_LONG).show()


        //current_time.setText(selectedPost?.created)
                    Glide.with(this@ActivitySeatLive).load(selectedPost?.user?.properties!!["profileImage"]).apply(options).into(seatcheck_live_user_dp)


        if(!Utils.compareTwoDates(createdTwo, Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()) ) {
            removeSeat(selectedPost!!, "Seat time is expired...")
            return
        }

            // val millis = Utils.calculateRemainTime(createdTwo!!)

        val millis =  Utils.getTimeDiff(createdTwo!!, Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse())
        current_time.setText(created?.let { getTimeAgo(it) })
       // getTimeAgo(created);
        val times =  TimeUnit.MILLISECONDS.toSeconds(millis)
//                val millisss = times?.minus(300)?.times(1000)?.toLong()
//                //val futureInMillis = SystemClock.elapsedRealtime() + millisss!!
//                Utils.setAlarm(1000,this@PostSeatActivity,response.objectId)

//
        if (times != null) {
            if(times < 300) {

                btn_extend_time_green.visibility = View.VISIBLE
            }else{
                btn_extend_time_green.visibility = View.GONE
            }
        }

        btn_extend_time_green.setOnClickListener {
            showExtendTimeDialog()
        }

//        val secs = (TimeUnit.MILLISECONDS.toSeconds(millis) % 60)
//        val mins =(TimeUnit.MILLISECONDS.toMinutes(millis) % 60)



        //val mins = Utils.calculateRemainTimeinMinute(createdTwo!!)

       // val millis = TimeUnit.MINUTES.toMillis(mins)
        // current_time.setReferenceTime(Utils.getTimeinLong(created!!))

//        current_time.setText("now")
//        if(mins < 5) {
//            current_time.setReferenceTime(Utils.getTimeinLong(created!!))
//        }else {
//            current_time.setText("now")
//        }


        if(millis <= 0) {
            Toast.makeText(this@ActivitySeatLive, "Your seat is expire", Toast.LENGTH_LONG).show()
            removeSeat(selectedPost!!, "Seat time is expired...")
            //val seatExpireTask = SeatExpireTask(this@ActivitySeatLive)
           // seatExpireTask.execute()
        }else {
           timer =  object : CountDownTimer(millis, 1000) {

                override fun onTick(millisUntilFinished: Long) {


                 val sec = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60


//                    if(sec < 300) {
//
//                        btn_extend_time_green.visibility = View.VISIBLE
//                    }

                    when {
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("9") -> c_mins.text = "09"
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("8") -> c_mins.text = "08"
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("7") -> c_mins.text = "07"
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("6") -> c_mins.text = "06"
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("5") -> c_mins.text = "05"
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("4") -> c_mins.text = "04"
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("3") -> c_mins.text = "03"
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("2") -> c_mins.text = "02"
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("1") -> c_mins.text = "01"
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString().equals("0") -> c_mins.text = "00"
                        else -> c_mins.text = (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString()
                    }

                    when
                    {

                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("9") -> c_secs.text = "09"
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("8") -> c_secs.text = "08"
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("7") -> c_secs.text = "07"
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("6") -> c_secs.text = "06"
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("5") -> c_secs.text = "05"
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("4") -> c_secs.text = "04"
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("3") -> c_secs.text = "03"
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("2") -> c_secs.text = "02"
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("1") -> c_secs.text = "01"
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString().equals("0") -> c_secs.text = "00"
                        else -> c_secs.text = (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString()
                    }




//                    val millisss = selectedPost?.time?.minus(300)?.times(1000)?.toLong()
//
//                    val millisCurrent =  Utils.getTimeDiff(createdTwo!!,Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse())
//
//                    if(millisCurrent!! <= millisss!!) {
//                        btn_extend_time.visibility = View.VISIBLE
//
//                    }else {
//                        btn_extend_time.visibility = View.INVISIBLE
//
//
//                    }


//                    val extendTime = Prefs.getLong("showExtendTimeButton",0)
//                    if(extendTime != (0).toLong()) {
//                        val updatedMillies =  Utils.getTimeDiff(createdTwo!!,Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse())
//                        if(updatedMillies < extendTime) {
//
//                            btn_extend_time.visibility = View.VISIBLE
//                        }else {
//                            btn_extend_time.visibility = View.GONE
//
//                        }
//
//                    }

                    //val mins = Utils.calculateRemainTimeinMinute(createdTwo!!)

                }

                override fun onFinish() {
                    timer?.cancel()
                    val seatExpireTask = SeatExpireTask(this@ActivitySeatLive)
                    seatExpireTask.execute()

                    //removeSeat(post_id,"Seat time is expired...")

                }

            }.start()
        }
    }

    private  val SECOND_MILLIS = 1000
    private  val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private  val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private  val DAY_MILLIS = 24 * HOUR_MILLIS

    private fun currentDate(): Date {
        val calendar = Calendar.getInstance()
        return calendar.time
    }

    fun getTimeAgo(date: Date): String {
        var time = date.time
        if (time < 1000000000000L) {
            time *= 1000
        }

        val now = currentDate().time
        if (time > now || time <= 0) {
            return "in the future"
        }

        val diff = now - time
        return when {
            diff < MINUTE_MILLIS -> "moments ago"
            diff < 2 * MINUTE_MILLIS -> "a minute ago"
            diff < 60 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS} minutes ago"
            diff < 2 * HOUR_MILLIS -> "an hour ago"
            diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS} hours ago"
            diff < 48 * HOUR_MILLIS -> "yesterday"
            else -> "${diff / DAY_MILLIS} days ago"
        }
    }

    private fun removeSeat(post: Post, message: String?) {

         pd = Utils.SCProgressDialog(this@ActivitySeatLive, null, message!!)
         pd?.show()
        Backendless.Persistence.of(Post::class.java).remove(post, object : AsyncCallback<Long> {
            override fun handleFault(fault: BackendlessFault?) {
                Log.d("TAG", fault?.message)
                Toast.makeText(this@ActivitySeatLive, "Something went wrong...", Toast.LENGTH_LONG).show()
                dismissProgressDialog()
            }

            override fun handleResponse(response: Long?) {

                dismissProgressDialog()

                Prefs.putBoolean("isLive", false)
                Prefs.putInt("userStatus", 0)
                Prefs.remove("requestForExtendTime")
                Utils.cancelAlarm(this@ActivitySeatLive)
                val intent = Intent(this@ActivitySeatLive, NeedASeatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                // findAndDeleteInvites(pd)


            }

        })
    }



//    private fun removeSeat(seatId : String?, message : String? ){
//        val pd = Utils.SCProgressDialog(this@ActivitySeatLive,null,message!!)
//        pd.show()
//
//        Backendless.Data.of(Post::class.java).findById(seatId, object : AsyncCallback<Post>{
//            override fun handleFault(fault: BackendlessFault?) {
//                Log.d("TAG",fault?.message)
//                Toast.makeText(this@ActivitySeatLive,"Something went wrong...",Toast.LENGTH_LONG).show()
//                pd.dismiss()
//
//            }
//            override fun handleResponse(response: Post?) {
//
//                Backendless.Data.of(Post::class.java).remove(response, object  : AsyncCallback<Long> {
//                    override fun handleResponse(response: Long?) {
//
//                        pd.dismiss()
//                        Prefs.putBoolean("isLive",false)
//                        Prefs.putInt("userStatus",0)
//                        val intent = Intent(this@ActivitySeatLive, NeedASeatActivity::class.java)
//                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                        startActivity(intent)
//                        finish() // call this to finish the current activity   pd.dismiss()
//                    }
//
//                    override fun handleFault(fault: BackendlessFault?) {
//                        Toast.makeText(this@ActivitySeatLive,"Something went wrong...",Toast.LENGTH_LONG).show()
//
//                    }
//
//                })
//            }
//        })
//    }

    private fun setupListener() {
        btn_remove_seat.setOnClickListener {


            showRemoveSeatDialog(selectedPost!!)
        }


        btn_cancel_seat.setOnClickListener {

            startActivity(Intent(this@ActivitySeatLive, NeedASeatActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        ObservableObject.getInstance().deleteObserver(this)
    }


    private fun setupAppbar() {

//        if(AppClass.inviteCounter > 0) {
//            invite_counter.visibility = View.VISIBLE
//            invite_counter.text = AppClass.inviteCounter.toString()
//        }else {
//            invite_counter.visibility = View.GONE
//        }
        appbar_back.setOnClickListener {


            if(AppClass.requestForExtend) {
                val intent = Intent(this, NeedASeatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }else {
                finish()
            }
        }

        appbar_img_bell.setOnClickListener {
            val intent = Intent(this@ActivitySeatLive, ActivityShowInvites::class.java)
            startActivity(intent)
            finish()
        }

        appbar_img_home.setOnClickListener {
            val intent = Intent(this@ActivitySeatLive, NeedASeatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        Glide.with(this@ActivitySeatLive).load(backendlessUser.properties["profileImage"]).apply(options).into(appbar_img)


    }



    private fun removeInvitation(invitesId: String?){


        Backendless.Data.of(Invites::class.java).findById(invitesId, object : AsyncCallback<Invites> {
            override fun handleFault(fault: BackendlessFault?) {
                Log.d("TAG", fault?.message)


            }

            override fun handleResponse(response: Invites?) {
                Backendless.Data.of(Invites::class.java).remove(response, object : AsyncCallback<Long> {
                    override fun handleResponse(response: Long?) {
                        Log.d("TAG", "Invites Delete")

                        // call this to finish the current activity   pd.dismiss()
                    }

                    override fun handleFault(fault: BackendlessFault?) {
                        Log.d("TAG", fault?.message)

                    }

                })
            }
        })
    }

    inner class SeatExpireTask(private val mContext: Context) : AsyncTask<Void, Void, Long>() {


        val pd = Utils.SCProgressDialog(mContext, null, "Seat is expired..")
        override fun onPreExecute() {
            super.onPreExecute()
            pd.show()

        }
        override fun onPostExecute(result: Long?) {
            dismissProgressDialog(pd!!)

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


    private fun showRemoveSeatDialog(post: Post) {
        val builder = AlertDialog.Builder(this@ActivitySeatLive)
        builder.setMessage("Do you want to remove your seat?")
                .setTitle("Confirm!")

        builder.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {

                p0?.dismiss()
                removeSeat(post, "Removing seat please wait..")
            }

        }).setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {

                p0?.dismiss()
            }

        })

        val dialog = builder.create()
        dialog.show()
    }

    override fun onResume() {
        super.onResume()

        if(AppClass.inviteCounter > 0) {
            invite_counter.visibility = View.VISIBLE
            invite_counter.text = AppClass.inviteCounter.toString()
        }else {
            invite_counter.visibility = View.GONE
        }
    }


    private fun showExtendTimeDialog() {

        val builder = AlertDialog.Builder(this@ActivitySeatLive)
        val inflater = layoutInflater

        val dialog_layout = inflater.inflate(R.layout.extend_time_dialog, null)
        builder.setView(dialog_layout)
        val dialog = builder.create()
        dialog.setCancelable(true)
        dialog.getWindow()!!.setBackgroundDrawableResource(R.drawable.rounded_corner)

        setupHourSpinner(dialog_layout)
        setupMinsSpinner(dialog_layout)
        dialog.show()


        dialog_layout.dialog_btn_done_change.setOnClickListener {

             if(selectedHour == 1 && selectedMins!! > 1) {

            Utils.showAlertDialog(this@ActivitySeatLive, "Sorry!", "The maximum time allowed is one hour.")

            }else if(selectedHour == 0 && selectedMins!! == 0) {

                Utils.showAlertDialog(this@ActivitySeatLive, "Sorry!", "Please select seat time.")

            }else {

                 dialog.dismiss()
                 updateSeatTime(selectedPost, "Extending seat time...")
                // Toast.makeText(this@ActivitySeatLive,"Welcome",Toast.LENGTH_SHORT).show()
            }
        }

        dialog_layout.dialog_btn_cancel_extend.setOnClickListener {

            dialog.dismiss()
        }


    }

    private fun setupHourSpinner(dialig_view: View) {



//        val listhoursss : MutableList<String>? = arrayListOf()
//        listhoursss?.add("0")
//        listhoursss?.add("1")
//        dialig_view.wheel_hoursss.setTextList(listhoursss)
//
//        dialig_view.wheel_hoursss.setOnItemSelectedListener(object : AbsWheelView.OnItemSelectedListener{
//            override fun onItemSelected(parentView: AbsWheelView?, position: Int) {
//
//                selectedHour = position
//
//                if(selectedHour == 1 && selectedMins!! > 0) {
//
//                    Handler().postDelayed({
//                        dialig_view.wheel_minsss.setSelectItem(0)
//
//                    },300)
//
//                }
//            }
//
//        })
//
//        dialig_view.wheel_hoursss.setSelectItem(1)
//        dialig_view.wheel_hoursss.setTheme(TextWheelPicker.Theme.white)
//



        val listHours : MutableList<Int>? = arrayListOf()
        listHours?.add(0)
        listHours?.add(1)
        dialig_view.wheel_hour.data = listHours

        dialig_view.wheel_hour.setOnItemSelectedListener(object : WheelPicker.OnItemSelectedListener {
            override fun onItemSelected(picker: WheelPicker?, data: Any?, position: Int) {

                selectedHour = listHours!![position]
            }

        })
    }

    private fun setupMinsSpinner(dialig_view: View) {





//        var minsListsss : MutableList<String>? = arrayListOf()
//        for(i in 0..59) {
//           minsListsss?.add(i.toString())
//        }
//
//        dialig_view.wheel_minsss.setOnItemSelectedListener(object : AbsWheelView.OnItemSelectedListener{
//            override fun onItemSelected(parentView: AbsWheelView?, position: Int) {
//
//                selectedMins = position
//                if(selectedHour == 1 && selectedMins!! > 0) {
//
//                    Handler().postDelayed({
//                        dialig_view.wheel_hoursss.setSelectItem(0)
//
//                    },300)
//                }
//            }
//
//        })
//
//        dialig_view.wheel_minsss.setTextList(minsListsss)
//        dialig_view.wheel_minsss.setSelectItem(1)
//
//        dialig_view.wheel_minsss.setTheme(TextWheelPicker.Theme.white)





        var minsList : MutableList<Int>? = arrayListOf()
        for(i in 0..59) {
            minsList?.add(i)
        }

        dialig_view.wheel_min.data = minsList
        dialig_view.wheel_min.setOnItemSelectedListener(object : WheelPicker.OnItemSelectedListener {
            override fun onItemSelected(picker: WheelPicker?, data: Any?, position: Int) {

                selectedMins = minsList!![position]

            }

        })
    }



    private fun updateSeatTime(post: Post?, message: String?){
         pd = Utils.SCProgressDialog(this@ActivitySeatLive, null, message!!)
        pd?.show()

        post?.time = selectedMins?.times(60)!!
        post?.extendedTime = Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse()
        post?.createdTwo = Utils.getCreatedTwoTimeTempForSolvingiOSIssue(selectedMins!!)
        //Prefs.putString("createdTwo",Utils.getCreatedTwoTime(selectedMins!!))

        Backendless.Data.of(Post::class.java).save(post, object : AsyncCallback<Post> {
            override fun handleResponse(response: Post?) {
                dismissProgressDialog()

                val times = response?.time?.toInt()
                if (times!! > 300) {
                    val millisss = times.minus(300).times(1000).toLong()
                    val futureInMillis = SystemClock.elapsedRealtime() + millisss
                    Utils.setAlarm(futureInMillis, this@ActivitySeatLive, "seatId")
                }
                btn_extend_time_green.visibility = View.GONE
                Prefs.remove("requestForExtendTime")


                val updatedMillies = Utils.getTimeDiff(response.createdTwo!!, Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse())


                if (updatedMillies <= 0) {
                    Toast.makeText(this@ActivitySeatLive, "Your seat is expire", Toast.LENGTH_LONG).show()
                    //removeSeat(post_id,"Seat time is expired...")
                    val seatExpireTask = SeatExpireTask(this@ActivitySeatLive)
                    seatExpireTask.execute()
                } else {
                    timer?.cancel()
                    timer = object : CountDownTimer(updatedMillies, 1000) {

                        override fun onTick(millisUntilFinished: Long) {

                            c_mins.text = (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60).toString()
                            c_secs.text = (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60).toString()


                        }

                        override fun onFinish() {
                            timer?.cancel()
                            val seatExpireTask = SeatExpireTask(this@ActivitySeatLive)
                            seatExpireTask.execute()

                            //removeSeat(post_id,"Seat time is expired...")

                        }

                    }.start()
                }


//                val intent = Intent(this@ActivitySeatLive, NeedASeatActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//                finish() // call this to finish the current activity   pd.dismiss()
            }

            override fun handleFault(fault: BackendlessFault?) {
                Toast.makeText(this@ActivitySeatLive, fault?.message, Toast.LENGTH_LONG).show()

                dismissProgressDialog()
            }

        })


        /**/

//        Backendless.Data.of(Post::class.java).findById(seatId, object : AsyncCallback<Post>{
//            override fun handleFault(fault: BackendlessFault?) {
//                Log.d("TAG",fault?.message)
//                Toast.makeText(this@ActivitySeatLive,"Extending seat time...",Toast.LENGTH_LONG).show()
//                pd.dismiss()
//
//            }
//            override fun handleResponse(response: Post?) {
//
//
//                /*post["User"] = Backendless.UserService.CurrentUser()
//        post["Quantity"] = seat!!
//        post["time"] = selectedMins?.times(60)!!
//        post["extendedTime"] = Utils.fetchCurrentTime()
//        Prefs.putString("extendedTime",Utils.fetchCurrentTime())
//        post["createdTwo"] = Utils.getCreatedTwoTime(selectedMins!!)
//        Prefs.putString("createdTwo",Utils.getCreatedTwoTime(selectedMins!!))
//        post["resturantName"] = Prefs.getString("name","")
//        post["resturantAddress"] = Prefs.getString("vicinity","")
//        post["resturantPhone"] = Prefs.getString("phone","")
//        post["resturantLocationLatitude"] = Prefs.getString("restaurantLat","")
//        post["resturantLocationLongitude"] = Prefs.getString("restaurantLng","")
//        post["shouldGoLive"] = postOnMap!!
//        post["showOnMap"] = showOnMap!!*/
//
//
//            }
//        })
    }


//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        val extras = intent?.getExtras()
//        val notificationMessage = extras?.getBoolean("comingFromExpireNotification", false)
//        AppClass.requestForExtend = notificationMessage!!
//
//    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(AppClass.requestForExtend) {
            val intent = Intent(this, NeedASeatActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }


    fun quote(s: String): String {
        return StringBuilder()
                .append('\'')
                .append(s)
                .append('\'')
                .toString()
    }

    private fun dismissProgressDialog(_pd: KProgressHUD) {
        if (_pd != null && _pd?.isShowing()!!) {
            _pd?.dismiss()
        }
    }

    private fun dismissProgressDialog() {
        if (pd != null && pd?.isShowing()!!) {
            pd?.dismiss()
        }
    }


}
