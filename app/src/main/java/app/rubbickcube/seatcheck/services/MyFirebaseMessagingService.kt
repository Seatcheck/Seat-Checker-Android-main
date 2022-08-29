package app.rubbickcube.seatcheck.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startActivity
import android.util.Log
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.MainActivity
import app.rubbickcube.seatcheck.ObservableObject
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.activities.NeedASeatActivity
import app.rubbickcube.seatcheck.activities.ShowAlertDialogActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var TAG = "MyFirebaseMessagingService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d(TAG, "FROM : " + remoteMessage.from)

        //Verify if the message contains data
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "MessageObject data : " + remoteMessage.data)
        }
        //Verify if the message contains notification
        if (remoteMessage.notification != null) {
            Log.d(TAG,"MessageObject body : "+ remoteMessage.notification!!.body)
            Log.d(TAG,"MessageObject body : "+remoteMessage.notification!!.body)
          //  sendNotification(remoteMessage.notification.body,remoteMessage.notification.title)


            //fireNotification(remoteMessage.notification.body)

            if(AppClass.appIsInForground) {
                if(remoteMessage.notification!!.body?.contains("requested to reserve your seat.")!!) {

                         AppClass.inviteCounter = 1
                         AppClass.message = remoteMessage.notification!!.body.toString()
                         AppClass.title = remoteMessage.notification!!.title.toString()
                         AppClass.notificationFor = "reservation"
                         ObservableObject.getInstance().updateValue("requested to reserve your seat.")
                         val intent =  Intent(this,ShowAlertDialogActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

//                         intent.action = "app.rubbickcube.seatcheck.SHOW_DIALOG"
//                         sendBroadcast(intent)

                }else if(remoteMessage.notification!!.title?.equals("Meeting cancelled")!!) {
                        AppClass.message = remoteMessage.notification!!.body.toString()
                        AppClass.title = remoteMessage.notification!!.title.toString()
                        AppClass.notificationFor = "cancelled"
                    val intent =  Intent(this,ShowAlertDialogActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

//                    intent.action = "app.rubbickcube.seatcheck.SHOW_DIALOG"
//                        sendBroadcast(intent)


                } else if(remoteMessage.notification!!.title?.equals("Meeting ended")!!) {
                        AppClass.message = remoteMessage.notification!!.body.toString() + " Would you like to rate other user"
                        AppClass.title = remoteMessage.notification!!.title.toString()
                        AppClass.notificationFor = "ended"

                    val intent =  Intent(this,ShowAlertDialogActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

//                    intent.action = "app.rubbickcube.seatcheck.SHOW_DIALOG"
//                    sendBroadcast(intent)


                }else if(remoteMessage.notification!!.title?.equals("Request Declined")!!) {
                        AppClass.message = remoteMessage.notification!!.body.toString()
                        AppClass.title = remoteMessage.notification!!.title.toString()
                        AppClass.notificationFor = "declined"

                    val intent =  Intent(this,ShowAlertDialogActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
//                        intent.action = "app.rubbickcube.seatcheck.SHOW_DIALOG"
//                        sendBroadcast(intent)


                }else if(remoteMessage.notification!!.title?.equals("Request Accepted")!!) {
                    ObservableObject.getInstance().updateValue("Request Accepted")
                    fireNotification(remoteMessage.notification!!.body!!)
                }else {
                    if(!AppClass.chatActive) {
                        fireNotification(remoteMessage.notification!!.body!!)
                    }                }
            }

            else {
                    fireNotification(remoteMessage.notification!!.body!!)

            }

        }
    }




    private fun fireNotification(body: String?) {
        var intent = Intent(this,NeedASeatActivity::class.java)
        //If set, and the activity being launched is already running in the current task,
        //then instead of launching a new instance of that activity, all of the other activities
        // on top of it will be closed and this Intent will be delivered to the (now on top)
        // old activity as a new Intent.

        var notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = 1
        val channelId = "channel-01"
        val channelName = "Channel Name"
        val importance = NotificationManager.IMPORTANCE_HIGH

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                    channelId, channelName, importance)

            assert(notificationManager != null)
            notificationManager.createNotificationChannel(mChannel)
        }

        intent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent?.putExtra("Notification",body)
        val r = Random()
        val low = 10
        val high = 100
        val result = r.nextInt(high - low) + low
        var pendingIntent = PendingIntent.getActivity(this,result,intent,PendingIntent.FLAG_ONE_SHOT/*Flag indicating that this PendingIntent can be used only once.*/)
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notificationBuilder = NotificationCompat.Builder(this,"Notification")
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("SeatCheck!")
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent)

        notificationManager.notify(0,notificationBuilder.build())
    }

}