package app.rubbickcube.seatcheck.Helpers

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import top.wefor.circularanim.CircularAnim
import java.util.regex.Matcher
import java.util.regex.Pattern
import androidx.appcompat.app.AlertDialog
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import app.rubbickcube.seatcheck.model.NotificationData
import app.rubbickcube.seatcheck.model.NotificationRequestModel
import app.rubbickcube.seatcheck.services.AlarmBroadcastReceiver
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kaopiz.kprogresshud.KProgressHUD
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.AsyncHttpResponseHandler
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.entity.StringEntity
import okhttp3.MediaType
import okhttp3.RequestBody
import java.nio.charset.Charset
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit






class Utils {

    companion object {


        fun getSimpleTextBody(string: String): RequestBody {

            return RequestBody.create(MediaType.parse("text/plain"), string)

        }

        fun grantCurrentUser(userId: String, context : Context) {


            val pd = Utils.SCProgressDialog(context!!,null,"Please wait for a moment...")
            pd.show()
            Backendless.UserService.findById(userId, object : AsyncCallback<BackendlessUser> {
                override fun handleFault(fault: BackendlessFault?) {

                    pd.dismiss()

                }

                override fun handleResponse(response: BackendlessUser?) {

                    Backendless.UserService.setCurrentUser( response )
                    HawkUtils.putHawk("BackendlessUser",response)

                    pd.dismiss()
                    //Utils.startActivityWithAnimation(this@SplashActivity,MainActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
                }

            })

        }

         fun getTimeinLong(created : String) : Long {
            val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
            try {
                val mDate = sdf.parse(created)
                val timeInMilliseconds = mDate.time
                return timeInMilliseconds
                // println("Date in milli :: $timeInMilliseconds")
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return 0

        }




        fun getCreatedTwoTimeTempForSolvingiOSIssue(min: Int): Date {


            val cal = Calendar.getInstance() // creates calendar
            cal.time = Date() // sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, 0) // adds one hour
            cal.add(Calendar.MINUTE, min) // adds one hour
            Prefs.putInt("min",min)
            return cal.time

        }
        fun getCreatedTwoTime(min: Int): String {


            val sf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
            val cal = Calendar.getInstance() // creates calendar
            cal.time = Date() // sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, 0) // adds one hour
            cal.add(Calendar.MINUTE, min) // adds one hour
            Prefs.putInt("min",min)
            return sf.format(cal.time)

        }
        fun sentNotification(context: Context,title: String, details : String, to : String, clickAction : String) {

            val notificationRequestModel =  NotificationRequestModel()
            val  notificationData =  NotificationData()

            notificationData.setmBody(details)
            notificationData.setmBadge(1)
            notificationData.setmContent(1)
            notificationData.setmSound("default")
            notificationData.setmTitle(title)
            if(!clickAction.equals("nothing")) {
                notificationData.setmClickAction(clickAction)
            }
            notificationRequestModel.data = notificationData;
            notificationRequestModel.to = to
            notificationRequestModel.setmPrioriy("high")

            val gson =  Gson()
            val type = object : TypeToken<NotificationRequestModel>() {
            }.type

            val json = gson.toJson(notificationRequestModel, type);

            val client = AsyncHttpClient()
            val entity =  StringEntity(json)
            val data = entity.toString()
            client.addHeader("Content-Type","application/json")
            client.addHeader("Authorization","key=AIzaSyBNY5zRRaposivP94OO-LVcsimbOZmh-pY")

            client.post(context,"https://fcm.googleapis.com/fcm/send",entity,"application/json",object : AsyncHttpResponseHandler() {
                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?) {

                    val str = responseBody?.toString(Charset.defaultCharset())
                    Log.d("fcm",str)
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseBody: ByteArray?, error: Throwable?) {
                    // val str = responseBody?.toString(Charset.defaultCharset())
                    Log.d("fcm",error?.message)
                }

            })
        }


    fun startActivityWithAnimation(activity: Activity, cls: Class<*>, target: View, removeBackStack: Boolean?, color: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            CircularAnim.fullActivity(activity, target)
                    .colorOrImageRes(color)
                    .go {
                        val intent = Intent(activity, cls)
                        activity.startActivity(intent)
                        if (removeBackStack!!) {
                            activity.finish()
                        }
                    }
        } else {

            val intent = Intent(activity, cls)
            activity.startActivity(intent)
            if (removeBackStack!!) {
                activity.finish()
            }

        }
    }


        fun isValidEmail(email: String?): Boolean {

            if (email == null) {
                return false
            }

            val emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
            val matcher: Matcher?
            val pattern = Pattern.compile(emailPattern)

            matcher = pattern.matcher(email)

            return matcher?.matches() ?: false
        }

