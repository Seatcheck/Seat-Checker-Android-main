package app.yasirameen.life.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.activities.ChatActivity
import app.rubbickcube.seatcheck.activities.OtherUserProfileActivity
import app.rubbickcube.seatcheck.model.ChatUser
import app.rubbickcube.seatcheck.model.ChatUserList
import app.rubbickcube.seatcheck.model.Users
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pixplicity.easyprefs.library.Prefs
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.app_bar_layout.*
import java.util.ArrayList

/**
 * Created by YASIR on 11/12/2017.
 */
class PeopleAroundMeAdapter(private var context: Context) : BaseAdapter() {


    val options =  RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    private var inflater: LayoutInflater? = null
    internal var nearUserList: MutableList<ChatUser> = ArrayList()

       init {
        inflater = LayoutInflater.from(context)
    }

    override fun getView(position: Int, convertview: View?, p2: ViewGroup?): View {

       val item = nearUserList.get(position)

        var convertview = convertview
        val holder: PeopleAroundMeAdapter.ViewHolder
        if (convertview == null) {

            convertview = inflater!!.inflate(R.layout.people_list_item, null)
            holder = ViewHolder()

            holder._cv = convertview.findViewById<View>(R.id.user_img) as CircleImageView
            holder._title = convertview.findViewById<View>(R.id.txt_username) as TextView
            holder._nd_chat_icon = convertview.findViewById<View>(R.id.nd_chat_icon) as ImageView
            holder._status = convertview.findViewById<View>(R.id.txt_status) as TextView
            holder._minsago = convertview.findViewById<View>(R.id.mins_ago) as TextView
            holder._rel = convertview.findViewById<View>(R.id.rel) as RelativeLayout

            convertview.tag = holder

        } else {
            holder = convertview.tag as ViewHolder
        }




        holder?._status?.text = item.email
        Glide.with(context).load(item.profilePic).apply(options).into(holder?._cv!!)

        holder?._title?.text = item.name
       // holder?._minsago?.text = item.timeDifference


        holder._nd_chat_icon?.setOnClickListener {


            val intent = Intent(context,ChatActivity::class.java)
            intent.putExtra("sender_id", Prefs.getString("userId",""))
            intent.putExtra("receiver_id",item.seatcheckUserId)
            intent.putExtra("chat_receiver_id",Prefs.getInt("chat_user_id",-1))
            intent.putExtra("user_dp",item.profilePic)
            intent.putExtra("fcmToken",item.deviceId)
            intent.putExtra("name",item.name)
           context.startActivity(intent)
        }

        holder?._cv?.setOnClickListener {
            val intent = Intent(context, OtherUserProfileActivity::class.java)
            intent.putExtra("otherUserProfileImage", item.profilePic)
            intent.putExtra("otherUserId", item.seatcheckUserId)
            intent.putExtra("otherUserName",item.name)
            context.startActivity(intent)

        }

        return convertview!!

    }

    override fun getItem(p0: Int): Any {
        return nearUserList.size
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return nearUserList.size

    }
    fun setNearUser(users: MutableList<ChatUser>) {

        this.nearUserList = users
        notifyDataSetChanged()
    }


    inner class ViewHolder {

        internal var _cv: CircleImageView? = null
        internal var _title: TextView? = null
        internal var _nd_chat_icon: ImageView? = null
        internal var _status: TextView? = null
        internal var _minsago: TextView? = null
        internal var _rel: RelativeLayout? = null


    }


}