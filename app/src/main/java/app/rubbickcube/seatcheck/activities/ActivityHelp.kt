package app.rubbickcube.seatcheck.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import app.rubbickcube.seatcheck.R
import androidx.core.app.NavUtils
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_help.*
import android.net.Uri.fromParts
import android.view.View
import kotlinx.android.synthetic.main.app_bar_layout.*


class ActivityHelp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        supportActionBar?.hide()

        appbar_title.text = "Help"
        appbar_img.visibility = View.INVISIBLE


        contanc_email.setOnClickListener {

            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "theseatcheck@gmail.com", null))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }

        appbar_back.setOnClickListener {

            finish()
        }


    }



}
