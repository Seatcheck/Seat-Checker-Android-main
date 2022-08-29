package app.rubbickcube.seatcheck

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import androidx.multidex.MultiDex
import android.util.Log
import app.rubbickcube.seatcheck.Helpers.AppLifecycleHandler
import app.rubbickcube.seatcheck.api.EndPointsConstants
import app.rubbickcube.seatcheck.di.component.AppComponent
import app.rubbickcube.seatcheck.di.component.DaggerAppComponent
import app.rubbickcube.seatcheck.di.module.BackendLessModule
import app.rubbickcube.seatcheck.model.Availability
import app.rubbickcube.seatcheck.model.Invites

import com.pixplicity.easyprefs.library.Prefs

import app.rubbickcube.seatcheck.model.Post
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.FirebaseApp
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException


class AppClass : Application(), LifecycleDelegate {


     //var appComponent: AppComponent? = createAppComponent()

    companion object {

         var chatActive = false
        private val socketId = ""
        private var mSocket: Socket? = null
        private val handler = Handler()
        private val onUserOnline: Emitter.Listener? = null
        private val onUserOfline: Emitter.Listener? = null

        fun getSocket(): Socket? {
            return mSocket
        }
        var AvailableList: MutableList<Availability>? = arrayListOf()
        var postList: MutableList<Post>? = arrayListOf()
        var allPost: MutableList<Post>? = arrayListOf()
        var invites : Invites? = null
        var selectedPost : Post? = null
        var inMeetingInvite : Invites? = null
        var appIsInForground = false
        var requestForExtend = false
        var reviewForOwner = true
        var notificationFor = ""
        var title = ""
        var message = ""
        var objectId = ""
        var lat : Double? = null
        var lng : Double? = null
        var inviteCounter = 0
//        var appComponent: AppComponent? =  DaggerAppComponent
//                .builder()
//                .backendLessModule(BackendLessModule())
//                .build()


    }


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)

    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)



        val lifeCycleHandler = AppLifecycleHandler(this)
        registerLifecycleHandler(lifeCycleHandler)

        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()

        run {
            try {
                mSocket = IO.socket(EndPointsConstants.SOCKET_BASE_URL)
                //mSocket = IO.socket(Prefs.getString("socketUrl",""));
            } catch (e: URISyntaxException) {
                throw RuntimeException(e)
            }
        }


        var token = FirebaseInstanceId.getInstance().token
        Prefs.putString("fcmToken",token)
        Log.d("FCM-Token","Token : "+token)

    }


    override fun onAppBackgrounded() {
        appIsInForground = false
        Log.d("Awww", "App in background")
    }

    override fun onAppForegrounded() {
        appIsInForground = true
        Log.d("Awww", "App in foreground")
    }

    private fun registerLifecycleHandler(lifeCycleHandler: AppLifecycleHandler) {
        registerActivityLifecycleCallbacks(lifeCycleHandler)
        registerComponentCallbacks(lifeCycleHandler)
    }




//     fun createAppComponent() : AppComponent {
//
//        return DaggerAppComponent
//                .builder()
//                .backendLessModule(BackendLessModule())
//                .build()
//    }




}
