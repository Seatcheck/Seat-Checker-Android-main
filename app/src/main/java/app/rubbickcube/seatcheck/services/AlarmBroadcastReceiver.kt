package app.rubbickcube.seatcheck.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.ObservableObject
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.activities.ActivitySeatLive
import com.pixplicity.easyprefs.library.Prefs

class AlarmBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {



            Prefs.putBoolean("requestForExtendTime",true)
            ObservableObject.getInstance().updateValue("requestForExtendTime")
            (context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(
                    intent!!.getIntExtra("notificationId", 0),
                    Notification.Builder(context).apply {
                        setSmallIcon(R.drawable.app_icon)
                        setContentTitle("SeatCheck!")
                        setContentText(intent.getCharSequenceExtra("reminder"))
                        setWhen(System.currentTimeMillis())
                        setPriority(Notification.PRIORITY_DEFAULT)
                        setAutoCancel(true)
                        setDefaults(Notification.DEFAULT_SOUND)
                        setContentIntent(PendingIntent.getActivity(context, 0, Intent(context,ActivitySeatLive::class.java).putExtra("comingFromExpireNotification",true).putExtra("postId", intent!!.getStringExtra("seatId")), PendingIntent.FLAG_UPDATE_CURRENT))
                    }.build()
            )


    }
}