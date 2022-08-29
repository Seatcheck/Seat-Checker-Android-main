package app.rubbickcube.seatcheck.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.Utils
import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.model.Contacts
import app.rubbickcube.seatcheck.model.Users
import app.yasirameen.life.adapter.ContactsAdapter
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
import kotlinx.android.synthetic.main.activity_people_around_me_activitiy.*
import kotlinx.android.synthetic.main.app_bar_layout.*

class ActivityContacts : AppCompatActivity() {


    val options =  RequestOptions()
            .centerCrop()
            .placeholder(R.drawable.avatar)
            .error(R.drawable.avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .priority(Priority.HIGH)

    private var contacts: MutableList<Contacts>? = null
    private var contactsBackendlessUser: MutableList<BackendlessUser>? = null
    private var contactsAdapter: ContactsAdapter? = null
    private var manager: LinearLayoutManager? = null
    var backendlessUser : BackendlessUser = BackendlessUser()
    private var pd: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        val ab = supportActionBar
        ab?.hide()

        if(Utils.getBackendLessUser(this) != null) {
            backendlessUser = Utils.getBackendLessUser(this)

        }else {
            return
        }

        appbar_title.text = "Contacts"
        Glide.with(this@ActivityContacts).load(backendlessUser.properties["profileImage"].toString()).apply(options).into(appbar_img)
        appbar_back.setOnClickListener {
            finish()
        }

        intializeAdapter()

        getContacts(backendlessUser.objectId)
    }


    private fun getContacts(ownerId : String) {


        //val whereClause = "ownerId = '$ownerId'"
        val ids = ownerId
        //val ids = "38E60C1A-17D6-48E6-FF64-88F469BBE200"
        val whereClause = "ownerId = '$ids'"

        val dataQuery = BackendlessDataQuery()
        dataQuery.whereClause = whereClause
        val pd = Utils.SCProgressDialog(this@ActivityContacts,null,"fetching contacts...")
        pd.show()
        Backendless.Persistence.of<Contacts>(Contacts::class.java).find(dataQuery,object : AsyncCallback<BackendlessCollection<Contacts>> {
            override fun handleFault(fault: BackendlessFault?) {

                //Toast.makeText(this@ActivityContacts,fault?.message, Toast.LENGTH_LONG).show()
                pd.dismiss()
                Log.d("TAG",fault?.message)

            }

            override fun handleResponse(response: BackendlessCollection<Contacts>?) {



                contacts = response?.data as MutableList<Contacts>?
                if(contacts?.size != 0) {
                    contactsBackendlessUser = contacts!![0].contacts
                    contactsAdapter?.setContacts(contactsBackendlessUser!!)
                }else {
                   /// Toast.makeText(this@ActivityContacts,"No contact available yet", Toast.LENGTH_LONG).show()

                }

                //backendlessContacts = contacts!![0]

//                for(i in response?.data!!.indices) {
//
//                    if(response?.data[i].lat != null && response.data[i].lng !=null) {
//                        if(inRange (AppClass.lat!!, AppClass.lng!!,response?.data[i].lat.toDouble(),response.data[i].lng.toDouble(),miles)) {
//
//                            if(!backendlessUser.objectId.equals(response.data[i].objectId)) {
//                                filteredUserList?.add(response.data[i])
//                            }
//                        }
//                    }
//
//                }
//

                pd.dismiss()

            }
        })

    }

    private fun intializeAdapter() {

        contactsAdapter = ContactsAdapter(this@ActivityContacts)
        rv_chat_list?.adapter = contactsAdapter
        // getUserChatList("0DD8C034-7E1A-23EE-FFC0-7919921E4400")
        getContacts("")

    }

}
