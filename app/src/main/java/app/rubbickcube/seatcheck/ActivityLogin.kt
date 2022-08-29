package app.rubbickcube.seatcheck

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.util.Log
import app.rubbickcube.seatcheck.activities.SignupActivity
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import kotlinx.android.synthetic.main.activity_login.*
import android.widget.Toast
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import com.backendless.exceptions.BackendlessFault
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.activities.AppIntroActivity
import app.rubbickcube.seatcheck.activities.NeedASeatActivity
import app.rubbickcube.seatcheck.model.Post
import app.rubbickcube.seatcheck.model.Users
import com.backendless.BackendlessCollection
import com.backendless.persistence.BackendlessDataQuery
import com.facebook.*
import com.pixplicity.easyprefs.library.Prefs
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.iid.FirebaseInstanceId
import com.kaopiz.kprogresshud.KProgressHUD
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.extend_time_dialog.view.*
import kotlinx.android.synthetic.main.forget_password_start_dialog.*
import kotlinx.android.synthetic.main.forget_password_start_dialog.view.*
import kotlinx.android.synthetic.main.show_notification_alert_dialog.view.*
import java.util.*


class ActivityLogin : AppCompatActivity() , GoogleApiClient.OnConnectionFailedListener{



    var mLoginManager :LoginManager?  = null
    var mAccessTokenTracker : AccessTokenTracker? =null
        /**/
    var callbackManager: CallbackManager? = null
    private val TAG = "MainActivity"
    private val SIGN_IN = 1
    private var _socialLogin : Boolean? = false
     var mGoogleApiClient: GoogleApiClient? = null
    var googleEmail : String? = null
    var facebookEmail : String? = null
    val facebookPass = "123"
    val googlePass = "fa77deaeb2916d341f1eb0f0df175805"
    var tapFbButton = false
    var tabGoogleButton = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()
        Hawk.init(this).build()

