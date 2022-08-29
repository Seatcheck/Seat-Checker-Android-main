package app.rubbickcube.seatcheck.activities

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import app.invision.morse.api.ApiUtils
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.api.EndPointsConstants
import app.rubbickcube.seatcheck.model.ChatUserList
import app.rubbickcube.seatcheck.model.SucessResponse
import app.yasirameen.life.adapter.ChatUserListAdapter
import com.backendless.BackendlessUser
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.kaopiz.kprogresshud.KProgressHUD
import com.pixplicity.easyprefs.library.Prefs
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_chat_list.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatListActivity : AppCompatActivity() {


    val options = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    public var chatUserList: MutableList<ChatUserList>? = null
    private var chatuserAdapter: ChatUserListAdapter? = null
    private var manager: LinearLayoutManager? = null
    private var chatUserListAdapter: ChatUserListAdapter? = null
    private var onNewMessage: Emitter.Listener? = null
    private var mSocket: Socket? = null
    var backendlessUser: BackendlessUser = BackendlessUser()


    private var pd: KProgressHUD? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

        val ab = supportActionBar
        ab?.hide()

        if (Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)

        } else {
            return
        }

        appbar_title.text = "Inbox"
        Glide.with(this@ChatListActivity).load(backendlessUser.properties["profileImage"].toString()).apply(options).into(appbar_img)
        appbar_back.setOnClickListener {
            finish()
        }


//        val app = application as Application
//        mSocket = app.socket
//        listenOnNewMessage()

        intializeAdapter()
    }


    private fun getUserChatList(senderId: String) {

        val mService = ApiUtils.getSOService()
        //val token = "bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOjMxMSwiaXNzIjoiaHR0cDpcL1wvZWMyLTM1LTE2MS0yNTUtMjA0LnVzLXdlc3QtMi5jb21wdXRlLmFtYXpvbmF3cy5jb21cL2FwaVwvYXV0aFwvdmVyaWZ5IiwiaWF0IjoxNTA5NzkzMjE1LCJleHAiOjE1MjQ5MTMyMTUsIm5iZiI6MTUwOTc5MzIxNSwianRpIjoiMjU0YzFhOGQzN2JmYzkzYmZkOTcxMjkyYjY5NWZlYTUifQ.TACsQjIDVXjqk5CHhFZY1Lpatzk0eVM5waLKSc5BXEE"
        val url = EndPointsConstants.CHAT_LIST

        pd = Utils.SCProgressDialog(this@ChatListActivity, null, "Please wait")
        pd?.show()
        mService.getUserChatList(url, Utils.getSimpleTextBody(senderId))
                .enqueue(object : Callback<Array<ChatUserList>> {

                    override fun onResponse(call: Call<Array<ChatUserList>>?, response: Response<Array<ChatUserList>>?) {


                        pd?.dismiss()
                        chatUserList = arrayListOf()

                        if (response?.isSuccessful!!) {
                            val chats = response?.body()

                            for (items in chats.indices) {
                                chatUserList!!.add(chats.get(items))
                            }
                            chatUserListAdapter?.setChatUsers(chatUserList!!)

                        } else {
                            Log.d("failure", response?.message())
                        }
                    }

                    override fun onFailure(call: Call<Array<ChatUserList>>?, t: Throwable?) {

                        pd?.dismiss()

                        Log.d("Server errro", t?.message)

                    }


                })
    }


    private fun intializeAdapter() {

        chatUserListAdapter = ChatUserListAdapter(this@ChatListActivity)
        rv_chat_list?.adapter = chatUserListAdapter
        rv_chat_list.isLongClickable = true
        // getUserChatList("0DD8C034-7E1A-23EE-FFC0-7919921E4400")
        getUserChatList(Prefs.getString("userId", ""))

        rv_chat_list?.onItemLongClickListener = object : AdapterView.OnItemLongClickListener {
            override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {


                val item = chatUserListAdapter?.getChatUserListItem(position)

                showChatDeleteIcoon(item?.name, backendlessUser.objectId, item?.seatcheckUserId)
                return true
            }

        }

        rv_chat_list?.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            }

        }