        fun SCProgressDialog(ctx: Context, _title: String?, _message: String/*, @DrawableRes @Nullable int icon*/): KProgressHUD {


            //        ProgressDialog pd = new ProgressDialog(ctx);
//        pd.setTitle(_title);
//        pd.setMessage(_message);
//        pd.setIndeterminate(false);
//        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        pd.setCancelable(false);
            return KProgressHUD.create(ctx)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel(_message)
                    .setCancellable(true)
                    .setAnimationSpeed(2)
                    .setDimAmount(0.5f)
                    .show()
        }

        fun showAlertDialog(c: Context, title: String, message: String) {
            AlertDialog.Builder(c).setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, { dialog, which -> dialog.dismiss() }).show()
        }
        fun showAlertDialogWithFinish(c: Context, title: String, message: String) {
            AlertDialog.Builder(c).setTitle(title).setMessage(message).setPositiveButton(android.R.string.ok, { dialog, which -> (c as Activity).finish() }).show()
        }

         fun isConnectedOnline(context: Context): Boolean {

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }


        fun CheckDates(startDate: String, endDate: String): Boolean {

            var b = false

            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val date1 = sdf.parse(startDate)
            val date2 = sdf.parse(endDate)

            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = date1
            cal2.time = date2

            if (cal1.after(cal2)) {
                println("Date1 is after Date2")
            }

            if (cal1.before(cal2)) {
                println("Date1 is before Date2")
            }

            if (cal1.equals(cal2)) {
                println("Date1 is equal Date2")
            }
//            val sdf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
//            val date1 = sdf.parse(endDate)
//            val date2 = sdf.parse(startDate)
//
////            println("date1 : " + sdf.format(date1))
////            println("date2 : " + sdf.format(date2))
//            var b = false
//            if (date1.compareTo(date2) > 0) {
//              b = false
//            } else if (date1.compareTo(date2) < 0) {
//                b = false
//            } else if (date1.compareTo(date2) === 0) {
//                b = false
//            } else {
//                println("How to get here?")
//            }

//            val dfDate = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
//
//            var b = false
//
//            try {
//                if (dfDate.parse(startDate).before(dfDate.parse(endDate))) {
//                    b = true  // If start date is before end date.
//                } else if (dfDate.parse(startDate).equals(dfDate.parse(endDate))) {
//                    b = true  // If two dates are equal.
//                } else {
//                    b = false // If start date is after the end date.
//                }
//            } catch (e: ParseException) {
//                e.printStackTrace()
//            }

            return b
        }

         fun getCuurentDateTime() : String {

            val timeInMillis = System.currentTimeMillis()
            val cal1 = Calendar.getInstance()
            cal1.timeInMillis = timeInMillis
            val dateFormat = SimpleDateFormat(
                    "MM/dd/yyyy hh:mm:ss")
             cal1.add(Calendar.HOUR,5)
            return  dateFormat.format(cal1.time)
        }

        fun fetchCurrentTimeWithGMT(): String {

            val dateFormatGmt = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
            dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
           return dateFormatGmt.format(Date())

//            val sf = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
//            val cal = Calendar.getInstance() // creates calendar
//            cal.time = Date() // sets calendar time/date
//            return sf.format(cal.time)

        }
        fun fetchCurrentTimeTempForSolvingiPhoneIsuse(): Date {

            val cal = Calendar.getInstance() // creates calendar
            cal.time = Date() // sets calendar time/date
            val date1 = cal.time
            return date1

        }
        fun fetchCurrentTime(): String {

                   val sf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
                    val cal = Calendar.getInstance() // creates calendar
                    cal.time = Date() // sets calendar time/date
                    return sf.format(cal.time)

        }

        fun getCurrentTimeFiveHours(): String {

            val sf = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
            val cal = Calendar.getInstance() //creates calendar
            cal.time = Date() //sets calendar time/date
            cal.add(Calendar.HOUR_OF_DAY, 5) // adds one hour
            return sf.format(cal.time)
        }

        private fun cvtToGmt(date: Date): Date {
            val tz = TimeZone.getDefault()
            var ret = Date(date.time - tz.rawOffset)

            // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
            if (tz.inDaylightTime(ret)) {
                val dstDate = Date(ret.time - tz.dstSavings)

                // check to make sure we have not crossed back into standard time
                // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
                if (tz.inDaylightTime(dstDate)) {
                    ret = dstDate
                }
            }
            return ret
        }


         fun isTimeExpire(dateDevice : String?, dateServer : String?) :Boolean {

            try {

                // You can use any format to compare by spliting the dateTime
                val formatter = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")

                val str1 =dateServer
                val selectedDate = formatter.parse(str1)

                val str2 = dateDevice
                val currentDate = formatter.parse(str2)

                if (selectedDate < currentDate) {
                    return true
                } else {
                    return false
                }

            } catch (e1: ParseException) {
                e1.printStackTrace()
            }

            return false

        }

         fun checktimings(endtime: String): Boolean {
             val c = Calendar.getInstance()
             val sdf = SimpleDateFormat("HH:mm")
             val getCurrentTime = sdf.format(c.time)
             return getCurrentTime.compareTo(endtime) < 0
        }


        fun compareTwoDates(date1: Date?, date2: Date?): Boolean {
            if (date1 != null && date2 != null) {
                val retVal = date1.compareTo(date2)

                if (retVal > 0)
                    return true // date1 is greatet than date2
                else if (retVal == 0)
                // both dates r equal
                    return false

            }
            return false // date1 is less than date2
        }

        fun compareTime(endTime: String, startTime: String): Boolean {

            try {
                val format = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
                val date1 = format.parse(endTime)
                val date2 = format.parse(startTime)
                if (date1.compareTo(date2) <= 0) {
                    return false
                }else {
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return true
        }
        fun calculateRemainTime(scheduled_date: String) : Long {

            var diffence_in_minute: Long? =null
            val format = SimpleDateFormat("HH:mm:ss")
            // two dates
            val scheduledDate: java.util.Date
            val current = Calendar.getInstance()
            val currentDate: java.util.Date
            val current_date = format.format(current.time)
            try {
                scheduledDate = format.parse(scheduled_date)
                currentDate = format.parse(current_date)
                val diffInMillies = scheduledDate.time - currentDate.time
                diffence_in_minute = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS)
                return diffInMillies
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return diffence_in_minute!!
        }

        fun calculateRemainTimeinMinute(scheduled_date: Date) : Long {

            var diffence_in_minute: Long? =null
            val format = SimpleDateFormat("HH:mm:ss")
            // two dates
            val scheduledDate: java.util.Date
            val current = Calendar.getInstance()
            val currentDate: java.util.Date
            val current_date = format.format(current.time)
            try {
                scheduledDate = scheduled_date
                currentDate = format.parse(current_date)
                val diffInMillies = scheduledDate.time - currentDate.time
                diffence_in_minute = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS)
               // return diffInMillies
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            return diffence_in_minute!!
        }

         fun converTimetoGMT(createdTwo : String) : String {


            val date = Date(createdTwo)
            val gmtFormat = SimpleDateFormat("MM/dd/yyyy hh:mm:ss")
            val gmtTime = TimeZone.getTimeZone("GMT")
            gmtFormat.timeZone = gmtTime

            return gmtFormat.format(date)
        }

        fun getCurrentDateTimeofDevice() : String {
            val df = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
            //df.setTimeZone(TimeZone.getTimeZone("GMT"));
            val cal = Calendar.getInstance() // creates calendar
            cal.time = Date() // sets calendar time/date

            // cal.add(Calendar.HOUR_OF_DAY, 5); // adds one hour
            return  df.format(cal.time) // returns new date object, one hour in the future
        }


         fun setAlarm(millis : Long,context: Context,postId : String) {

            var notificationId = 0
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    millis,
                    PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(context, AlarmBroadcastReceiver::class.java).apply {
                                putExtra("notificationId", ++notificationId)
                                putExtra("seatId",postId)
                                putExtra("reminder", "Time for your seat is about to expire. Check your seat details to extend time before it expires.")
                            },
                            PendingIntent.FLAG_CANCEL_CURRENT
                    )
            )

        }

         fun cancelAlarm(context: Context) {
             val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(
                    PendingIntent.getBroadcast(
                            context, 0, Intent(context, AlarmBroadcastReceiver::class.java), 0))

        }

        fun convertStringtoDate(createdTwo : String) : Date {

            val format = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.ENGLISH)
            val date = format.parse(createdTwo)
            return date
        }

        fun getTimeDiff(date1 : Date,date2 : Date) : Long{

            var diff = "";
            val timeDiff = Math.abs(date1.getTime() - date2.getTime());
          // val  remainingMillis = TimeUnit.MINUTES.toMillis(timeDiff)
//            diff = String.format("%d hour(s) %d min(s)", TimeUnit.MILLISECONDS.toHours(timeDiff),
//                    TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));
            return timeDiff
        }

        private fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            return if (connectivityManager is ConnectivityManager) {
                val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
                networkInfo?.isConnected ?: false
            } else false
        }

        fun getBackendLessOtherUser(context: Context) : BackendlessUser {
            Hawk.init(context).build()
            val user = HawkUtils.getHawk("BackendlessOtherUser") as BackendlessUser
            return user
        }

        fun getBackendLessUser(context: Context) : BackendlessUser {
            Hawk.init(context).build()
            if(HawkUtils.getHawk("BackendlessUser") != null) {
                val user = HawkUtils.getHawk("BackendlessUser") as BackendlessUser
                return user
            }else{

                return null!!
            }


        }

        fun quote(s: String): String {
            return StringBuilder()
                    .append('\'')
                    .append(s)
                    .append('\'')
                    .toString()
        }

         fun dismissProgressDialog(_pd : KProgressHUD) {
            if (_pd != null && _pd?.isShowing()!!) {
                _pd?.dismiss()
            }
        }
    }





}