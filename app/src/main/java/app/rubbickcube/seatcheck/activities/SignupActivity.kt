package app.rubbickcube.seatcheck.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import app.rubbickcube.seatcheck.ActivityLogin
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.MainActivity
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.Helpers.Utils
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.google.firebase.iid.FirebaseInstanceId
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        supportActionBar?.hide()
        initializeComponents()
        setupListener()
        Hawk.init(this).build()

    }

    private fun initializeComponents() {
        Backendless.initApp(this@SignupActivity,resources.getString(R.string.appId), resources.getString(R.string.secretKey),"v1")

    }


    private fun registerUser(name: String?, displaName: String? ,email: String? ,password: String?, mobile: String?) {

        var token = FirebaseInstanceId.getInstance().token

        val user = BackendlessUser()
        user.email = email
        user.password = password
        user.setProperty("name",displaName)
        user.setProperty("firstName",name)
        user.setProperty("fcmToken",token)
        user.setProperty("phone",mobile)

        val pd = Utils.SCProgressDialog(this@SignupActivity,null,"Signing Up please wait...")
        pd.show()

        Backendless.UserService.register(user, object : AsyncCallback<BackendlessUser> {
            override fun handleResponse(response: BackendlessUser) {
                Utils.dismissProgressDialog(pd)

                Backendless.UserService.setCurrentUser(response)
                HawkUtils.putHawk("BackendlessUser",response)

                Prefs.putString("userId",response?.userId)
                grantCurrentUser()

              //Prefs.putInt("userId",response.properties.user);


            }

            override fun handleFault(fault: BackendlessFault) {
                Utils.dismissProgressDialog(pd)
                Utils.showAlertDialog(this@SignupActivity,"Something went wrong",fault.message);
                //Toast.makeText(getContext(), fault.message, Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun setupListener(){

        back_btn.setOnClickListener {
            finish()
        }

        btn_sign_in.setOnClickListener {
            startActivity(Intent(this@SignupActivity, ActivityLogin::class.java))

        }

        btn_signup.setOnClickListener {

            if(txt_name.text.toString().isNullOrEmpty()) {
                Toast.makeText(getContext(),"Name is required",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            } else if(txt_displayname.text.toString().isNullOrEmpty()) {
                Toast.makeText(getContext(),"Display Name is required",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else if(txt_email.text.toString().isNullOrEmpty()) {
                Toast.makeText(getContext(),"Email is required",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else if(!Utils.isValidEmail(txt_email.text.toString())) {
                Toast.makeText(getContext(),"Please enter correct email",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else if(txt_pass.text.toString().isNullOrEmpty()) {
                Toast.makeText(getContext(),"Password is required",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else if(txt_confirmpass.text.toString().isNullOrEmpty()) {
                Toast.makeText(getContext(),"Confirm password is required",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else if(!txt_pass.text.toString().equals(txt_confirmpass.text.toString())) {
                Toast.makeText(getContext(),"Password does not match",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }else {
                registerUser(txt_name.text.toString().trim(),txt_displayname.text.toString().trim(),txt_email.text.toString().trim(),txt_pass.text.toString().trim(),txt_mobile.text.toString().trim())

            }
        }
    }

    fun getContext(): Context {
        return this
    }

    private fun grantCurrentUser() {
        val pd = Utils.SCProgressDialog(this@SignupActivity,null,"Signing Up please wait...")
        pd.show()


        Backendless.UserService.findById(Prefs.getString("userId",""), object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {
                Utils.dismissProgressDialog(pd)
                Toast.makeText(this@SignupActivity,"Something went wrong..",Toast.LENGTH_LONG).show()

            }

            override fun handleResponse(response: BackendlessUser?) {
                Utils.dismissProgressDialog(pd)
                Backendless.UserService.setCurrentUser( response )
                HawkUtils.putHawk("BackendLessUser",response)
                if(response!!.properties["profileImage"] != null) {
                    Prefs.putString("profileImage",response!!.properties["profileImage"].toString())
                }
                if(response!!.properties["phone"] != null) {
                    Prefs.putString("profilePhone",response!!.properties["phone"].toString())
                }
                if(response!!.properties["about"] != null) {
                    Prefs.putString("profileAbout",response!!.properties["phone"].toString())
                }
                if(response!!.properties["status"] != null) {
                    Prefs.putString("profileStaus",response!!.properties["phone"].toString())
                }
                Prefs.putString("profileName",response!!.properties["name"].toString())
                Prefs.putString("profileEmail",response.email)

                Toast.makeText(getContext(), "Registration success!", Toast.LENGTH_LONG).show()
                Prefs.putBoolean("isLogin",true)
                startActivity(Intent(this@SignupActivity, NeedASeatActivity::class.java))
                finish()

            }

        })

    }
}