//        rv_chat_list.setOnItemClickListener{ adapterView, view, i, l ->
//
//
//            val chatUser = chatUserList?.get(i)
//            val intent = Intent(this@InboxActivity, ChatActivity::class.java)
//            intent.putExtra("userId",chatUser?.id.toString())
//            intent.putExtra("name",chatUser?.name)
//            intent.putExtra("userdp",chatUser?.profile_pic)
//            startActivity(intent)
//
//
//
//
//        }
        //getUserChatList()
    }

    override fun onResume() {
        super.onResume()


        if (chatUserList != null) {

            chatUserList?.clear()
            intializeAdapter()
        }

//        if(mSocket != null) {
//            mSocket?.on("user-global-${UserUtils.getUserId()}:list_msg",onNewMessage)
//        }
//
//        if(chatUserListAdapter != null) {
//            getUserChatList()
//
//        }

    }

    override fun onDestroy() {
        super.onDestroy()
//        if(mSocket != null) {
//            mSocket?.off("user-global-${UserUtils.getUserId()}:list_msg",onNewMessage)
//
//        }
    }

    override fun onPause() {
        super.onPause()

//        if(mSocket != null) {
//            mSocket?.off("user-global-${UserUtils.getUserId()}:list_msg",onNewMessage)
//        }
    }

//    private fun listenOnNewMessage() {
//
//        onNewMessage = Emitter.Listener { args ->
//
//            runOnUiThread {
//
//                try {
//                    val jObject = JSONObject(args[0].toString())
//                    if(jObject.getBoolean("success")) {
//                        val chatUser = GsonUtils.fromJSON(jObject.getJSONObject("data"),ChatUserList::class.java)
//                        chatUserListAdapter?.updateUser(chatUser)
//                        Log.d("data",jObject.toString())
//                    }
//                }catch (e: JSONException) {
//
//                    e.printStackTrace()
//                }
//
//
//            }
//
//        }
//    }


    private fun deleteChat(senderId: String, recieverId: String) {

        val mService = ApiUtils.getSOService()
        //val token = "bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOjMxMSwiaXNzIjoiaHR0cDpcL1wvZWMyLTM1LTE2MS0yNTUtMjA0LnVzLXdlc3QtMi5jb21wdXRlLmFtYXpvbmF3cy5jb21cL2FwaVwvYXV0aFwvdmVyaWZ5IiwiaWF0IjoxNTA5NzkzMjE1LCJleHAiOjE1MjQ5MTMyMTUsIm5iZiI6MTUwOTc5MzIxNSwianRpIjoiMjU0YzFhOGQzN2JmYzkzYmZkOTcxMjkyYjY5NWZlYTUifQ.TACsQjIDVXjqk5CHhFZY1Lpatzk0eVM5waLKSc5BXEE"
        val url = EndPointsConstants.CHAT_LIST

        pd = Utils.SCProgressDialog(this@ChatListActivity, null, "Deleting conversation...")
        pd?.show()
        mService.deleteChat(url, Utils.getSimpleTextBody(senderId), Utils.getSimpleTextBody(recieverId))
                .enqueue(object : Callback<SucessResponse> {
                    override fun onFailure(call: Call<SucessResponse>?, t: Throwable?) {

                        pd?.dismiss()

                    }

                    override fun onResponse(call: Call<SucessResponse>?, response: Response<SucessResponse>?) {


                        pd?.dismiss()

                        if (chatUserList != null) {

                            chatUserList?.clear()
                            getUserChatList(Prefs.getString("userId", ""))

                        }

                    }

                })

    }


    private fun showChatDeleteIcoon(name: String?, sender: String?, reciever: String?) {
        val builder = AlertDialog.Builder(this@ChatListActivity)
        builder.setMessage("Deleting chat will also remove messages from $name")
                .setTitle("Confirm!")

        builder.setPositiveButton("Delete", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {

                deleteChat(sender!!, reciever!!)
                p0?.dismiss()


            }

        }).setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {

                p0?.dismiss()
            }

        })

        val dialog = builder.create()
        dialog.show()
    }

}
