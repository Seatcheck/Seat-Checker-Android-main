package app.rubbickcube.seatcheck.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.bumptech.glide.Glide
import com.pixplicity.easyprefs.library.Prefs

import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.model.Invites
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import de.hdodenhof.circleimageview.CircleImageView
import android.app.Activity
import android.app.ProgressDialog
import android.location.Location
import androidx.appcompat.app.AlertDialog
import android.util.Log
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.ObservableObject
import app.rubbickcube.seatcheck.activities.ActivitySeatLiveInMeeting
import app.rubbickcube.seatcheck.activities.ChatActivity
import app.rubbickcube.seatcheck.activities.OtherUserProfileActivity
import app.rubbickcube.seatcheck.model.Post
import app.rubbickcube.seatcheck.services.LocationService
import com.backendless.BackendlessCollection
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.curioustechizen.ago.RelativeTimeTextView
import com.kaopiz.kprogresshud.KProgressHUD
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.need_seat_layou.*
import kotlinx.android.synthetic.main.seat_invitatino_denied_dialog.view.*


/**
 * Created by hp on 10/29/2016.
 */

class InvitesAdapter(private val context: Context, invites: List<Invites>) : BaseAdapter() {
    private var inflater: LayoutInflater? = null

    init {
        inflater = LayoutInflater.from(context)
        invitesList = invites
        Hawk.init(context).build()

        RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)


    }

    override fun getCount(): Int {
        return invitesList.size
    }

    override fun getItem(i: Int): Any {
        return invitesList.size
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, convertview: View?, viewGroup: ViewGroup): View {
        var convertview = convertview

        val invites = invitesList[i]

        val holder: ViewHolder
        if (convertview == null) {

            convertview = inflater!!.inflate(R.layout.invite_list_item, null)
            holder = ViewHolder()

            holder.accept = convertview!!.findViewById(R.id.btn_accept)
            holder.decline = convertview.findViewById(R.id.btn_decline)
            holder.chat = convertview.findViewById(R.id.btn_chat)
            holder.name = convertview.findViewById(R.id.txt_name_invite_list)
            holder.txt_view_profile = convertview.findViewById(R.id.txt_view_profile)
            holder.userdp = convertview.findViewById(R.id.img_invite_list)
            holder.minsAgo = convertview.findViewById(R.id.txt_mins_ago_invite_list)

            convertview.tag = holder

        } else {
            holder = convertview.tag as ViewHolder
        }

        holder.name!!.text = invites.sender.properties["name"].toString()
        holder.minsAgo!!.setReferenceTime(Utils.getTimeinLong(invites.created)) /*= invites.sender.properties["name"].toString()*/
        Glide.with(context).load(invites.sender.properties["profileImage"].toString()).apply(RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)).into(holder.userdp!!)

        holder.accept!!.setOnClickListener {

            if(Prefs.getBoolean("inMeeting",false)) {
                Utils.showAlertDialog(context,"In Meeting","You can't accept invitation while you are in meeting")

            }else {
                updateInvitationStatusCopy(context,invites)

            }
            //AppClass.invites = invites
            //context.startActivity(Intent(context, ActivitySeatAccepted::class.java))
        }

        holder.userdp?.setOnClickListener {


            //            otherUserProfileImage = intent.getStringExtra("otherUserProfileImage")
//            otherUserId = intent.getStringExtra("otherUserId")
//            otherUserName = intent.getStringExtra("otherUserName")

            AppClass.reviewForOwner = false
            val intent = Intent(context,OtherUserProfileActivity::class.java)
            if(invites.sender.properties["profileImage"] != null) {
                intent.putExtra("otherUserProfileImage",invites.sender.properties["profileImage"].toString())
            }
            intent.putExtra("otherUserId",invites.sender.objectId)
            intent.putExtra("otherUserName",invites.sender.properties["name"].toString())
            context.startActivity(intent)

        }

        holder.decline!!.setOnClickListener {

            showInviteDeniedDialo(invites)
        }

        holder.chat!!.setOnClickListener {

            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("sender_id", Prefs.getString("userId",""))
            intent.putExtra("receiver_id",invites.sender.objectId)
            intent.putExtra("name",invites.sender.properties["name"].toString())
            context.startActivity(intent)
        }

        return convertview
    }

    inner class ViewHolder {

        internal var name: TextView? = null
        internal var txt_view_profile: TextView? = null
        internal var minsAgo: RelativeTimeTextView? = null
        internal var accept: ImageView? = null
        internal var decline: ImageView? = null
        internal var chat: ImageView? = null
        internal var userdp: CircleImageView? = null

    }

    companion object {
        lateinit var invitesList: List<Invites>
    }


    private fun updateInvitationStatusCopy(mContext: Context, invite: Invites) {

        val pd = Utils.SCProgressDialog(mContext,null,"Accepting Seatcheck...")
        pd.show()


        var _post : Post? = null
        if(HawkUtils.getHawk("postList") != null) {
            val postList = HawkUtils.getHawk("postList") as MutableList<Post>
            for(i in postList.indices) {

                if(postList!![i].objectId.equals(Prefs.getString("seatId",""))) {
                    _post = postList!![i]
                }
            }
        }

        _post?.shouldGoLive = "no"
        _post?.invite = invite
        _post?.invite?.status = "accepted"



        Backendless.Persistence.save(_post,object : AsyncCallback<Post> {

            override fun handleResponse(response: Post?) {

                Utils.sentNotification(context,"Request Accepted",response?.invite?.receiver?.properties!!["name"].toString() +" accepted your invitation.",response.invite.sender.properties["fcmToken"].toString(),"nothing")


                Utils.cancelAlarm(context)
                HawkUtils.putHawk("inMeetingPost",response)
                HawkUtils.putHawk("invites",response?.invite)
                Prefs.putBoolean("inMeeting",true)
                Prefs.putInt("userStatus",2)
                AppClass.inviteCounter--
                context.startActivity(Intent(context, ActivitySeatLiveInMeeting::class.java))
                (context as Activity).finish()
                Utils.dismissProgressDialog(pd)

                //fetchPostViaModelForRadiusSearch(2,context,pd,invite,response!!)
            }


            override fun handleFault(fault: BackendlessFault?) {
                Toast.makeText(mContext,fault?.message, Toast.LENGTH_LONG).show()
                (context as Activity).finish()
                Utils.dismissProgressDialog(pd)


            }

        })


    }


    private fun updateInvitationStatusToDeclined(mContext: Context, invites: Invites, reason : String?) {

        val pd = Utils.SCProgressDialog(mContext,null,"Declined Seatcheck...")
        pd.show()





        invites.status = "declined"
        invites.reason = reason
        //HawkUtils.putHawk("invites",invites)


        Backendless.Data.save(invites,object : AsyncCallback<Invites> {
            override fun handleFault(fault: BackendlessFault?) {

                Toast.makeText(mContext,fault?.message, Toast.LENGTH_LONG).show()
                Utils.dismissProgressDialog(pd)

            }

            override fun handleResponse(response: Invites?) {

                Utils.dismissProgressDialog(pd)

                AppClass.inviteCounter--
                Utils.sentNotification(context,"Request Declined",response?.receiver?.properties!!["name"].toString() +" declined your request.",response.sender.properties["fcmToken"].toString(),"nothing")

                (context as Activity).finish()

            }

        })


        /*------------------------------------------------------*/


//        Backendless.Persistence.of(Invites::class.java).findById(objectId, object : AsyncCallback<Invites> {
//            override fun handleResponse(response: Invites) {
//
//
//                pd.dismiss()
//                response.status = "accepted"
//                HawkUtils.putHawk("invites",invites)
//                 changeSeatStatus(Prefs.getString("seatId",""),context,response)
//
//                Backendless.Data.of(Invites::class.java).save(response,object  : AsyncCallback<Invites>{
//                    override fun handleFault(fault: BackendlessFault?) {
//
//                        Toast.makeText(mContext,fault?.message, Toast.LENGTH_LONG).show()
//
//                    }
//
//                    override fun handleResponse(response: Invites?) {
//                        //AppClass.invites = invites
//                        Prefs.putBoolean("inMeeting",true)
//                        Prefs.putInt("userStatus",2)
//                        context.startActivity(Intent(context, ActivitySeatLiveInMeeting::class.java))
//                        (context as Activity).finish()
//
//                    }
//
//                })
//
//
//            }
//
//            override fun handleFault(fault: BackendlessFault) {
//                pd.dismiss()
//                Toast.makeText(mContext,fault.message, Toast.LENGTH_LONG).show()
//
//
//
//            }
//        })
    }



    private fun showInviteDeniedDialo(invites: Invites) {

        var reasonForDenying = ""
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)

        val view = inflater.inflate(R.layout.seat_invitatino_denied_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.rounded_corner)

        dialog.show()


        view.radio_leaving.setOnCheckedChangeListener { p0, p1 ->
            if(p1) {
                reasonForDenying  = "Sorry I'm leaving"
                view.radio_other.isChecked = false
                view.radio_restaurant_close.isChecked = false

            }else {

            }
        }



        view.btn_done.setOnClickListener {

            if(!reasonForDenying.isNullOrEmpty()){
                updateInvitationStatusToDeclined(context,invites,reasonForDenying)


            }else {
                Toast.makeText(context,"Please select a reason for denying",Toast.LENGTH_LONG).show()

            }
        }

        view.close_dialog.setOnClickListener {

            dialog.dismiss()
        }

        view.radio_restaurant_close.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {


                if(p1) {
                    reasonForDenying  = "Restaurant about to close"
                    view.radio_leaving.isChecked = false
                    view.radio_other.isChecked = false

                }else {

                }
            }

        })

        view.radio_other.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {

                if(p1) {
                    reasonForDenying  = "Other"
                    view.radio_leaving.isChecked = false
                    view.radio_restaurant_close.isChecked = false

                }else {

                }

            }

        })


        // manipulationSeatsButton(dialog_layout)


    }


