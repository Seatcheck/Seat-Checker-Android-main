package app.rubbickcube.seatcheck

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.activities.AppIntroActivity
import app.rubbickcube.seatcheck.activities.NDLoginSingupWithActivitiy
import app.rubbickcube.seatcheck.activities.NeedASeatActivity
import app.rubbickcube.seatcheck.model.Post
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.bumptech.glide.Glide
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_splash.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()
        Backendless.initApp(this@SplashActivity, resources.getString(R.string.appId), resources.getString(R.string.secretKey), "v1")
        printKeyHash(this)
        Hawk.init(this).build()

        Glide.with(this).load(R.drawable.nd_splash_new).into(vview)


//        if(!Utils.isConnectedOnline(this)) {
//
//            JavaUtilSeatCheck.showAlertDialogWithFinishActivity(this,"Opps","Internet is not connected")
//        }


        CheckMapPermission()


    }


    //Commenting for new Design
//    private fun playVideo() {
//        val path = "android.resource://" + packageName + "/" + R.raw.intro
//        vview.setVideoURI(Uri.parse(path))
//        vview.start()
//
//        vview.setOnCompletionListener {
//
//
//            if(Prefs.getBoolean("isLogin",false)) {
//
//                val currentUserObjectId = Prefs.getString("userId","-1")
//                if (Backendless.UserService.CurrentUser()  == null) {
//
//                    if(currentUserObjectId.equals("-1")) {
//                        Utils.startActivityWithAnimation(this@SplashActivity,ActivityLogin::class.java,findViewById(R.id.vview),true,R.color.app_orange)
//
//                    }else {
//
//                        if(!Utils.isConnectedOnline(this)) {
//
//                            Utils.showAlertDialogWithFinish(this@SplashActivity,"Connectivity Error","It seems internet connection is down. Please connect your phone with network and try again")
//                        }else {
//                            grantCurrentUser(currentUserObjectId)
//
//                        }
//
//                    }
//                }else {
//
//                        if(Prefs.getBoolean("isLive",false) || Prefs.getBoolean("inMeeting",false)){
//                            startActivity(Intent(this@SplashActivity,NeedASeatActivity::class.java))
//                            finish()
//                            //Utils.startActivityWithAnimation(this@SplashActivity,ActivityLogin::class.java,findViewById(R.id.vview),true,R.color.app_orange)
//                        }else {
//                            Utils.startActivityWithAnimation(this@SplashActivity,AppIntroActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
//                        }
//
//                }
//            }else {
//                Utils.startActivityWithAnimation(this@SplashActivity,ActivityLogin::class.java,findViewById(R.id.vview),true,R.color.app_orange)
//
//            }
//
//
////            if(HawkUtils.getHawk("BackendlessUser") != null) {
////
////                if(Prefs.getBoolean("isLogin",false)) {
////
////                    if(Prefs.getBoolean("isLive",false) || Prefs.getBoolean("inMeeting",false)){
////                        startActivity(Intent(this@SplashActivity,NeedASeatActivity::class.java))
////                        finish()
////                        //Utils.startActivityWithAnimation(this@SplashActivity,ActivityLogin::class.java,findViewById(R.id.vview),true,R.color.app_orange)
////                    }else {
////                        Utils.startActivityWithAnimation(this@SplashActivity,AppIntroActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
////                    }
////                }else {
////                    Utils.startActivityWithAnimation(this@SplashActivity,ActivityLogin::class.java,findViewById(R.id.vview),true,R.color.app_orange)
////                }
////
////            }else {
////                if(Prefs.getBoolean("isLogin",false)) {
////                    grantCurrentUser(currentUserObjectId)
////
////                }else {
////                    Utils.startActivityWithAnimation(this@SplashActivity,ActivityLogin::class.java,findViewById(R.id.vview),true,R.color.app_orange)
////                }
////            }
//        }
//    }


    private fun CheckMapPermission() {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {

            if (ActivityCompat.checkSelfPermission(this@SplashActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@SplashActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@SplashActivity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.READ_CONTACTS), 1002)
            } else {


                playVideo()
            }
        } else {
            playVideo()
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1002 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this@SplashActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        playVideo()
                    }
                } else {

                    Toast.makeText(this@SplashActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
                    //finish();
                }
            }
        }
    }

    private fun grantCurrentUser(userId: String) {

        Backendless.UserService.findById(userId, object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {

                if (fault?.message?.contains("Unable to resolve host “api.backendless.com”: No address associated with hostname")!!) {

                    Utils.showAlertDialogWithFinish(this@SplashActivity, "Connectivity Error", "It seems internet connection is down. Please connect your phone with network and try again")

                } else {
                    Toast.makeText(this@SplashActivity, "Something went wrong..", Toast.LENGTH_LONG).show()

                }
            }

            override fun handleResponse(response: BackendlessUser?) {

                Backendless.UserService.setCurrentUser(response)
                HawkUtils.putHawk("BackendlessUser", response)

                if (Prefs.getBoolean("isLogin", false)) {

                    startActivity(Intent(this@SplashActivity, NeedASeatActivity::class.java))
                    finish()

//                    if(Prefs.getBoolean("isLive",false) || Prefs.getBoolean("inMeeting",false)){
//                        startActivity(Intent(this@SplashActivity,NeedASeatActivity::class.java))
//                        finish()
//                        //Utils.startActivityWithAnimation(this@SplashActivity,ActivityLogin::class.java,findViewById(R.id.vview),true,R.color.app_orange)
//                    }
//                    Utils.startActivityWithAnimation(this@SplashActivity,AppIntroActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
                } else {
                    startActivity(Intent(this@SplashActivity, NDLoginSingupWithActivitiy::class.java))
                    //Utils.startActivityWithAnimation(this@SplashActivity,NDLoginSingupWithActivitiy::class.java,findViewById(R.id.vview),true,R.color.app_orange)
                }

                //Utils.startActivityWithAnimation(this@SplashActivity,MainActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
            }

        })

    }

    fun printKeyHash(context: Activity): String? {
        val packageInfo: PackageInfo
        var key: String? = null
        try {
            //getting application package name, as defined in manifest
            val packageName = context.applicationContext.packageName

            //Retriving package info
            packageInfo = context.packageManager.getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES)

            Log.e("Package Name=", context.applicationContext.packageName)

            for (signature in packageInfo.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                key = String(Base64.encode(md.digest(), 0))

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key)
            }
        } catch (e1: PackageManager.NameNotFoundException) {
            Log.e("Name not found", e1.toString())
        } catch (e: NoSuchAlgorithmException) {
            Log.e("No such an algorithm", e.toString())
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }

        return key
    }


    fun findSeat(seatId: String?, context: Context) {


        Backendless.Data.of(Post::class.java).findById(seatId, object : AsyncCallback<Post> {
            override fun handleFault(fault: BackendlessFault?) {

                Log.d("TAG", fault?.message)
                Toast.makeText(context!!, fault?.message, Toast.LENGTH_LONG).show()

            }

            override fun handleResponse(post: Post) {

                post.shouldGoLive = "no"
                Backendless.Persistence.save(post, object : AsyncCallback<Post> {

                    override fun handleFault(fault: BackendlessFault?) {
                        Toast.makeText(context!!, fault?.message, Toast.LENGTH_LONG).show()
                    }

                    override fun handleResponse(response: Post?) {
                        Toast.makeText(context!!, "Seat status updated", Toast.LENGTH_LONG).show()

                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                })

            }
        })
    }


    private fun getBackendLessUsr(userId: String) {

        Backendless.UserService.findById(userId, object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {
                Toast.makeText(this@SplashActivity, "Something went wrong.." + fault?.message, Toast.LENGTH_LONG).show()
            }

            override fun handleResponse(response: BackendlessUser?) {

                Backendless.UserService.setCurrentUser(response)
                HawkUtils.putHawk("BackendlessUser", response)

                if (Prefs.getBoolean("isLogin", false)) {

                    if (Prefs.getBoolean("isLive", false) || Prefs.getBoolean("inMeeting", false)) {
                        startActivity(Intent(this@SplashActivity, NeedASeatActivity::class.java))
                        finish()
                        //Utils.startActivityWithAnimation(this@SplashActivity,ActivityLogin::class.java,findViewById(R.id.vview),true,R.color.app_orange)
                    }
                    Utils.startActivityWithAnimation(this@SplashActivity, AppIntroActivity::class.java, findViewById(R.id.vview), true, R.color.app_orange)
                } else {
                    Utils.startActivityWithAnimation(this@SplashActivity, NDLoginSingupWithActivitiy::class.java, findViewById(R.id.vview), true, R.color.app_orange)
                }

                //Utils.startActivityWithAnimation(this@SplashActivity,MainActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
            }

        })

    }


    private fun playVideo() {

        Handler().postDelayed({

            if (Prefs.getBoolean("isLogin", false)) {
                val currentUserObjectId = Prefs.getString("userId", "-1")
                if (Backendless.UserService.CurrentUser() == null) {

                    if (currentUserObjectId.equals("-1")) {
                        //Utils.startActivityWithAnimation(this@SplashActivity,NDLoginSingupWithActivitiy::class.java,findViewById(R.id.vview),true,R.color.app_orange)
                        startActivity(Intent(this@SplashActivity, NDLoginSingupWithActivitiy::class.java))
                        finish()


                    } else {

                        if (!Utils.isConnectedOnline(this)) {

                            Utils.showAlertDialogWithFinish(this@SplashActivity, "Connectivity Error", "It seems internet connection is down. Please connect your phone with network and try again")
                        } else {
                            grantCurrentUser(currentUserObjectId)

                        }

                    }
                } else {

                    if (Prefs.getBoolean("isLive", false) || Prefs.getBoolean("inMeeting", false)) {
                        startActivity(Intent(this@SplashActivity, NeedASeatActivity::class.java))
                        finish()
                        //Utils.startActivityWithAnimation(this@SplashActivity,ActivityLogin::class.java,findViewById(R.id.vview),true,R.color.app_orange)
                    } else {
                        // Utils.startActivityWithAnimation(this@SplashActivity,AppIntroActivity::class.java,findViewById(R.id.vview),true,R.color.app_orange)
                        startActivity(Intent(this@SplashActivity, NeedASeatActivity::class.java))
                        finish()

                    }

                }
            } else {
                //Utils.startActivityWithAnimation(this@SplashActivity,NDLoginSingupWithActivitiy::class.java,findViewById(R.id.vview),true,R.color.app_orange)
                startActivity(Intent(this@SplashActivity, NDLoginSingupWithActivitiy::class.java))
                finish()


            }

        }, 1000)
    }


}
