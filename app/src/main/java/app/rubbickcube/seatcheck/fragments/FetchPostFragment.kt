package app.rubbickcube.seatcheck.fragments


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import app.rubbickcube.seatcheck.*
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.Helpers.Utils

import app.rubbickcube.seatcheck.activities.ActivitySeatLive
import app.rubbickcube.seatcheck.activities.ActivitySeatLiveInMeeting
import app.rubbickcube.seatcheck.activities.NeedASeatActivity
import app.rubbickcube.seatcheck.adapters.PostOnListAdapter
import app.rubbickcube.seatcheck.model.Availability
import app.rubbickcube.seatcheck.model.Post
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_fetch_post.view.*
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
const val TAG = "FetchPostFragment"

class FetchPostFragment : Fragment(), Observer {

    var backendlessUser: BackendlessUser = BackendlessUser()
    var selectedPos: Int? = null
    override fun onStart() {
        super.onStart()
        ObservableObject.getInstance().addObserver(this)
    }

    override fun update(p0: Observable?, p1: Any?) {

     //   showAvailableList(AppClass.AvailableList!!)
        if (p1 is String) {
            if (p1.equals("showList")) {

                showSeatList(AppClass.postList!!)
            } else if (p1.equals("listIsEmpty")) {
            }
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fetch_post, container, false)
        setupComponents()
        return view
    }

    private fun setupComponents() {
        if (Utils.getBackendLessUser(context!!) != null) {
            backendlessUser = Utils.getBackendLessUser(context!!)
        } else {
            return
        }
    }


    private fun showSeatList(list: MutableList<Post>) {
        var tempList: MutableList<Post>? = arrayListOf()

        for (tempvar in list) {
            if (tempvar.shouldGoLive.equals("yes")) {
                tempList?.add(tempvar)
            }
        }
        val adapter = PostOnListAdapter(context, tempList, backendlessUser)
        view?.post_list?.adapter = adapter

        view?.post_list?.onItemClickListener = AdapterView.OnItemClickListener { p0, p1, p2, p3 ->

            val post = tempList!![p2]


            handlePost(post)
            //findSeat(post.objectId,"Fetching seat details...")
//            if(Prefs.getString("userId","").equals(post?.user?.objectId)) {
//
//                var intent : Intent?
//                if(Prefs.getBoolean("inMeeting",false)){
//                    intent =Intent(context,ActivitySeatLiveInMeeting::class.java)
////                    intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                    intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                    intent.putExtra("seatcheck_location_name",post.resturantName)
////                    intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                    intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////                    intent.putExtra("seatcheck_post_id",post.objectId)
////                    intent.putExtra("seats",post.quantity.toInt())
////                    intent.putExtra("createdTwo",post.createdTwo)
////                    intent.putExtra("extendedTime",post.extendedTime)
////                    intent.putExtra("created",post.created)
//                    AppClass.selectedPost = post
//                    startActivity(intent)
//
//                }else {
//                    intent =Intent(context,ActivitySeatLive::class.java)
////                    intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                    intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                    intent.putExtra("seatcheck_location_name",post.resturantName)
////                    intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                    intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////                    intent.putExtra("extendedTime",post.extendedTime)
////                    intent.putExtra("created",post.created)
////
////                    intent.putExtra("seatcheck_post_id",post.objectId)
////                    intent.putExtra("seats",post.quantity.toInt())
////                    intent.putExtra("createdTwo",post.createdTwo)
//                    AppClass.selectedPost = post
//                    startActivity(intent)
//                }
//
//            }else {
//                val intent = Intent(context,SeatCheckUserActivity::class.java)
////                intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                intent.putExtra("seatcheck_location_name",post.resturantName)
////                intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////                intent.putExtra("extendedTime",post.extendedTime)
////                intent.putExtra("created",post.created)
////
////                intent.putExtra("seatcheck_post_id",post.objectId)
////                intent.putExtra("seats",post.quantity.toInt())
////
////                intent.putExtra("createdTwo",post.createdTwo)
//               AppClass.selectedPost = post
//                startActivity(intent)
//            }

////            if(HawkUtils.getHawk("postList") != null) {
////
////                val postList = HawkUtils.getHawk("postList") as MutableList<Post>
////                for(i in postList.indices) {
////                  if(postList[i].objectId.equals(post.objectId)) {
////
////                      if(Prefs.getString("userId","").equals(post?.user?.properties!!["ownerId"])) {
////                          var intent : Intent? = null
////
////                          if(Prefs.getBoolean("inMeeting",false)){
////                              intent =Intent(context,ActivitySeatLiveInMeeting::class.java)
////                              intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                              intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                              intent.putExtra("seatcheck_location_name",post.resturantName)
////                              intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                              intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////                              intent.putExtra("seatcheck_post_id",post.objectId)
////                              intent.putExtra("seats",post.quantity.toInt())
////                              intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
////                              intent.putExtra("extendedTime",post.extendedTime)
////                              AppClass.selectedPost = post
////                              startActivity(intent)
////
////                          }else {
////                              intent =Intent(context,ActivitySeatLive::class.java)
////                              intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                              intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                              intent.putExtra("seatcheck_location_name",post.resturantName)
////                              intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                              intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////                              intent.putExtra("extendedTime",post.extendedTime)
////
////                              intent.putExtra("seatcheck_post_id",post.objectId)
////                              intent.putExtra("seats",post.quantity.toInt())
////                              intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
////                              AppClass.selectedPost = post
////                              startActivity(intent)
////                          }
////
////                      }else {
////                          val intent = Intent(context,SeatCheckUserActivity::class.java)
////                          intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
////                          intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
////                          intent.putExtra("seatcheck_location_name",post.resturantName)
////                          intent.putExtra("seatcheck_location_desc",post.resturantAddress)
////                          intent.putExtra("seatcheck_location_phone",post.resturantPhone)
////                          intent.putExtra("extendedTime",post.extendedTime)
////
////                          intent.putExtra("seatcheck_post_id",post.objectId)
////                          intent.putExtra("seats",post.quantity.toInt())
////
////                          intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
////                          AppClass.selectedPost = post
////                          startActivity(intent)
////                      }
////                  }
////                }
//            }
            //  findSeat(post.objectId,"Fetching seat details...")

        }
    }

