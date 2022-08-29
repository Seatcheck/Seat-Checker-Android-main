package app.rubbickcube.seatcheck

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.Toast
import app.invision.morse.api.ApiUtils
import app.invision.morse.api.SOService
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.Helpers.Utils.Companion.dismissProgressDialog
import app.rubbickcube.seatcheck.activities.*
import app.rubbickcube.seatcheck.adapters.InvitesAdapter
import app.rubbickcube.seatcheck.adapters.ReviewAdapter
import app.rubbickcube.seatcheck.api.EndPointsConstants
import app.rubbickcube.seatcheck.model.*
import com.backendless.Backendless
import com.backendless.BackendlessCollection
import com.backendless.BackendlessUser
import com.backendless.IDataStore
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.BackendlessDataQuery
import com.kaopiz.kprogresshud.KProgressHUD
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_show_invites.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.forget_password_dialog.view.*
import kotlinx.android.synthetic.main.fragment_review_fragments.view.*
import kotlinx.android.synthetic.main.need_seat_layou.*
import kotlinx.android.synthetic.main.show_notification_alert_dialog.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import weborb.util.ThreadContext.context
import javax.inject.Inject

class SettingsActivity : AppCompatActivity(),
        CompoundButton.OnCheckedChangeListener {


    var pd: KProgressHUD? = null
    var backendlessUser: BackendlessUser = Backendless.UserService.CurrentUser()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()
        // AppClass.appComponent?.inject(this)
        setupComponents()
        setupListener()
    }

    private lateinit var listItem: ArrayList<String>

    private fun setupComponents() {

        appbar_title.text = "Settings"
        appbar_img.visibility = View.INVISIBLE

        val btn_delete = findViewById(R.id.deleteaccount) as RelativeLayout

//        btn_delete.setOnClickListener {
//            pd = Utils.SCProgressDialog(this@SettingsActivity, null, "Deleting Profile...")
//            pd?.show()
//             deletePost()
//        }

        btn_delete.setOnClickListener {
            val builder = AlertDialog.Builder(this@SettingsActivity)
            builder.setMessage("Are you sure you want to delete your account? this action cannot be undone.")
                    .setTitle("Delete Account?")
                    .setCancelable(false)
                    .setPositiveButton("Delete") { dialog, id ->
                        pd = Utils.SCProgressDialog(this@SettingsActivity, null, "Deleting Profile...")
                        pd?.show()
                        deletePost()
                    }
                    .setNegativeButton("Cancel") { dialog, id ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
            val alert = builder.create()
            alert.show()
        }


        privacy.setOnCheckedChangeListener(this)
        privacy.isChecked = Prefs.getBoolean("visibility", false)

        listItem = if (Prefs.getBoolean("socialLogin", false)) {
            arrayListOf<String>("Notification On/Off", "Edit Profile", "Invite", "Contact Us", "Watch Tutorials", "Delete Account")
        } else {
            arrayListOf<String>("Notification On/Off", "Edit Profile", "Changed Password", "Invite", "Contact Us", "Watch Tutorials")
        }
        settingList.adapter = ArrayAdapter<String>(this, R.layout.listview_text, listItem)

        settingList.setOnItemClickListener { adapterView, view, i, l ->

            if (listItem[i].equals("Notification On/Off")) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    val intent = Intent()
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"

//for Android 5-7
                    intent.putExtra("app_package", packageName)
                    intent.putExtra("app_uid", applicationInfo.uid)

// for Android O
                    intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)

                    startActivity(intent)
                }

            } else if (listItem[i].equals("Edit Profile")) {
                startActivity(Intent(this@SettingsActivity, ProfileActivity::class.java))

            } else if (listItem[i].equals("Changed Password")) {


                showSeatPostDialog()
            } else if (listItem[i].equals("Invite")) {
                startActivity(Intent(this@SettingsActivity, ActivityShowUserContacts::class.java))

            } else if (listItem[i].equals("Contact Us")) {
                startActivity(Intent(this@SettingsActivity, ActivityHelp::class.java))

            } else if (listItem[i].equals("Watch Tutorials")) {
                startActivity(Intent(this@SettingsActivity, AppIntroActivity::class.java))

            }
        }

    }
    private var postsList: MutableList<Post>? = null
    private var filteredpostsList: MutableList<Post>? = null

    private fun deletePost() {

        val currentObjectId = Utils.quote(backendlessUser.objectId)
        val whereClause = "User.objectId = $currentObjectId"
        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        Backendless.Persistence.of(Post::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Post>> {
                    override fun handleResponse(foundInvites: BackendlessCollection<Post>) {

                        postsList = arrayListOf()
                        filteredpostsList = arrayListOf()
                        Log.d(Companion.TAG, "handleResponse1: "+foundInvites.data.indices )
                        for (i in foundInvites.data.indices) {
                            if (foundInvites.data[i] != null) {
                                // invitesList?.add(foundInvites.data[i])
                                Backendless.Data.of(Post::class.java).remove(foundInvites.data[i], delpost)

                                Prefs.putBoolean("isLive", false)
                                Prefs.putInt("userStatus", 0)
                                Prefs.remove("requestForExtendTime")
                                Utils.cancelAlarm(this@SettingsActivity)
                                finish()
                            }
                        }
                        deleteReviews()
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Toast.makeText(this@SettingsActivity, fault.message, Toast.LENGTH_LONG).show()

                    }
                })


    }
    private var availibilityList: MutableList<Invites>? = null
    private var filteredAvailibilityList: MutableList<Invites>? = null

    private fun deleteAvailibility() {
        val currentObjectId = Utils.quote(backendlessUser.objectId)

        val whereClause = "receiver.objectId = $currentObjectId  "
        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        Backendless.Persistence.of(Invites::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Invites>> {
                    override fun handleResponse(foundInvites: BackendlessCollection<Invites>) {

                        availibilityList = arrayListOf()
                        filteredAvailibilityList = arrayListOf()
                        for (i in foundInvites.data.indices) {
                            if (foundInvites.data[i].receiver != null) {
                                // invitesList?.add(foundInvites.data[i])
                                Backendless.Data.of(Invites::class.java).remove(foundInvites.data[i], delinvies)
                            }
                        }
                        deleteInvies()

                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Toast.makeText(this@SettingsActivity, fault.message, Toast.LENGTH_LONG).show()

                    }
                })
    }
    private var invitesList: MutableList<Invites>? = null
    private var filteredInvitesList: MutableList<Invites>? = null

    private fun deleteInvies() {
        val currentObjectId = Utils.quote(backendlessUser.objectId)
        val queryStatus = Utils.quote("pending")
        val whereClause = "receiver.objectId = $currentObjectId AND status = $queryStatus"
        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        Backendless.Persistence.of(Invites::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Invites>> {
                    override fun handleResponse(foundInvites: BackendlessCollection<Invites>) {

                        invitesList = arrayListOf()
                        filteredInvitesList = arrayListOf()
                        for (i in foundInvites.data.indices) {
                            if (foundInvites.data[i].receiver != null) {
                                // invitesList?.add(foundInvites.data[i])
                                Backendless.Data.of(Invites::class.java).remove(foundInvites.data[i], delinvies)
                            }
                        }
                        deleteUserProfile()
                    }
                    override fun handleFault(fault: BackendlessFault) {
                        Toast.makeText(this@SettingsActivity, fault.message, Toast.LENGTH_LONG).show()
                    }
                })
    }
    val delAccount = object : AsyncCallback<Long> {
        override fun handleResponse(response: Long?) {
            grantCurrentUser()
            dismissProgressDialog()
            Toast.makeText(this@SettingsActivity, "Profile Deleted Successfully!", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this@SettingsActivity, ActivityLogin::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
           // finish() // call this to finish the current activity

            val logOutTask = LogOutTask(this@SettingsActivity)
            logOutTask.execute()
        }

        override fun handleFault(fault: BackendlessFault?) {
            Toast.makeText(this@SettingsActivity, fault?.message, Toast.LENGTH_SHORT).show()
        }

    }
    val delpost = object : AsyncCallback<Long> {
        override fun handleResponse(response: Long?) {
           // Toast.makeText(this@SettingsActivity, "Posts Deleted", Toast.LENGTH_SHORT).show()
        }

        override fun handleFault(fault: BackendlessFault?) {
            Toast.makeText(this@SettingsActivity, fault?.message, Toast.LENGTH_SHORT).show()
        }

    }
    val delinvies = object : AsyncCallback<Long> {
        override fun handleResponse(response: Long?) {
           // Toast.makeText(this@SettingsActivity, "Deleted", Toast.LENGTH_SHORT).show()
        }

        override fun handleFault(fault: BackendlessFault?) {
            Toast.makeText(this@SettingsActivity, fault?.message, Toast.LENGTH_SHORT).show()
        }

    }
    val del = object : AsyncCallback<Long> {
        override fun handleResponse(response: Long?) {
          //  Toast.makeText(this@SettingsActivity, "Deleted", Toast.LENGTH_SHORT).show()
        }

        override fun handleFault(fault: BackendlessFault?) {
            Toast.makeText(this@SettingsActivity, fault?.message, Toast.LENGTH_SHORT).show()
        }

    }


    private lateinit var currentObjectId: String
    private fun deleteReviews() {
        if (Utils.getBackendLessUser(this@SettingsActivity!!) != null) {
            backendlessUser = Utils.getBackendLessUser(this@SettingsActivity!!)

            if (AppClass.reviewForOwner) {
                currentObjectId = backendlessUser.objectId
            } else {
                currentObjectId = Prefs.getString("otherUserId", "")
            }

            val currentObjectId = quote(currentObjectId)
            val whereClause = "toUser.objectId = $currentObjectId"
            val dataQuery = BackendlessDataQuery()
            dataQuery.whereClause = whereClause
            Backendless.Persistence.of(Reviews::class.java).find(dataQuery,
                    object : AsyncCallback<BackendlessCollection<Reviews>> {

                        override fun handleResponse(foundContacts: BackendlessCollection<Reviews>) {

                            try {
                                val filteredReviewList: MutableList<Reviews>? = arrayListOf()
                                if (foundContacts.data != null) {
                                    for (i in foundContacts.data.indices) {
                                        if (foundContacts.data[i].fromUser != null && foundContacts.data[i].toUser != null) {
                                            // filteredReviewList?.add(foundContacts.data[i])
                                            //delete
                                            Backendless.Data.of(Reviews::class.java).remove(foundContacts.data[i], del)


                                        }
                                    }
                                }

                                if (filteredReviewList != null) {
                                    //   view?.reviewList?.adapter = ReviewAdapter(this@SettingsActivity!!, filteredReviewList!!)

                                }
                            } catch (ex: NullPointerException) {

                            }
                            deleteAvailibility()
                        }

                        override fun handleFault(fault: BackendlessFault) {

                            //  pd.dismiss()
                            Toast.makeText(this@SettingsActivity, fault.message, Toast.LENGTH_SHORT).show()

                        }
                    })
        }
    }

    private fun setupListener() {

        appbar_back.setOnClickListener { finish() }
    }


    private fun showSeatPostDialog() {

        val builder = AlertDialog.Builder(this@SettingsActivity)
        val inflater = layoutInflater

        val dialog_layout = inflater.inflate(R.layout.forget_password_dialog, null)
        builder.setView(dialog_layout)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.getWindow()!!.setBackgroundDrawableResource(R.drawable.rounded_corner)

        dialog.show()


        dialog_layout.dialog_btn_no_change.setOnClickListener {

            dialog.dismiss()
        }
        dialog_layout.dialog_btn_yes_change.setOnClickListener {

            if (dialog_layout.et_password_change.text.isNullOrEmpty()) {
                Toast.makeText(this@SettingsActivity, "New password field is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            } else if (dialog_layout.et_confirm_password_change.text.isNullOrEmpty()) {
                Toast.makeText(this@SettingsActivity, "Confirm password field is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!dialog_layout.et_confirm_password_change.text.toString().equals(dialog_layout.et_password_change.text.toString())) {
                Toast.makeText(this@SettingsActivity, "Password does not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                dialog.dismiss()
                changePassword(dialog_layout.et_password_change.text.toString())
            }
        }

    }


    private fun changePassword(password: String) {

        val pd = Utils.SCProgressDialog(this@SettingsActivity, null, "Changing password...")
        pd.show()
        val user = backendlessUser
        user.password = password

        Backendless.Data.save(user, object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {
                Utils.dismissProgressDialog(pd)
                Toast.makeText(this@SettingsActivity, "Password could not change", Toast.LENGTH_SHORT).show()
            }

            override fun handleResponse(response: BackendlessUser?) {
                Utils.dismissProgressDialog(pd)
                Prefs.remove("isLive")
                Prefs.remove("seatId")
                Prefs.remove("userStatus")
                Prefs.remove("isLogin")
                HawkUtils.deleteHawk()
                val intent = Intent(this@SettingsActivity, ActivityLogin::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

        })
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        if (buttonView?.id == R.id.privacy) {
            if (isChecked) {
                setUserVisibility(isChecked)
            } else {
                setUserVisibility(isChecked)
            }
        }
    }

    private fun setUserVisibility(visibility: Boolean) {


        var setVisibility: String
        val mService = ApiUtils.getSOService()
        if (visibility) {
            setVisibility = "0"
        } else {
            setVisibility = "1"
        }

        mService.userVisibility(EndPointsConstants.VISIBILITY, Utils.getSimpleTextBody(backendlessUser.userId), Utils.getSimpleTextBody(setVisibility))
                .enqueue(object : Callback<SucessResponse> {
                    override fun onFailure(call: Call<SucessResponse>?, t: Throwable?) {

                        Toast.makeText(this@SettingsActivity, t?.message, Toast.LENGTH_LONG).show()

                    }

                    override fun onResponse(call: Call<SucessResponse>?, response: Response<SucessResponse>?) {


                        if (response?.isSuccessful!!) {
                            if (response?.body().success) {

                                Prefs.putBoolean("visibility", false)

                            } else {
                                Prefs.putBoolean("visibility", true)

                            }
                        } else {
                            response?.body().message
                        }
                    }

                })
    }


    fun deleteUserProfile() {


        val user = Users()
        user.objectId = backendlessUser.objectId
        user.fcmToken = Prefs.getString("fcmToken", "")
        user.email = backendlessUser.email


        Log.d(TAG, "deleteUserProfile: "+user)
        Backendless.Data.of(Users::class.java).remove(user,delAccount)


    }

    private fun grantCurrentUser() {

        Backendless.UserService.findById(Prefs.getString("userId", ""), object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {
                //Toast.makeText(this@SplashActivity,"Something went wrong.."+fault?.message,Toast.LENGTH_LONG).show()
            }

            override fun handleResponse(response: BackendlessUser?) {
                Backendless.UserService.setCurrentUser(response)
                HawkUtils.putHawk("BackendlessUser", response)
                //Utils.startActivityWithAnimation(this@SplashActivity,MainActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
            }

        })
    }

    private fun dismissProgressDialog() {
        if (pd != null && pd?.isShowing()!!) {
            pd?.dismiss()
        }
    }

    inner class LogOutTask(private val mContext: Context) : AsyncTask<Void, Void, Long>() {
        // val pd = Utils.SCProgressDialog(mContext,null,"Please wait..")
        override fun onPreExecute() {
            super.onPreExecute()
            // pd.show()
           // progressBar.visibility = View.VISIBLE
        }

        override fun onPostExecute(result: Long?) {
            //pd.dismiss()

            //Utils.dismissProgressDialog(pd!!)
//            progressBar.visibility = View.INVISIBLE

            Prefs.remove("isLive")
            Prefs.remove("seatId")
            Prefs.remove("userStatus")
            Prefs.remove("isLogin")
            Prefs.remove("socialLogin")
            Prefs.putInt("rating", 0)
            Prefs.remove("introDone")
            HawkUtils.deleteHawk()
            Utils.cancelAlarm(this@SettingsActivity)
            val intent = Intent(mContext, NDLoginSingupWithActivitiy::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // call this to finish the current activity   pd.dismiss()


        }

        override fun doInBackground(vararg p0: Void?): Long {

            for (i in 1..2) {
                Thread.sleep(1000)
            }

            return 0
        }


    }

    companion object {
        private const val TAG = "SettingsActivity"
    }
}

private fun <E> IDataStore<E>.remove(user: E, asyncCallback: AsyncCallback<E>) {

}

fun quote(s: String): String {
    return StringBuilder()
            .append('\'')
            .append(s)
            .append('\'')
            .toString()
}

 


