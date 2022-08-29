package app.rubbickcube.seatcheck.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import app.rubbickcube.seatcheck.Helpers.Utils

import app.rubbickcube.seatcheck.R
import app.rubbickcube.seatcheck.adapters.ReviewAdapter
import app.rubbickcube.seatcheck.model.Reviews
import kotlinx.android.synthetic.main.fragment_review_fragments.*
import kotlinx.android.synthetic.main.fragment_review_fragments.view.*
import com.backendless.exceptions.BackendlessFault
import com.backendless.BackendlessCollection
import com.backendless.async.callback.AsyncCallback
import com.backendless.Backendless
import com.backendless.persistence.BackendlessDataQuery
import com.pixplicity.easyprefs.library.Prefs
import java.text.SimpleDateFormat
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.HawkUtils
import app.rubbickcube.seatcheck.model.ReviewsForShowingList
import com.backendless.BackendlessUser
import java.util.*
import javax.inject.Inject


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class ReviewFragments : Fragment() {


    var backendlessUser : BackendlessUser = BackendlessUser()
    private val SECOND_MILLIS = 1000
    private val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private val DAY_MILLIS = 24 * HOUR_MILLIS

    private lateinit var currentObjectId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_review_fragments, container, false)
        //AppClass.appComponent?.inject(this)

        getReviews(view)
        return view
    }


    private fun getReviews(view : View)     {
//
//        val pd = Utils.SCProgressDialog(context!!,null,"Fetching user reviews...")
//        pd.show()




        if(Utils.getBackendLessUser(context!!) != null) {
            backendlessUser = Utils.getBackendLessUser(context!!)

            if(AppClass.reviewForOwner) {
                currentObjectId = backendlessUser.objectId
            }else {
                currentObjectId = Prefs.getString("otherUserId","")
            }

            val currentObjectId = quote(currentObjectId)
            val whereClause = "toUser.objectId = $currentObjectId"
            val dataQuery = BackendlessDataQuery()
            dataQuery.whereClause = whereClause
            Backendless.Persistence.of(Reviews::class.java).find(dataQuery,
                    object : AsyncCallback<BackendlessCollection<Reviews>> {

                        override fun handleResponse(foundContacts: BackendlessCollection<Reviews>) {

                            try {
                                val filteredReviewList : MutableList<Reviews>? = arrayListOf()
                                if(foundContacts.data != null) {
                                    for(i in foundContacts.data.indices) {

                                        if(foundContacts.data[i].fromUser != null && foundContacts.data[i].toUser != null) {
                                            filteredReviewList?.add(foundContacts.data[i])
                                        }
                                    }
                                }

                                if(filteredReviewList != null) {
                                    view?.reviewList?.adapter = ReviewAdapter(context!!,filteredReviewList!!)

                                }
                            }catch (ex : NullPointerException) {

                            }


//
//                        var sum = 0
//                        for(i in foundContacts.data.indices) {
//                            val star = foundContacts.data[i].numberOfStars
//                            sum = sum.plus(star.toInt())
//                        }
//
//                        val rating = sum.div(foundContacts.data.size)
//                        Prefs.putInt("rating",rating)


                        }

                        override fun handleFault(fault: BackendlessFault) {

                            //  pd.dismiss()
                            Toast.makeText(context,fault.message,Toast.LENGTH_SHORT).show()

                        }
                    })
        }

    }

    fun quote(s: String): String {
        return StringBuilder()
                .append('\'')
                .append(s)
                .append('\'')
                .toString()
    }







    






}
