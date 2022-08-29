package app.rubbickcube.seatcheck.activities

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import app.invision.morse.api.ApiUtils
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.AudioPlayer
import app.rubbickcube.seatcheck.GsonUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.api.EndPointsConstants
import app.rubbickcube.seatcheck.model.Chats
import app.rubbickcube.seatcheck.model.Contacts
import app.rubbickcube.seatcheck.model.SocketResponse
import app.rubbickcube.seatcheck.model.SucessResponse
import app.yasirameen.life.adapter.ChatAdapter
import com.backendless.Backendless
import com.backendless.BackendlessCollection
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.BackendlessDataQuery
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.kaopiz.kprogresshud.KProgressHUD
import com.pixplicity.easyprefs.library.Prefs
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {

    val options = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    internal var chatAdapter: ChatAdapter? = null
    internal var manager: LinearLayoutManager? = null
    internal var chatList: MutableList<Chats>? = ArrayList<Chats>()
    internal var tempChatList: MutableList<Chats>? = ArrayList<Chats>()
    private var mSocket: Socket? = null
    private var onUserTyping: Emitter.Listener? = null
    private var onUserStopTyping: Emitter.Listener? = null
    private var onMessageRecieved: Emitter.Listener? = null
    private var onMessageRecievedSocket: Emitter.Listener? = null
    private var onUserStatus: Emitter.Listener? = null
    private var onMessageRead: Emitter.Listener? = null
    private var onReadAll: Emitter.Listener? = null
    private var userId: String? = null //Seatcheck_object id which is in amazon
    private var userId_str: String? = null //Seatcheck_object id which is in amazon
    private var username: String? = null
    private var userphone: String? = null

    // private val token = "bearer "+getTempToken()
    private val token = "bearer " + Prefs.getString("token", "")
    private var mTyping = false
    private var isOnline: Boolean? = false
    private var lastSeen: String? = null
    private var appUserIdofHim: Int? = null
    private val mTypingHandler = Handler()
    private var currentPageURl: String? = ""
    private var sender_id: String? = ""
    private var receiver_id: String? = ""
    private var recieverName: String? = ""
    private var randomId: String? = ""
    private var pd: KProgressHUD? = null
    private var user_dp: String? = null
    private var fcmToken: String? = null
    private var contacts: MutableList<Contacts>? = null
    private var contactsBackendlessUser: MutableList<BackendlessUser>? = null

    var backendlessUser: BackendlessUser = BackendlessUser()


    val myUserId = Prefs.getInt("chat_user_id", -1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val ab = supportActionBar
        ab?.hide()

        if (Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
            getContacts(backendlessUser.objectId)

        } else {
            return
        }

        init()
    }


    private fun init() {

        mSocket = IO.socket(EndPointsConstants.SOCKET_BASE_URL)
        mSocket?.on("connect", object : Emitter.Listener {
            override fun call(vararg args: Any?) {

                runOnUiThread {
                    //  Toast.makeText(this@ChatActivity,"Socket Connected to server!",Toast.LENGTH_LONG).show()

                }

            }

        })

        mSocket?.connect()
        listenRecievedMessageEvent()
        listUserTypingEvent()

        sender_id = intent.getStringExtra("sender_id")
        receiver_id = intent.getStringExtra("receiver_id")
        recieverName = intent.getStringExtra("name")
        user_dp = intent.getStringExtra("user_dp")
        fcmToken = intent.getStringExtra("fcmToken")
        userId = intent.getIntExtra("chat_receiver_id", 0).toString()
        if (intent.hasExtra("chat_receiver_id_str")) {
            userId_str = intent.getStringExtra("chat_receiver_id_str")
        }

        appbar_title.text = recieverName
        Glide.with(this@ChatActivity).load(R.drawable.add_contats).apply(options).into(appbar_img)

        appbar_back.setOnClickListener {
            finish()
        }

        appbar_img.setOnClickListener {

            grantCurrentUser(receiver_id!!)
        }

        chatList = ArrayList()
        intializeAdapter(userId, user_dp)


        tv_sendMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {


                val typingOjbect = JSONObject()
                typingOjbect.put("name", recieverName)
                typingOjbect.put("sender_id", sender_id)
                typingOjbect.put("reciever_id", userId)
                mSocket?.emit("userTyping", typingOjbect)


            }


        })

        btn_sendButton.setOnClickListener {

            if (!tv_sendMessage.text.isEmpty()) {
                sendMessageRequest(Prefs.getInt("chat_user_id", -1), 1, tv_sendMessage.text.toString())
                tv_sendMessage.setText("")
            }
        }

    }


    private fun intializeAdapter(userId: String?, userdp: String?) {

        chatAdapter = ChatAdapter(this@ChatActivity, userId, userdp)
        rv_chat?.adapter = chatAdapter

        manager = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        rv_chat?.layoutManager = manager

        getChats(sender_id!!, receiver_id!!)
    }

    fun getChats(senderId: String, recieverid: String) {

        val mService = ApiUtils.getSOService()
        val url = EndPointsConstants.CHAT_VIEW

        pd = Utils.SCProgressDialog(this@ChatActivity, null, "Please wait")
        pd?.show()
        mService.getChats(url, Utils.getSimpleTextBody(senderId), Utils.getSimpleTextBody(recieverid))
                .enqueue(object : Callback<Array<Chats>> {
                    override fun onFailure(call: Call<Array<Chats>>?, t: Throwable?) {

                        try {
                            pd?.dismiss()
                            Log.d("error", t?.message)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onResponse(call: Call<Array<Chats>>?, response: Response<Array<Chats>>?) {


                        pd?.dismiss()

                        try {

                            if (response!!.isSuccessful) {

                                val chatResponse = response.body()
                                chatList = ArrayList()

                                for (i in chatResponse.indices) {

                                    chatList!!.add(chatResponse[i])
                                }//mine:6013 | Yasir bhai: 6014
                                updateUserId(chatList!!)
                                chatAdapter?.setMessages(chatList!!, true)
                                rv_chat?.scrollToPosition(chatAdapter!!.itemCount - 1)

                                Log.d("Chatlist", chatList.toString())
                                // fetchUserStatus(userId)


                            } else {

                                Toast.makeText(this@ChatActivity, response.message(), Toast.LENGTH_LONG).show()
                                Log.d("Failure Response", "" + response.message())

                            }
                        } catch (e: Exception) {

                            e.printStackTrace()
                        }
                    }
                })
    }

    private fun updateUserId(list: List<Chats>) {
        if (userId_str != null && userId_str!!.length > 0) {
            for (i in list.indices) {
                if (list[i].getUser()?.seatcheckUserId.equals(userId_str)) {
                    chatAdapter?.updateUserId((list[i].getUser()?.id).toString())
                    userId = (list[i].getUser()?.id).toString()
                    return
                }
            }
        }
    }

    fun getIOSTime(): String {
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        return sdf.format(c.time)
    }

    fun sendMessageRequest(sender: Int?, reciever: Int?, message: String?) {

        val mService = ApiUtils.getSOService()
        val url = EndPointsConstants.SEND_CHAT_MESSAGE
        val singleChatMessage = Chats()
        singleChatMessage.setId(1)
        singleChatMessage.setSeatcheckUserId(Prefs.getString("userId", ""))
        singleChatMessage.setMessage(message!!)
        singleChatMessage.setReceiver(reciever)
        singleChatMessage.setSender(sender)
        singleChatMessage.setIosTime(getIOSTime())
        singleChatMessage.setTime(currentMessageDateTime())
        singleChatMessage.setIsRead(0)

        chatAdapter?.setSingleMessage(singleChatMessage)
        rv_chat?.scrollToPosition(chatAdapter!!.itemCount - 1)

        mService.sendchatMessage(url, Utils.getSimpleTextBody(sender_id!!), Utils.getSimpleTextBody(receiver_id!!), Utils.getSimpleTextBody(message), Utils.getSimpleTextBody(singleChatMessage.getIosTime()!!))
                .enqueue(object : Callback<SucessResponse> {
                    override fun onFailure(call: Call<SucessResponse>?, t: Throwable?) {

                        Toast.makeText(this@ChatActivity, t?.message, Toast.LENGTH_LONG).show()
                        Log.d("Server", "server error")
                    }

                    override fun onResponse(call: Call<SucessResponse>?, response: Response<SucessResponse>?) {

                        try {

                            if (response?.isSuccessful!!) {


                                Log.d("message", response.message())
                                var name = backendlessUser.properties["name"].toString()
                                val fcmToken = fcmToken
                                Utils.sentNotification(this@ChatActivity, name, message, fcmToken!!, "OPEN_ACTIVITY_4")

                                //  chatAdapter?.updateMessageId(randomId!!,response.body().message?.id)

                            } else {
                                val code = response.code()
                                Log.d("Server", "server error " + code)

                            }

                        } catch (e: Exception) {

                            e.printStackTrace()
                        }
                    }

                })

    }


    fun currentMessageDateTime(): String {

        var monthStr = ""
        var currentTIme = ""
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val monthNum = c.get(Calendar.MONTH)
        val currentday = c.get(Calendar.DAY_OF_MONTH)
        val sdf = SimpleDateFormat("hh:mm a")
        sdf.format(c.time)
        currentTIme = sdf.format(c.time)

        when (monthNum + 1) {
            1 -> monthStr = "Jan"
            2 -> monthStr = "Feb"
            3 -> monthStr = "Mar"
            4 -> monthStr = "Apr"
            5 -> monthStr = "May"
            6 -> monthStr = "June"
            7 -> monthStr = "July"
            8 -> monthStr = "Aug"
            9 -> monthStr = "Sep"
            10 -> monthStr = "Oct"
            11 -> monthStr = "Nov"
            12 -> monthStr = "Dec"
            else -> {
            }
        }

        /* return monthStr + " " + currentday + ", " +
                 "" + currentTIme*/

        return currentTIme

    }


    override fun onResume() {
        super.onResume()



        AppClass.chatActive = true
        if (mSocket != null) {
            // mSocket?.on("userid-0DD8C034-7E1A-23EE-FFC0-7919921E4400:send_msg",onMessageRecieved)
            mSocket?.on("userid-$myUserId:send_msg", onMessageRecieved)
            mSocket?.on("userid-$myUserId:send_msg", onMessageRecievedSocket)
            mSocket?.on("userid-$myUserId:typing", onUserTyping)

//            mSocket?.on("user-"+UserUtils.getUserId()+":typing",onUserTyping)
//            mSocket?.on("user-status:"+userId+"",onUserStatus)
//            mSocket?.on("user-global-${userId}:read",onMessageRead)
//            mSocket?.on("user-global:read_all",onReadAll)
            //mSocket?.connect()

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        AppClass.chatActive = false

        if (mSocket != null) {
            // mSocket?.off("userid-0DD8C034-7E1A-23EE-FFC0-7919921E4400:send_msg",onMessageRecieved)

            mSocket?.off("userid-$myUserId:send_msg", onMessageRecieved)
            mSocket?.off("userid-$myUserId:send_msg", onMessageRecievedSocket)
            mSocket?.off("userid-$myUserId:typing", onUserTyping)

//            mSocket?.off("user-"+UserUtils.getUserId()+":typing",onUserTyping)
//            mSocket?.off("user-status:"+userId+"",onUserStatus)
//            mSocket?.off("user-global-${userId}:read",onMessageRead)
//            mSocket?.off("user-global:read_all",onReadAll)
            mSocket?.disconnect()


        }
    }

    override fun onPause() {
        super.onPause()

        AppClass.chatActive = false

        if (mSocket != null) {

            // mSocket?.off("userid-0DD8C034-7E1A-23EE-FFC0-7919921E4400:send_msg",onMessageRecieved)
            mSocket?.off("userid-$myUserId:send_msg", onMessageRecieved)
            mSocket?.off("userid-$myUserId:send_msg", onMessageRecievedSocket)
            mSocket?.off("userid-$myUserId:typing", onUserTyping)

//            mSocket?.off("user-"+UserUtils.getUserId()+":typing",onUserTyping)
//            mSocket?.off("user-status:"+userId+"",onUserStatus)
//            mSocket?.off("user-global-${userId}:read",onMessageRead)
//            mSocket?.off("user-global:read_all",onReadAll)

        }
    }

    private fun listenRecievedMessageEvent() {

        onMessageRecieved = Emitter.Listener { args ->
            runOnUiThread {
                try {
                    val _chat = Chats()
                    val jsonObject = JSONObject(args[0].toString())
                    val socketResponse = GsonUtils.fromJSON(jsonObject, SocketResponse::class.java) as SocketResponse
                    val message = jsonObject.getJSONObject("message")
                    if (!Prefs.getString("userId", "").equals(socketResponse.user.seatcheckUserId)) {
                        _chat.setId(socketResponse.user.id!!)
                        _chat.setSeatcheckUserId(socketResponse.user.seatcheckUserId)
                        _chat.setIosTime(getIOSTime())
                        _chat.setSender(socketResponse.message.sender)
                        _chat.setReceiver(socketResponse.message.receiver)
                        _chat.setMessage(socketResponse.message.message)
                        _chat.setTime(socketResponse.time)
                        user_dp = socketResponse.user.profilePic
                        chatAdapter?.setSingleMessage(_chat)
                        rv_chat?.scrollToPosition(chatAdapter!!.itemCount - 1)
                        AudioPlayer().play(this@ChatActivity, "tick")

                    }
                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }
        }

        onMessageRecievedSocket = Emitter.Listener { args ->
            runOnUiThread {
                try {
                    val _chat = Chats()
                    val jsonObject = JSONObject(args[0].toString())
                    val socketResponse = GsonUtils.fromJSON(jsonObject, SocketResponse::class.java) as SocketResponse
                    val message = jsonObject.getJSONObject("message")
                    if (Prefs.getString("userId", "").equals(socketResponse.user.seatcheckUserId)) {
                        _chat.setId(socketResponse.user.id!!)
                        _chat.setSeatcheckUserId(socketResponse.user.seatcheckUserId)
                        _chat.setIosTime(getIOSTime())
                        _chat.setSender(socketResponse.message.sender)
                        _chat.setReceiver(socketResponse.message.receiver)
                        _chat.setMessage(socketResponse.message.message)
                        _chat.setTime(socketResponse.time)
                        user_dp = socketResponse.user.profilePic
                        chatAdapter?.setSingleMessage(_chat)
                        rv_chat?.scrollToPosition(chatAdapter!!.itemCount - 1)
                    }

                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }
        }

    }


    private val onTypingTimeout = Runnable {


//        nd_typing_pencil.visibility = View.GONE
//        nd_user_typing.visibility = View.GONE
    }


    private fun listUserTypingEvent() {

        onUserTyping = Emitter.Listener { args ->


            runOnUiThread {

                val userTypingObject = JSONObject(args[0].toString())
                val message = userTypingObject.getJSONObject("message")

                val name = message.getString("name")


//                    nd_typing_pencil.visibility = View.VISIBLE
//                nd_user_typing.visibility = View.VISIBLE
//
//                nd_user_typing.text = "typing..."

                mTypingHandler.removeCallbacks(onTypingTimeout)
                mTypingHandler.postDelayed(onTypingTimeout, 1000)


            }
        }


        /* val mHomeWatcher =  HomeWatcher(this)
         mHomeWatcher.setOnHomePressedListener(object: OnHomePressedListener {
             override fun onHomePressed() {
                 mSocket?.disconnect()
                 finish()


             }

             override fun onHomeLongPressed() {
             }


         })
         mHomeWatcher.startWatch()*/

    }

    private fun addToContacts() {


    }


    private fun getContacts(ownerId: String) {


        //val whereClause = "ownerId = '$ownerId'"
        val ids = ownerId
        //val ids = "38E60C1A-17D6-48E6-FF64-88F469BBE200"
        val whereClause = "ownerId = '$ids'"

        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause

        Backendless.Persistence.of<Contacts>(Contacts::class.java).find(dataQuery, object : AsyncCallback<BackendlessCollection<Contacts>> {
            override fun handleFault(fault: BackendlessFault?) {

                ///Toast.makeText(this@ActivityContacts,fault?.message, Toast.LENGTH_LONG).show()
                pd?.dismiss()
                Log.d("TAG", fault?.message)

            }

            override fun handleResponse(response: BackendlessCollection<Contacts>?) {


                contacts = response?.data as MutableList<Contacts>?
                // Log.d("sdfa","sad")


            }
        })

    }

    private fun grantCurrentUser(userId: String) {


        pd = Utils.SCProgressDialog(this@ChatActivity, null, "Adding to your contacts...")
        pd?.show()

        Backendless.UserService.findById(userId, object : AsyncCallback<BackendlessUser> {
            override fun handleFault(fault: BackendlessFault?) {


                pd?.dismiss()
                Toast.makeText(this@ChatActivity, fault?.message, Toast.LENGTH_LONG)
            }

            override fun handleResponse(response: BackendlessUser?) {


                if (contacts?.size != 0) {

                    contacts!![0].contacts.add(response)


                    Backendless.Persistence.save(contacts!![0], object : AsyncCallback<Contacts> {
                        override fun handleFault(fault: BackendlessFault?) {

                            pd?.dismiss()
                            Toast.makeText(this@ChatActivity, fault?.message, Toast.LENGTH_LONG)
                        }

                        override fun handleResponse(response: Contacts?) {

                            Toast.makeText(this@ChatActivity, "contacts successfully added", Toast.LENGTH_LONG)

                            pd?.dismiss()
                        }

                    })

                } else {

                    val contact = Contacts()
                    contact.user = backendlessUser
                    contactsBackendlessUser = arrayListOf()
                    contactsBackendlessUser?.add(response!!)
                    contact.contacts = contactsBackendlessUser as ArrayList<BackendlessUser>
                    contact.ownerId = backendlessUser.objectId

                    Backendless.Persistence.save(contact, object : AsyncCallback<Contacts> {
                        override fun handleFault(fault: BackendlessFault?) {

                            pd?.dismiss()
                            Toast.makeText(this@ChatActivity, fault?.message, Toast.LENGTH_LONG)
                        }

                        override fun handleResponse(response: Contacts?) {

                            Toast.makeText(this@ChatActivity, "contacts successfully added", Toast.LENGTH_LONG)

                            pd?.dismiss()
                        }

                    })

                }


            }

        })

    }


}
