package app.rubbickcube.seatcheck.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.view.View
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.R
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_seat_live_in_meeting.*
import kotlinx.android.synthetic.main.show_notification_alert_dialog.view.*
import android.media.MediaPlayer



class ShowAlertDialogActivity : AppCompatActivity() {

    var mPlayer : MediaPlayer? =null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_alert_dialog)

        showSeatPostDialog(AppClass.title,AppClass.message,"")

         mPlayer = MediaPlayer.create(this@ShowAlertDialogActivity, R.raw.alert)
         mPlayer?.start()

    }

    private fun showSeatPostDialog(title: String, message: String,notificationFor : String) {

        val builder = AlertDialog.Builder(this@ShowAlertDialogActivity)
        val inflater = layoutInflater

        val dialog_layout = inflater.inflate(R.layout.show_notification_alert_dialog, null)
        builder.setView(dialog_layout)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.getWindow()!!.setBackgroundDrawableResource(R.drawable.rounded_corner)

        dialog.show()

        dialog_layout.dialog_title.setText(title)
        dialog_layout.dialog_message.setText(message)


        if(AppClass.notificationFor.equals("cancelled")) {
            dialog_layout.dialog_btn_no.visibility = View.INVISIBLE
            dialog_layout.dialog_btn_yes.text = "Okey"


        }else {
            dialog_layout.dialog_btn_no.visibility = View.VISIBLE
            dialog_layout.dialog_btn_yes.text = "Check"


        }

        if(AppClass.notificationFor.equals("ended")) {
            dialog_layout.dialog_btn_no.visibility = View.VISIBLE
            dialog_layout.dialog_btn_no.text = "No thanks"
            dialog_layout.dialog_btn_yes.visibility = View.VISIBLE
            dialog_layout.dialog_btn_yes.text = "Yes"

        }


        if(AppClass.notificationFor.equals("declined")) {
            dialog_layout.dialog_btn_no.visibility = View.INVISIBLE
            dialog_layout.dialog_btn_no.text = "No thanks"
            dialog_layout.dialog_btn_yes.visibility = View.VISIBLE
            dialog_layout.dialog_btn_yes.text = "OK"

        }

        if(AppClass.notificationFor.equals("expired")) {
            dialog_layout.dialog_btn_no.visibility = View.INVISIBLE
            dialog_layout.dialog_btn_no.text = "No thanks"
            dialog_layout.dialog_btn_yes.visibility = View.VISIBLE
            dialog_layout.dialog_btn_yes.text = "OK"

        }




        dialog_layout.dialog_btn_no.setOnClickListener {


            if(AppClass.notificationFor.equals("ended")) {
                val intent = Intent(this, NeedASeatActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }else {
                dialog.dismiss()
                finish()
            }

        }


        dialog_layout.dialog_btn_yes.setOnClickListener {



            if(AppClass.notificationFor.equals("cancelled")) {

                if(Prefs.getBoolean("isLive",false)) {
                    Prefs.putBoolean("inMeeting",false)
                    Prefs.putInt("userStatus",1)
                    startActivity(Intent(this@ShowAlertDialogActivity,NeedASeatActivity::class.java))
                    finish()
                }else {
                    Prefs.putBoolean("isLive",false)
                    Prefs.putBoolean("inMeeting",false)
                    Prefs.putInt("userStatus",0)
                    //HawkUtils.deleteHawk()
                    startActivity(Intent(this@ShowAlertDialogActivity,NeedASeatActivity::class.java))
                    finish()
                }


            }else if(AppClass.notificationFor.equals("ended")) {

                if(Prefs.getBoolean("isLive",false)) {
                    startActivity(Intent(this@ShowAlertDialogActivity,ActivityEndMeeting::class.java).putExtra("user","owner"))

                }else {
                    startActivity(Intent(this@ShowAlertDialogActivity,ActivityEndMeeting::class.java).putExtra("user","notOwner"))

                }
                finish()

            }else if(AppClass.notificationFor.equals("declined")) {

                dialog.dismiss()
                startActivity(Intent(this@ShowAlertDialogActivity,NeedASeatActivity::class.java))
                finish()
            }else if(AppClass.notificationFor.equals("expired")) {

                dialog.dismiss()
                startActivity(Intent(this@ShowAlertDialogActivity,NeedASeatActivity::class.java))
                finish()
            } else {

                startActivity(Intent(this@ShowAlertDialogActivity,ActivityShowInvites::class.java))
                finish()
            }

        }

    }

    override fun onDestroy() {
        mPlayer?.stop();
        super.onDestroy();
    }



}
