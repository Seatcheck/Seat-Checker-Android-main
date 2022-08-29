package app.rubbickcube.seatcheck.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import app.rubbickcube.seatcheck.ActivityLogin
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.model.Users
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.bumptech.glide.Glide
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.iid.FirebaseInstanceId
import com.kaopiz.kprogresshud.KProgressHUD
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_ndlogin_singup_with_activitiy.*
import java.util.*
import kotlin.system.exitProcess

class NDLoginSingupWithActivitiy : AppCompatActivity(), View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private lateinit var webView: WebView
    var mLoginManager : LoginManager?  = null
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
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_ndlogin_singup_with_activitiy)
        supportActionBar?.hide()


        init()
        showDialog()
    }
    private fun showDialog(){
        val builder = AlertDialog.Builder(this)
        //performing positive action
        builder.setPositiveButton("Accept"){dialogInterface, which ->
        }

        builder.setNegativeButton("Decline"){dialogInterface, which ->
            Toast.makeText(applicationContext,"You declined Privacy Policy",Toast.LENGTH_LONG).show()
            finishAffinity()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)


        // webView = new Webview(this);
        val webView = WebView(this)
        webView.settings.setJavaScriptEnabled(true)

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url)
                return true
            }
        }
        webView.loadUrl("http://seatcheckapp.com/seatcheck-privacy-policy/")
        alertDialog.setView(webView)
        alertDialog.show()
    }
    private fun init() {

        btn_nd_facebook.setOnClickListener(this)
        btn_nd_google.setOnClickListener(this)
        btn_nd_signin_email.setOnClickListener(this)
        btn_nd_signup.setOnClickListener(this)
        initializeComponents()
    }

    private fun initializeComponents() {
        Backendless.initApp(this@NDLoginSingupWithActivitiy,resources.getString(R.string.appId), resources.getString(R.string.secretKey),"v1")
        //getDataFromfb()
        loginWithGoogle()


    }

    override fun onClick(v: View?) {

        if(v == btn_nd_facebook) {

            tapFbButton = true
            tabGoogleButton = false
            //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
            LoginWithFacebook()

        }else if(v == btn_nd_google) {

            tabGoogleButton = true
            tapFbButton = false
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, SIGN_IN)

        }else if(v == btn_nd_signup) {

           startActivity(Intent(this@NDLoginSingupWithActivitiy,SignupActivity::class.java))
            finish()
        }else if(v == btn_nd_signin_email) {
            startActivity(Intent(this@NDLoginSingupWithActivitiy, ActivityLogin::class.java))
            finish()

        }
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



    override fun onConnectionFailed(p0: ConnectionResult) {
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

        }
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

        val pd = Utils.SCProgressDialog(this@NDLoginSingupWithActivitiy,null,"Signing Up please wait...")
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
                startActivity(Intent(this@NDLoginSingupWithActivitiy, NeedASeatActivity::class.java))
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
                    Utils.showAlertDialog(this@NDLoginSingupWithActivitiy,"Something went wrong",fault.message);
                }
                //Toast.makeText(getContext(), fault.message, Toast.LENGTH_LONG).show()
            }
        })

    }


    private fun loginUser(email : String?, password : String?,socialLogin : Boolean) {

        val pd = Utils.SCProgressDialog(this@NDLoginSingupWithActivitiy,null,"Signing In Please wait...")
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


                        _socialLogin = socialLogin;
                        updateDeviceToken(response.objectId,pd)

                        ///startActivity(Intent(this@ActivityLogin,MainActivity::class.java))


                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Utils.dismissProgressDialog(pd)
                        // Toast.makeText(getContext(), fault.message, Toast.LENGTH_LONG).show()
                        Utils.showAlertDialog(this@NDLoginSingupWithActivitiy,"Something went wrong!",fault.message)
                    }
                })

    }

    private fun updateDeviceToken(userId : String, pd : KProgressHUD) {





        Backendless.Data.of(Users::class.java).findById(userId, object : AsyncCallback<Users>{
            override fun handleFault(fault: BackendlessFault?) {
                Toast.makeText(this@NDLoginSingupWithActivitiy,fault?.message, Toast.LENGTH_SHORT).show()
                Log.d("seatCheck","Error : " +fault?.message)
                Utils.dismissProgressDialog(pd)

            }


            override fun handleResponse(response: Users?) {

                var token = FirebaseInstanceId.getInstance().token
                response?.fcmToken =  token

                Backendless.Persistence.save(response,object : AsyncCallback<Users>{
                    override fun handleFault(fault: BackendlessFault?) {
                        Utils.dismissProgressDialog(pd)
                        Toast.makeText(this@NDLoginSingupWithActivitiy,fault?.message, Toast.LENGTH_SHORT).show()

                    }

                    override fun handleResponse(response: Users?) {

                        Prefs.putBoolean("isLogin",true)
                        Prefs.putBoolean("socialLogin",_socialLogin!!)

                        Utils.dismissProgressDialog(pd)

                        startActivity(Intent(this@NDLoginSingupWithActivitiy, NeedASeatActivity::class.java))
                        finish()

                    }


                })

            }


        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)

        if(resultCode != RESULT_CANCELED){
            if (requestCode == SIGN_IN && data != null) {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result)

            }
        }
        if (resultCode != Activity.RESULT_CANCELED) {

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun LoginWithFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext())
        mLoginManager = LoginManager.getInstance()
        callbackManager = CallbackManager.Factory.create()
        mLoginManager?.logInWithReadPermissions(this@NDLoginSingupWithActivitiy, Arrays.asList("public_profile"))


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
                Toast.makeText(this@NDLoginSingupWithActivitiy,"Login Cancel",Toast.LENGTH_SHORT).show()

            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(this@NDLoginSingupWithActivitiy,"Login Failed!" + error?.message,Toast.LENGTH_SHORT).show()

            }


        })
    }




}
