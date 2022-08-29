package app.rubbickcube.seatcheck.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.rubbickcube.seatcheck.JavaUtilSeatCheck
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.model.Chats
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView
import net.cachapa.expandablelayout.ExpandableLayout
import java.text.SimpleDateFormat
import java.util.*


class ChatUpdatedAdapter(internal var context: Context, private var recieverId: String?, private var user_dp: String?) : ListAdapter<Chats, RecyclerView.ViewHolder>(DiffCallback()) {

    internal var inflater: LayoutInflater = LayoutInflater.from(context)
    internal var chatList: MutableList<Chats> = ArrayList()
    val options = RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var item_view: View?
        var holder: RecyclerView.ViewHolder? = null
        if (viewType == 0) {
            item_view = inflater.inflate(R.layout.chat_list_item_sender, parent, false)
            holder = SenderViewHolder(item_view)
        } else if (viewType == 1) {
            item_view = inflater.inflate(R.layout.chat_list_item_reciever, parent, false)
            holder = ReceiverViewHolder(item_view)
        }


        return holder!!
    }

    override fun getItemCount(): Int {
        return this.chatList?.size!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val chats = this.chatList!![position]
//                                    2020-10-11 13:08:18
//        2020-10-11 13:08:50
        if (holder is ReceiverViewHolder) {

            holder._tv_reciever_msg?.text = chats.getMessage()
            if (chats.getCreatedAt() != null && chats.getUpdatedAt() != null) {
                holder._tv_reciever_time?.text = getDateFromData(chats.getCreatedAt()!!)
            } else {
                if (chats.getTime() != null) {
                    holder._tv_reciever_time?.text = chats.getTime()
                } else {
                    holder._tv_reciever_time?.visibility = View.GONE
                }
            }
            Glide.with(context).load(user_dp).apply(options).into(holder._img_reciever_dp!!)

            holder._tv_reciever_msg?.setOnClickListener {

                holder?._time_expandLayout_reciever?.toggle()

            }

        } else if (holder is SenderViewHolder) {

            holder._tv_sender_msg?.text = chats.getMessage()


            if (chats.getIsRead() == 1) {

                Glide.with(context).load(R.drawable.chat_sent_fill).into(holder._img_waiting_clock!!)
                holder._tv_message_time?.text = getDateFromData(chats.getCreatedAt()!!)

            } else {
                Glide.with(context).load(R.drawable.chat_sent).into(holder._img_waiting_clock!!)
                holder._tv_message_time?.text = getDateFromData(chats.getTime()!!)


            }

            holder._tv_sender_msg?.setOnClickListener {

                holder._time_expandLayout?.toggle()


            }


        }

        /*else if(holder is MessagesTimeViewHolder) {

           var previousTs: Long? = 0
           if(position >= 1) {

               val PreviousChatMsg =  chatList.get(position - 1)
               previousTs = PreviousChatMsg.createdAt?.toLong()
           }
           val cal1 = Calendar.getInstance()
           val cal2 = Calendar.getInstance()
           cal1.timeInMillis = chatList.get(position).createdAt?.toLong()?.times(1000)!!
           cal2.timeInMillis = previousTs?.times(1000)!!
           //holder._group_msg_time_view
       }

*/
    }

    fun getDateFromData(date: String): String {
        try {
            val c = Calendar.getInstance().time
//        2020-10-11 13:08:50
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val d = sdf.parse(date)
            if (date.split(" ")[0].equals(df.format(c))) {
                return SimpleDateFormat("HH:mm a").format(d)
            } else {
                return SimpleDateFormat("dd MMM, yyyy at HH:mm a").format(d)
            }
        } catch (e: Exception) {
//            Mon Oct 12 13:32:02 GMT+05:00 2020
            return date
        }
    }

    override fun getItemViewType(position: Int): Int {
        val chatmessage = this.chatList!![position]
        return if (chatmessage.getSender() == recieverId?.toInt()) {
            1
        } else {
            0
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var _tv_reciever_msg: TextView? = null
        internal var _img_reciever_dp: CircleImageView? = null
        internal var _tv_reciever_time: TextView? = null
        internal var _tv_reciever_isseen: TextView? = null
        internal var _time_expandLayout_reciever: ExpandableLayout? = null


        init {

            _tv_reciever_msg = itemView.findViewById<View>(R.id.tv_reciever_msg) as TextView?
            _tv_reciever_time = itemView.findViewById<View>(R.id.tv_reciever_time) as TextView?
            _time_expandLayout_reciever = itemView.findViewById<View>(R.id.time_expandLayout_reciever) as ExpandableLayout?
            _img_reciever_dp = itemView.findViewById<View>(R.id.img_reciever_dp) as CircleImageView?

            //_tv_reciever_isseen = itemView.findViewById(R.id.tv_reciever_isseen) as TextView?

        }
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var _tv_sender_msg: TextView? = null
        internal var _tv_message_time: TextView? = null
        internal var _img_waiting_clock: ImageView? = null
        internal var _time_expandLayout: ExpandableLayout? = null


        init {

            _tv_sender_msg = itemView.findViewById<View>(R.id.tv_sender_msg) as TextView?
            _tv_message_time = itemView.findViewById<View>(R.id.tv_message_time) as TextView?
            _img_waiting_clock = itemView.findViewById<View>(R.id.img_status) as ImageView?
            _time_expandLayout = itemView.findViewById<View>(R.id.time_expandLayout) as ExpandableLayout?

        }
    }

    private fun doBubbleSortLogic() {

    }

    fun setMessages(messages: MutableList<Chats>) {
        this.chatList = messages
        chatList = JavaUtilSeatCheck.doBubbleSortLogic(chatList.toTypedArray())
        submitList(chatList)
    }

    fun updateMoreMessages(chatList: MutableList<Chats>) {


    }

    fun setSingleMessage(singleChatMessage: Chats) {

        this.chatList?.add(singleChatMessage)
        chatList = JavaUtilSeatCheck.doBubbleSortLogic(chatList.toTypedArray())
        submitList(chatList)

    }

    fun updateUserId(userId: String) {
        recieverId = userId
    }

    private class DiffCallback : DiffUtil.ItemCallback<Chats>() {
        override fun areItemsTheSame(
                oldItem: Chats,
                newItem: Chats
        ): Boolean {
            return oldItem.getId() == newItem.getId()
        }

        override fun areContentsTheSame(
                oldItem: Chats,
                newItem: Chats
        ): Boolean {
            return oldItem.getId() == newItem.getId() && oldItem.getTime().equals(newItem.getTime())
        }
    }
}