//    fun findSeat(seatId : String?, context: Context ){
//
//
//        Backendless.Data.of(Post::class.java).findById(seatId, object : AsyncCallback<Post>{
//            override fun handleFault(fault: BackendlessFault?) {
//                Log.d("TAG",fault?.message)
//                Toast.makeText(context!!,fault?.message,Toast.LENGTH_LONG).show()
//
//            }
//            override fun handleResponse(post: Post) {
//
//                post.shouldGoLive = "no"
//                Backendless.Persistence.save(post,object : AsyncCallback<Post> {
//
//                    override fun handleFault(fault: BackendlessFault?) {
//                        Toast.makeText(context!!,fault?.message,Toast.LENGTH_LONG).show()
//                    }
//
//                    override fun handleResponse(response: Post?) {
//                        Toast.makeText(context!!,"Seat status updated",Toast.LENGTH_LONG).show()
//
//                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//                    }
//
//                })
//
//            }
//        })
//    }



    private fun changeSeatStatus(seatId : String, context : Context, _invite : Invites) {
        Backendless.Persistence.of(Post::class.java).findById(seatId, object : AsyncCallback<Post> {
            override fun handleResponse(response: Post) {

                response.shouldGoLive = "no"
                response.invite = _invite
                Backendless.Data.of(Post::class.java).save(response, object : AsyncCallback<Post> {

                    override fun handleResponse(response: Post?) {

                    }

                    override fun handleFault(fault: BackendlessFault?) {

                        Toast.makeText(context,fault?.message, Toast.LENGTH_LONG).show()
                        Log.d("daz",fault?.message)

                    }

                })

            }

            override fun handleFault(fault: BackendlessFault) {

                Toast.makeText(context,fault.message, Toast.LENGTH_LONG).show()
                Log.d("daz",fault?.message)


            }
        })

    }


    private fun fetchPostViaModelForRadiusSearch(seekbar: Int, context : Context, pd : KProgressHUD, invites: Invites, response: Post) {


        var tempPostList : MutableList<Post>? = null
        var changeRadiusPostList : MutableList<Post>? = null
        var currentMarkerList : MutableList<Post>? = null

        Backendless.Persistence.of<Post>(Post::class.java).find(object : AsyncCallback<BackendlessCollection<Post>> {
            override fun handleResponse(foundPost: BackendlessCollection<Post>) {


                tempPostList = arrayListOf()
                changeRadiusPostList = foundPost.data
                currentMarkerList = arrayListOf()
                AppClass.allPost = foundPost.data
                val miles = 1609

                currentMarkerList!!.clear()

                for(i in foundPost.data!!.indices) {
                    if(inRange (AppClass.lat!!,AppClass.lng!!,foundPost.data[i].resturantLocationLatitude.toDouble(),foundPost.data[i].resturantLocationLongitude.toDouble(),miles.times(seekbar))) {
                        if (Utils.compareTwoDates(foundPost.data!![i].createdTwo, Utils.fetchCurrentTimeTempForSolvingiPhoneIsuse())) {
                            foundPost.data!![i].inRange = true
                            tempPostList!!.add(foundPost.data[i])
                        }
                    }
                }

                //Second Loop starts here
                //itterating all the seats which are lie in radius
                for(n in tempPostList!!.indices) {
                    if(tempPostList!![n].inRange) {
                        currentMarkerList?.add(tempPostList!![n])
                    }
                }

                if(currentMarkerList!!.isEmpty()) {


                }else {

                    AppClass.postList = currentMarkerList
                    HawkUtils.putHawk("postList",currentMarkerList)


//                    if(hasSeatLiveNo && currentMarkerList!!.size > 0) {
//                        no_pin_layout.visibility = View.GONE
//                    }
//                    else  {
//                        no_pin_layout.visibility = View.VISIBLE
//                    }


                }



                HawkUtils.putHawk("invites",invites)
                Prefs.putBoolean("inMeeting",true)
                Prefs.putInt("userStatus",2)
                AppClass.inviteCounter--
                Utils.sentNotification(context,"SeatCheck Accepted",response?.invite?.receiver?.properties!!["name"].toString() +" accepted your invitation.",response.invite.sender.properties["fcmToken"].toString(),"nothing")
                context.startActivity(Intent(context, ActivitySeatLiveInMeeting::class.java))
                (context as Activity).finish()
                Utils.dismissProgressDialog(pd)

            }

            override fun handleFault(fault: BackendlessFault) {

                HawkUtils.putHawk("invites",invites)
                Prefs.putBoolean("inMeeting",true)
                Prefs.putInt("userStatus",2)
                AppClass.inviteCounter--
                Utils.sentNotification(context,"SeatCheck Accepted",response?.invite?.receiver?.properties!!["name"].toString() +" accepted your invitation.",response.invite.sender.properties["fcmToken"].toString(),"nothing")
                context.startActivity(Intent(context, ActivitySeatLiveInMeeting::class.java))
                (context as Activity).finish()
                Utils.dismissProgressDialog(pd)
            }
        })


    }
    private fun inRange(latCenter : Double, lngCenter : Double, latDest : Double, lngDest : Double, radius : Int) : Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(latCenter, lngCenter, latDest, lngDest, results)
        val distanceInMeters = results[0]
        val isWithinRange = distanceInMeters < radius

        return isWithinRange
    }


}
