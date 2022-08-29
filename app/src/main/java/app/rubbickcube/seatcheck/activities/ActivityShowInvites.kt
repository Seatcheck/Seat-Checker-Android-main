package app.rubbickcube.seatcheck.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.adapters.InvitesAdapter
import app.rubbickcube.seatcheck.adapters.ReviewAdapter
import app.rubbickcube.seatcheck.model.Invites
import app.rubbickcube.seatcheck.model.Reviews
import com.backendless.exceptions.BackendlessFault
import com.backendless.BackendlessCollection
import com.backendless.async.callback.AsyncCallback
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.persistence.BackendlessDataQuery
import com.bumptech.glide.Glide
import com.orhanobut.hawk.Hawk
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_show_invites.*
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.fragment_review_fragments.view.*
import javax.inject.Inject


class ActivityShowInvites : AppCompatActivity() {


    var backendlessUser: BackendlessUser = BackendlessUser()
    private var invitesList: MutableList<Invites>? = null
    private var filteredInvitesList: MutableList<Invites>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_invites)

        Hawk.init(this).build()
        if (Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)
        } else {
            return
        }
        // AppClass.appComponent?.inject(this)

        appbar_title.text = "Requests"
        appbar_img.visibility = View.INVISIBLE
        appbar_back.setOnClickListener {
            finish()
        }
        supportActionBar?.hide()


        Backendless.initApp(this@ActivityShowInvites, resources.getString(R.string.appId), resources.getString(R.string.secretKey), "v1")


//        Backendless.Persistence.of("Invites").find(object : AsyncCallback<BackendlessCollection<Map<*, *>>> {
//            override fun handleResponse(foundInvites: BackendlessCollection<Map<*, *>>) {
//
//                pd.dismiss()
//                invitesList = arrayListOf()
//                filteredInvitesList = arrayListOf()
//                 for(i in foundInvites.data.indices) {
//
//                    if(foundInvites.data[i]["receiver"] != null) {
//                        val invite = Invites()
//                        invite.objectId = foundInvites.data[i]["objectId"].toString()
//                        invite.status = foundInvites.data[i]["status"].toString()
//                        invite.receiver = foundInvites.data[i]["receiver"] as BackendlessUser
//                        invite.sender = foundInvites.data[i]["sender"] as BackendlessUser
//                        invite.requestCreated = foundInvites.data[i]["created"].toString()
//
//                        invitesList?.add(invite)
//                    }
//
//
//                }
//
//
//
//
//                for(i in invitesList?.indices!!) {
//
//                    if (invitesList!![i].status.equals("pending")) {
//                        if (Prefs.getString("userId", "").equals(invitesList!![i].receiver.properties["objectId"])) {
//
//                            filteredInvitesList!!.add(invitesList!![i])
//                        }
//                    }
//                }
//                request_lists.adapter = InvitesAdapter(this@ActivityShowInvites,filteredInvitesList!!)
//
//               if(filteredInvitesList!!.isEmpty()) {
//                   no_invites_layout.visibility = View.VISIBLE
//                   request_lists.visibility = View.GONE
//
//               }else {
//                   no_invites_layout.visibility = View.GONE
//                   request_lists.visibility = View.VISIBLE
//
//               }
//
//
//
//
//              //  Toast.makeText(this@ActivityShowInvites,invitesList?.size.toString(),Toast.LENGTH_LONG).show()
//            }
//
//            override fun handleFault(fault: BackendlessFault) {
//                pd.dismiss()
//
//                Toast.makeText(this@ActivityShowInvites,fault.message,Toast.LENGTH_LONG).show()
//
//            }
//        })

        findInvitesUserSQL()
    }


    private fun findInvitesUserSQL() {
        val pd = Utils.SCProgressDialog(this@ActivityShowInvites, null, "Checking invitations...")
        pd.show()
        val currentObjectId = Utils.quote(backendlessUser.objectId)
        val queryStatus = Utils.quote("pending")
        val whereClause = "receiver.objectId = $currentObjectId AND status = $queryStatus"
        val asdfa = "toUser.objectId = $currentObjectId"

        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        Backendless.Persistence.of(Invites::class.java).find(dataQuery,
                object : AsyncCallback<BackendlessCollection<Invites>> {

                    override fun handleResponse(foundInvites: BackendlessCollection<Invites>) {

                        Utils.dismissProgressDialog(pd)
                        invitesList = arrayListOf()
                        filteredInvitesList = arrayListOf()
                        for (i in foundInvites.data.indices) {


                            if (foundInvites.data[i].receiver != null) {
                                // val invite = Invites()
//                                invite.objectId = foundInvites.data[i].objectId.toString()
//                                invite.status = foundInvites.data[i].objectId.toString()
//                                invite.receiver = foundInvites.data[i].receiver as BackendlessUser
//                                invite.sender = foundInvites.data[i].sender as BackendlessUser
//                               // invite.requestCreated = foundInvites.data[i].requestCreated.toString()

                                invitesList?.add(foundInvites.data[i])
                            }

                            if (invitesList!!.isEmpty()) {
                                no_invites_layout.visibility = View.VISIBLE
                                request_lists.visibility = View.GONE

                            } else {
                                no_invites_layout.visibility = View.GONE
                                request_lists.visibility = View.VISIBLE

                            }

                            request_lists.adapter = InvitesAdapter(this@ActivityShowInvites, invitesList!!)
                            AppClass.inviteCounter = invitesList!!.size


                        }

                    }

                    override fun handleFault(fault: BackendlessFault) {


                        Utils.dismissProgressDialog(pd)
                        Toast.makeText(this@ActivityShowInvites, fault.message, Toast.LENGTH_LONG).show()

                    }
                })


    }


}
