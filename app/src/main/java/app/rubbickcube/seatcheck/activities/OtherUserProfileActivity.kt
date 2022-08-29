package app.rubbickcube.seatcheck.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils

import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.adapters.SimpleFragmentPagerAdapter
import app.rubbickcube.seatcheck.model.Invites
import app.rubbickcube.seatcheck.model.Reviews
import app.rubbickcube.seatcheck.model.Users
import com.backendless.Backendless
import com.backendless.BackendlessCollection
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.files.BackendlessFile
import com.backendless.persistence.BackendlessDataQuery
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_other_user_profile.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.bg_layer_image
import kotlinx.android.synthetic.main.activity_profile.dp_image_profile
import kotlinx.android.synthetic.main.activity_profile.dp_name_profile
import kotlinx.android.synthetic.main.activity_profile.sliding_tabs
import kotlinx.android.synthetic.main.activity_profile.viewpager
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.nd_profile_activity.*
import kotlinx.android.synthetic.main.profile_edit_dialog.view.*
import java.io.File
import java.util.*

class OtherUserProfileActivity : AppCompatActivity() {

    val options =  RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)
    private var otherUserProfileImage: String? = null
    private var otherUserId: String? = null
    private var otherUserName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_user_profile)
        Hawk.init(this).build()
        supportActionBar?.hide()
        setupComponents()
        setupListener()
    }



    private fun setupComponents() {


         otherUserProfileImage = intent.getStringExtra("otherUserProfileImage")
         otherUserId = intent.getStringExtra("otherUserId")
         otherUserName = intent.getStringExtra("otherUserName")
            Prefs.putString("otherUserId",otherUserId)
            setHeader()
            appbar_title.text = ""
        nd_edit_profile.visibility = View.GONE

        nd_chat_icon.setOnClickListener {

            val intent = Intent(this@OtherUserProfileActivity,ChatActivity::class.java)
            intent.putExtra("sender_id", Prefs.getString("userId",""))
            intent.putExtra("receiver_id",otherUserId)
            intent.putExtra("name",otherUserName)
            startActivity(intent)
        }

            grantCurrentUser(otherUserId.toString())
            getReviews(otherUserId.toString())

    }

    private fun setupListener() {
        appbar_back.setOnClickListener { finish() }
    }

    private fun setHeader() {

        Glide.with(this@OtherUserProfileActivity).load(otherUserProfileImage).apply(options).into(dp_image_profile)
        Glide.with(this@OtherUserProfileActivity).load(otherUserProfileImage).apply(options).into(bg_layer_image)

        dp_name_profile.setText(otherUserName)
    }

    private fun grantCurrentUser(userId : String) {

        Backendless.UserService.findById(userId, object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {
                //Toast.makeText(this@SplashActivity,"Something went wrong.."+fault?.message,Toast.LENGTH_LONG).show()
            }

            override fun handleResponse(response: BackendlessUser?) {
                HawkUtils.putHawk("BackendlessOtherUser",response)

                viewpager.adapter = SimpleFragmentPagerAdapter(this@OtherUserProfileActivity,supportFragmentManager)
                sliding_tabs.setupWithViewPager(viewpager)

                //Utils.startActivityWithAnimation(this@SplashActivity,MainActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
            }

        })

    }


    override fun onResume() {
        super.onResume()


    }


    private fun getReviews(userId: String) {
        val currentObjectId = Utils.quote(userId)
        val whereClause = "toUser.objectId = $currentObjectId"
        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        Backendless.Persistence.of(Reviews::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Reviews>> {

                    override fun handleResponse(foundContacts: BackendlessCollection<Reviews>) {

                        var sum = 0
                        for(i in foundContacts.data.indices) {

                            val star = foundContacts.data[i].numberOfStars
                            sum = sum.plus(star.toInt())
                        }

                        if(foundContacts.data.size != 0) {
                            val rating = sum.div(foundContacts.data.size)
                            ratingBar_rating_stars_profiles.visibility = View.VISIBLE
                            ratingBar_rating_stars_profiles.rating = rating.toFloat()

                        }else {
                            ratingBar_rating_stars_profiles.visibility = View.INVISIBLE

                        }




                    }

                    override fun handleFault(fault: BackendlessFault) {

                        Toast.makeText(this@OtherUserProfileActivity,fault.message,Toast.LENGTH_SHORT).show()

                    }
                })
    }


    override fun onDestroy() {
        super.onDestroy()
        AppClass.reviewForOwner = false
    }



}
