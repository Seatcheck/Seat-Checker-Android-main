package app.rubbickcube.seatcheck.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.R

import app.rubbickcube.seatcheck.activities.ActivityShowInvites
import weborb.util.ThreadContext.context
import androidx.core.content.ContextCompat.startActivity
import app.rubbickcube.seatcheck.MainActivity
import app.rubbickcube.seatcheck.activities.ShowAlertDialogActivity
import java.util.logging.Handler


class InviteBroadCast : BroadcastReceiver() {


    private var mContext : Context? = null
    override fun onReceive(p0: Context?, p1: Intent?) {
        this.mContext = p0

        val startIntent = Intent(p0,ShowAlertDialogActivity::class.java)
        // val startIntent = Intent("app.rubbickcube.seatcheck.services.InviteBroadCast","app.rubbickcube.seatcheck.activities.ShowAlertDialogActivity")
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext?.startActivity(startIntent)

//        android.os.Handler().postDelayed({
//                },1500)


    }

}