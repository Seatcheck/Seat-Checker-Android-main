package app.rubbickcube.seatcheck.activities

import android.app.ProgressDialog
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import app.invision.morse.api.ApiUtils
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.api.EndPointsConstants
import app.rubbickcube.seatcheck.model.ChatUser
import app.rubbickcube.seatcheck.model.Chats
import app.rubbickcube.seatcheck.model.Users
import app.yasirameen.life.adapter.PeopleAroundMeAdapter
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
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_people_around_me_activitiy.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PeopleAroundMeActivitiy : AppCompatActivity() {


    private var filteredUserList : MutableList<ChatUser>? = null
    val options =  RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    private var people: MutableList<Users>? = null
    private var peopleAdapter: PeopleAroundMeAdapter? = null
    private var manager: LinearLayoutManager? = null
    var backendlessUser : BackendlessUser = BackendlessUser()
    private var pd: KProgressHUD? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people_around_me_activitiy)

        val ab = supportActionBar
        ab?.hide()

        if(Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)

        }else {
            return
        }

        appbar_title.text = "People Around Me"
        Glide.with(this@PeopleAroundMeActivitiy).load(backendlessUser.properties["profileImage"].toString()).apply(options).into(appbar_img)
        appbar_back.setOnClickListener {
            finish()
        }

        intializeAdapter()
    }


    private fun intializeAdapter() {

        peopleAdapter = PeopleAroundMeAdapter(this@PeopleAroundMeActivitiy)
        rv_chat_list?.adapter = peopleAdapter
        // getUserChatList("0DD8C034-7E1A-23EE-FFC0-7919921E4400")

        if(AppClass.lat != null && AppClass.lng != null) {
            getPeopleAroundMe(AppClass.lat.toString(),AppClass.lng.toString(),"16")

        }

    }


//    private fun fetchPeopleAroundMe(lat : String? , lng : String?) {
//
//
//        val whereClause = "distance( $lat, $lng, location.latitude, location.longitude ) < mi(10)"
//
//        val dataQuery = BackendlessDataQuery()
//        dataQuery.whereClause = whereClause
//        val pd = Utils.SCProgressDialog(this@PeopleAroundMeActivitiy,null,"fetching nearby people...")
//        pd.show()
//        Backendless.Persistence.of<Users>(Users::class.java).find(dataQuery,object : AsyncCallback<BackendlessCollection<Users>> {
//            override fun handleFault(fault: BackendlessFault?) {
//
//        Toast.makeText(this@PeopleAroundMeActivitiy,fault?.message,Toast.LENGTH_LONG).show()
//                pd.dismiss()
//                Log.d("TAG",fault?.message)
//
//
//            }
//
//            override fun handleResponse(response: BackendlessCollection<Users>?) {
//
//                filteredUserList = arrayListOf()
//                for(i in response?.data!!.indices) {
//
//                    if(!backendlessUser.objectId.equals(response.data[i].objectId)) {
//                        filteredUserList?.add(response.data[i])
//                    }
//
//                }
//
//                peopleAdapter?.setNearUser(filteredUserList!!)
//                if(filteredUserList?.isEmpty()!!) {
//                    Toast.makeText(this@PeopleAroundMeActivitiy,"No People around you right now",Toast.LENGTH_LONG).show()
//                }
//
//                pd.dismiss()
//
//            }
//        })
//
//    }

    private fun inRange(latCenter : Double, lngCenter : Double, latDest : Double, lngDest : Double, radius : Int) : Boolean {
        val results = FloatArray(1)
        Location.distanceBetween(latCenter, lngCenter, latDest, lngDest, results)
        val distanceInMeters = results[0]
        val isWithinRange = distanceInMeters < radius

        return isWithinRange
    }

    fun getPeopleAroundMe(lat : String, lng :String,radius : String) {

        val mService = ApiUtils.getSOService()
        val url = EndPointsConstants.GET_PEOPLE

        pd = Utils.SCProgressDialog(this@PeopleAroundMeActivitiy,null,"Fetching People Around You!")
        pd?.show()
        mService.getPeopleAroundMe(url,Utils.getSimpleTextBody(lat),Utils.getSimpleTextBody(lng),Utils.getSimpleTextBody(radius))
                .enqueue(object : Callback<MutableList<ChatUser>> {
                    override fun onFailure(call: Call<MutableList<ChatUser>>?, t: Throwable?) {
                        pd?.dismiss()

                    }

                    override fun onResponse(call: Call<MutableList<ChatUser>>?, response: Response<MutableList<ChatUser>>?) {



                        if(response?.isSuccessful!!) {


                            filteredUserList = arrayListOf()

                            for(i in  response?.body().indices) {

                                if(response.body()[i].seatcheckUserId != backendlessUser.objectId) {
                                    filteredUserList?.add(response.body()[i])

                                }
                            }

                            if(filteredUserList?.isEmpty()!!) {
                                no_people_layout.visibility = View.VISIBLE
                                rv_chat_list.visibility = View.GONE
                            }else {
                                rv_chat_list.visibility = View.VISIBLE
                                no_people_layout.visibility = View.GONE
                                peopleAdapter?.setNearUser(filteredUserList!!)

                            }
                        }

                        pd?.dismiss()

                    }



                })



    }

}