        initializeComponents()
        setupListener()

    }


    private fun initializeComponents() {
        Backendless.initApp(this@ActivityLogin,resources.getString(R.string.appId), resources.getString(R.string.secretKey),"v1")
        //getDataFromfb()
        loginWithGoogle()


    }

    private fun setupListener() {
        btn_sign_in.setOnClickListener {

            when {
                txt_email_signin.text.toString().trim().isNullOrEmpty() -> {
                    Toast.makeText(getContext(), "Email is required", Toast.LENGTH_LONG).show()

                }
                !Utils.isValidEmail(txt_email_signin.text.toString().trim()) -> {
                    Toast.makeText(getContext(), "Please enter correct email address", Toast.LENGTH_LONG).show()

                }
                txt_password_signin.text.toString().trim().isNullOrEmpty() -> {
                    Toast.makeText(getContext(), "Password is required", Toast.LENGTH_LONG).show()

                }
                else -> {
                    loginUser(txt_email_signin.text.toString().trim(),txt_password_signin.text.toString().trim(),false)
                }
            }

        }

        btn_forgot_password.setOnClickListener {

            showForgotPasswordDialog()
        }

        tv_register.setOnClickListener {
            startActivity(Intent(this@ActivityLogin,SignupActivity::class.java))
        }

        btn_fbloging.setOnClickListener{
            tapFbButton = true
            tabGoogleButton = false
            //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
            LoginWithFacebook()
        }

        btn_googlelogin.setOnClickListener{

            tabGoogleButton = true
            tapFbButton = false
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, SIGN_IN)
        }

    }

    private fun loginUser(email : String?, password : String?,socialLogin : Boolean) {

        val pd = Utils.SCProgressDialog(this@ActivityLogin,null,"Signing In Please wait...")
        pd.show()
        Backendless.UserService.login(
                email,
                password,
                object : AsyncCallback<BackendlessUser> {
                    override fun handleResponse(response: BackendlessUser) {
                        Prefs.putString("userId",response.userId)
                        Prefs.putString("userName",response.properties["name"].toString())

                        Backendless.UserService.setCurrentUser(response);
                        HawkUtils.putHawk("BackendlessUser",response)


                        _socialLogin = socialLogin

                        Prefs.putBoolean("isLogin",true)
                        Prefs.putBoolean("socialLogin",_socialLogin!!)

                        Utils.dismissProgressDialog(pd)

                        startActivity(Intent(this@ActivityLogin, NeedASeatActivity::class.java))
                        finish()

                       // updateDeviceToken(response.objectId,pd)

                        ///startActivity(Intent(this@ActivityLogin,MainActivity::class.java))


                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Utils.dismissProgressDialog(pd)
                       // Toast.makeText(getContext(), fault.message, Toast.LENGTH_LONG).show()
                        Utils.showAlertDialog(this@ActivityLogin,"Something went wrong!",fault.message)
                    }
                })

    }

    fun getContext(): Context {
        return this
    }

    private fun grantCurrentUser() {
        val pd = Utils.SCProgressDialog(this@ActivityLogin,null,"Signing Up please wait...")
        pd.show()


        Backendless.UserService.findById(Prefs.getString("userId",""), object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {
                Utils.dismissProgressDialog(pd)
                Toast.makeText(this@ActivityLogin,"Something went wrong..",Toast.LENGTH_LONG).show()

            }

            override fun handleResponse(response: BackendlessUser?) {
                Utils.dismissProgressDialog(pd)
                HawkUtils.putHawk("BackendlessUser",response)
              //  Backendless.UserService.setCurrentUser( response )
                if(response!!.properties["profileImage"] != null) {
                    Prefs.putString("profileImage",response.properties["profileImage"].toString())
                }
                if(response.properties["phone"] != null) {
                    Prefs.putString("profilePhone",response.properties["phone"].toString())
                }
                if(response.properties["about"] != null) {
                    Prefs.putString("profileAbout",response.properties["about"].toString())
                }
                if(response.properties["status"] != null) {
                    Prefs.putString("profileStaus",response.properties["status"].toString())
                }
                Prefs.putString("profileName",response.properties["name"].toString())
                Prefs.putString("profileEmail",response.email)

                Prefs.putBoolean("isLogin",true)
                startActivity(Intent(this@ActivityLogin, NeedASeatActivity::class.java))
                finish()


            }

        })

    }

    private fun getDataFromfb() {

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {


                val request = GraphRequest.newMeRequest(loginResult.accessToken) { data, response ->
                    try {
                        val id = data.getString("id")
                        facebookEmail = data.getString("email")
                        val fname = data.getString("first_name")
                        val lname =  data.getString("last_name")
                        val profileImage = "http://graph.facebook.com/$id/picture?type=large"

                        registerUser(fname+" "+ lname,fname+" "+ lname,facebookEmail,facebookPass,"-------",profileImage)
                    } catch (e: Exception) {

                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,first_name,last_name,email");
                request.parameters = parameters
                request.executeAsync()

            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException) {
                Log.i("RESAULTS : ", error.message)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)

        if(resultCode != RESULT_CANCELED){
            if (requestCode == SIGN_IN && data != null) {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result)

            }else{
                Toast.makeText(applicationContext, "Error!!", Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode != Activity.RESULT_CANCELED) {

        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun registerUser(name: String?, displaName: String? ,email: String? ,password: String?, mobile: String?, profilImage : String) {

        var token = FirebaseInstanceId.getInstance().token
        val user = BackendlessUser()
        user.email = email
        user.password = password
        user.setProperty("name",displaName)
        user.setProperty("firstName",name)
        user.setProperty("phone",mobile)
        user.setProperty("fcmToken",token)
        user.setProperty("profileImage",profilImage)

        val pd = Utils.SCProgressDialog(this@ActivityLogin,null,"Signing Up please wait...")
        pd.show()

        Backendless.UserService.register(user, object : AsyncCallback<BackendlessUser> {
            override fun handleResponse(response: BackendlessUser) {
                Utils.dismissProgressDialog(pd)

                Backendless.UserService.setCurrentUser(response);
                Prefs.putString("userId",response.userId)

                HawkUtils.putHawk("BackendlessUser",response)
                //  Backendless.UserService.setCurrentUser( response )
                if(response!!.properties["profileImage"] != null) {
                    Prefs.putString("profileImage",response!!.properties["profileImage"].toString())
                }
                if(response!!.properties["phone"] != null) {
                    Prefs.putString("profilePhone",response!!.properties["phone"].toString())
                }
                if(response!!.properties["about"] != null) {
                    Prefs.putString("profileAbout",response!!.properties["about"].toString())
                }
                if(response!!.properties["status"] != null) {
                    Prefs.putString("profileStaus",response!!.properties["status"].toString())
                }
                Prefs.putString("profileName",response!!.properties["name"].toString())
                Prefs.putString("profileEmail",response.email)

                Prefs.putBoolean("isLogin",true)
                startActivity(Intent(this@ActivityLogin, NeedASeatActivity::class.java))
                finish()

                //grantCurrentUser()

                //  Prefs.putInt("userId",response.properties.user);
            }

            override fun handleFault(fault: BackendlessFault) {
                Utils.dismissProgressDialog(pd)

                if(tapFbButton) {
                    if(fault.code.equals("3033")) {

                        loginUser(facebookEmail,facebookPass.trim(),true)
                    }
                }else if(tabGoogleButton) {
                    if(fault.code.equals("3033")) {
                        loginUser(googleEmail,googlePass.trim(),true)
                    }
                }else {
                    Utils.showAlertDialog(this@ActivityLogin,"Something went wrong",fault.message);
                }
                //Toast.makeText(getContext(), fault.message, Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun loginWithGoogle() {
        val gso =  GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()

        mGoogleApiClient =  GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

     private fun handleSignInResult(result : GoogleSignInResult) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            val acct = result.getSignInAccount()
            val accountName = acct?.getDisplayName()
            googleEmail = acct?.getEmail()
            val accountPic = acct?.getPhotoUrl()


            if (mGoogleApiClient != null && mGoogleApiClient?.isConnected()!!) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            }
            // logout
//            if (mGoogleApiClient != null && mGoogleApiClient?.isConnected()!!) {
//                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
//            }
            registerUser(accountName,accountName,googleEmail,googlePass,"------",accountPic.toString())


        } else {
            Log.d(TAG, "handleSignInResulterror:" + result.isSuccess());
        }
    }

    override fun onStart() {
        super.onStart()
//      val opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient) as   OptionalPendingResult<GoogleSignInResult>
//        if (opr.isDone()) {
//            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
//            // and the GoogleSignInResult will be available instantly.
//            Log.d(TAG, "Got cached sign-in");
//            val result = opr.get();
//            handleSignInResult(result)
//        } else {
//            // If the user has not previously signed in on this device or the sign-in has expired,
//            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
//            // single sign-on will occur in this branch.
//            opr.setResultCallback(object : ResultCallback<GoogleSignInResult> {
//                override fun onResult(p0: GoogleSignInResult) {
//
//                    handleSignInResult(p0)
//                }
//
//            });
//        }
    }


    private fun showForgotPasswordDialog() {

        val builder = AlertDialog.Builder(this@ActivityLogin)
        val inflater = layoutInflater

        val dialog_layout = inflater.inflate(R.layout.forget_password_start_dialog, null)
        builder.setView(dialog_layout)
        val dialog = builder.create()
        dialog.setCancelable(true)
        dialog.getWindow()!!.setBackgroundDrawableResource(R.drawable.rounded_corner)


        dialog.show()


        dialog_layout.dialog_btn_yes_forgot.setOnClickListener {



            if(dialog_layout.et_user_email.text.isNullOrEmpty()) {

                Toast.makeText(this@ActivityLogin,"Email is required.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if(!Utils.isValidEmail(dialog_layout.et_user_email.text.toString())) {
                Toast.makeText(this@ActivityLogin,"Please enter correct email address.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else {
                dialog.dismiss()
                requestForgetPasswordEmail(dialog_layout.et_user_email.text.toString().trim())
            }

        }

        dialog_layout.dialog_btn_no_forgot.setOnClickListener {

            dialog.dismiss()
        }


    }


    private fun requestForgetPasswordEmail(email : String) {

        val pd = Utils.SCProgressDialog(this@ActivityLogin,null,"Please wait....")
        pd.show()
        Backendless.UserService.restorePassword(email, object : AsyncCallback<Void> {
            override fun handleFault(fault: BackendlessFault?) {
                Utils.dismissProgressDialog(pd)
            }

            override fun handleResponse(response: Void?) {

                Toast.makeText(this@ActivityLogin,"Password reset email has sent",Toast.LENGTH_SHORT).show()
                Utils.dismissProgressDialog(pd)
            }

        })
    }


    private fun LoginWithFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext())
        mLoginManager = LoginManager.getInstance()
        callbackManager = CallbackManager.Factory.create()
        mLoginManager?.logInWithReadPermissions(this@ActivityLogin, Arrays.asList("public_profile"))


        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {


                val request = GraphRequest.newMeRequest(result?.accessToken) { data, response ->
                    try {
                        val id = data.getString("id")

                        if(data.has("email")) {
                            facebookEmail = data.getString("email")
                        }else {
                            facebookEmail = id+"@facebook.com"

                        }

                        val fname = data.getString("first_name")
                        val lname =  data.getString("last_name")
                        val profileImage = "http://graph.facebook.com/$id/picture?type=large"

                        registerUser(fname+" "+ lname,fname+" "+ lname,facebookEmail,facebookPass,"-------",profileImage)
                    } catch (e: Exception) {

                        e.message
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,first_name,last_name,email");
                request.parameters = parameters
                request.executeAsync()
            }


            override fun onCancel() {
                Toast.makeText(this@ActivityLogin,"Login Cancel",Toast.LENGTH_SHORT).show()

            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(this@ActivityLogin,"Login Failed!" + error?.message,Toast.LENGTH_SHORT).show()

            }


        })
    }



    private fun updateDeviceToken(userId : String, pd : KProgressHUD) {





        Backendless.Data.of(Users::class.java).findById(userId, object : AsyncCallback<Users>{
            override fun handleFault(fault: BackendlessFault?) {
                Toast.makeText(this@ActivityLogin,fault?.message,Toast.LENGTH_SHORT).show()
                Log.d("seatCheck","Error : " +fault?.message)
                Utils.dismissProgressDialog(pd)

            }


            override fun handleResponse(response: Users?) {

               var token = FirebaseInstanceId.getInstance().token
                response?.fcmToken =  token

                Backendless.Persistence.save(response,object : AsyncCallback<Users>{
                    override fun handleFault(fault: BackendlessFault?) {
                        Utils.dismissProgressDialog(pd)
                        Toast.makeText(this@ActivityLogin,fault?.message,Toast.LENGTH_SHORT).show()

                    }

                    override fun handleResponse(response: Users?) {

                        Prefs.putBoolean("isLogin",true)
                        Prefs.putBoolean("socialLogin",_socialLogin!!)

                        Utils.dismissProgressDialog(pd)

                        startActivity(Intent(this@ActivityLogin, NeedASeatActivity::class.java))
                        finish()
                    }
                })
            }

        })

    }

    override fun onBackPressed() {
        finishAffinity()
    }
}