    private fun showAvailableList(list: MutableList<Availability>) {
        var tempList: MutableList<Availability>? = arrayListOf()

        for (tempvar in list) {
            if (tempvar.isIsAvailable ) {
                tempList?.add(tempvar)
            }
        }
        Log.d(TAG, "showAvailableList1: "+tempList?.size)
//        val adapter = PostOnListAdapter(context, tempList, backendlessUser)
//        view?.post_list?.adapter = adapter
//
//        view?.post_list?.onItemClickListener = AdapterView.OnItemClickListener { p0, p1, p2, p3 ->
//
//            val post = tempList!![p2]
//
//
//            handlePost(post)
//        }
    }

    fun fetchTime(createdTwo: String): String {
        val date = SimpleDateFormat("MM/dd/yyyy hh:mm:ss").parse(createdTwo)
        return SimpleDateFormat("HH:mm:ss").format(date) // 9:00
    }

    fun findSeat(seatId: String?, message: String?) {
        val pd = Utils.SCProgressDialog(context!!, null, message!!)
        pd.show()

        Backendless.Data.of(Post::class.java).findById(seatId, object : AsyncCallback<Post> {
            override fun handleFault(fault: BackendlessFault?) {
                Log.d("TAG", fault?.message)
                Utils.dismissProgressDialog(pd!!)
                val seatExpireTask = SeatExpireTask(context!!)
                seatExpireTask.execute()


            }

            override fun handleResponse(post: Post?) {
                Utils.dismissProgressDialog(pd!!)


                val backendlessUser = HawkUtils.getHawk("BackendlessUser") as BackendlessUser
                if (backendlessUser.objectId.equals(post?.user?.properties!!["ownerId"])) {
                    var intent: Intent? = null

                    if (Prefs.getBoolean("inMeeting", false)) {
                        intent = Intent(context, ActivitySeatLiveInMeeting::class.java)
//                        intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                        intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                        intent.putExtra("seatcheck_location_name",post.resturantName)
//                        intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                        intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//                        intent.putExtra("seatcheck_post_id",post.objectId)
//                        intent.putExtra("seats",post.quantity.toInt())
//                        intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
//                        AppClass.selectedPost = post
                        startActivity(intent)

                    } else {
                        intent = Intent(context, ActivitySeatLive::class.java)
//                        intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                        intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                        intent.putExtra("seatcheck_location_name",post.resturantName)
//                        intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                        intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//
//                        intent.putExtra("seatcheck_post_id",post.objectId)
//                        intent.putExtra("seats",post.quantity.toInt())
//                        intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
                        AppClass.selectedPost = post
                        startActivity(intent)
                    }

                } else {
                    val intent = Intent(context, SeatCheckUserActivity::class.java)
//                    intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                    intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                    intent.putExtra("seatcheck_location_name",post.resturantName)
//                    intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                    intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//
//                    intent.putExtra("seatcheck_post_id",post.objectId)
//                    intent.putExtra("seats",post.quantity.toInt())
//
//                    intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
                    AppClass.selectedPost = post
                    startActivity(intent)
                }

            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        ObservableObject.getInstance().deleteObserver(this)

    }

    inner class SeatExpireTask(private val mContext: Context) : AsyncTask<Void, Void, Long>() {


        val pd = Utils.SCProgressDialog(mContext, null, "Seat is expired..")
        override fun onPreExecute() {
            super.onPreExecute()
            pd.show()

        }

        override fun onPostExecute(result: Long?) {
            Utils.dismissProgressDialog(pd!!)

            //startActivity(Intent(context,NeedASeatActivity::class.java))
            ObservableObject.getInstance().updateValue("Refresh")


        }

        override fun doInBackground(vararg p0: Void?): Long {

            for (i in 1..3) {
                Thread.sleep(1000)
            }

            return 0
        }


    }


    fun handlePost(post: Post) {

        val backendlessUser = HawkUtils.getHawk("BackendlessUser") as BackendlessUser
        if (backendlessUser.objectId.equals(post?.user?.properties!!["ownerId"])) {
            var intent: Intent? = null

            if (Prefs.getBoolean("inMeeting", false)) {
                intent = Intent(context, ActivitySeatLiveInMeeting::class.java)
//                        intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                        intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                        intent.putExtra("seatcheck_location_name",post.resturantName)
//                        intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                        intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//                        intent.putExtra("seatcheck_post_id",post.objectId)
//                        intent.putExtra("seats",post.quantity.toInt())
//                        intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
//                        AppClass.selectedPost = post
                startActivity(intent)

            } else {
                intent = Intent(context, ActivitySeatLive::class.java)
//                        intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                        intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                        intent.putExtra("seatcheck_location_name",post.resturantName)
//                        intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                        intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//
//                        intent.putExtra("seatcheck_post_id",post.objectId)
//                        intent.putExtra("seats",post.quantity.toInt())
//                        intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
                AppClass.selectedPost = post
                startActivity(intent)
            }

        } else {
            val intent = Intent(context, SeatCheckUserActivity::class.java)
//                   intent.putExtra("seatcheck_name",post.user.properties["name"].toString())
//                    intent.putExtra("seatcheck_profileImage",post.user.properties["profileImage"].toString())
//                    intent.putExtra("seatcheck_location_name",post.resturantName)
//                    intent.putExtra("seatcheck_location_desc",post.resturantAddress)
//                    intent.putExtra("seatcheck_location_phone",post.resturantPhone)
//
//                    intent.putExtra("seatcheck_post_id",post.objectId)
//                    intent.putExtra("seats",post.quantity.toInt())
//
//                    intent.putExtra("createdTwo",fetchTime(Utils.converTimetoGMT(post.createdTwo)))
            AppClass.selectedPost = post
            startActivity(intent)
        }
    }


}
