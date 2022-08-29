package app.rubbickcube.seatcheck.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.rubbickcube.seatcheck.AppClass
import app.rubbickcube.seatcheck.Helpers.Utils

import app.rubbickcube.seatcheck.R
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.bumptech.glide.Glide
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.fragment_about.view.*
import kotlinx.android.synthetic.main.nav_panel_layout.*
import kotlinx.android.synthetic.main.profile_edit_dialog.view.*
import javax.inject.Inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 *
 */


class AboutFragment : Fragment() {


    var backendlessUser : BackendlessUser = BackendlessUser()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_about, container, false)
        //AppClass.appComponent?.inject(this)
        setHeader(view)
        return view
    }


    private fun setHeader(view : View) {

        if(Utils.getBackendLessUser(context!!) != null) {
            backendlessUser = Utils.getBackendLessUser(context!!)
        }else {
            return
        }


        if(AppClass.reviewForOwner) {

            if(backendlessUser == null) {

                return
            }
            if(backendlessUser.properties["about"] != null){
                view.tv_about_profile.setText(backendlessUser.properties["about"].toString())

            }

            if(backendlessUser.properties["status"] != null){
                view.tv_status_profile.setText(backendlessUser.properties["status"].toString())

            }

            if(backendlessUser.properties["phone"] != null){
                view.tv_phone_profile.setText(backendlessUser.properties["phone"].toString())

            }
            view.tv_email_profile.setText(backendlessUser.properties["email"].toString())

        }else {
            if(Utils.getBackendLessOtherUser(context!!).properties["about"] != null){
                view.tv_about_profile.setText(Utils.getBackendLessOtherUser(context!!).properties["about"].toString())

            }

            if(Utils.getBackendLessOtherUser(context!!).properties["status"] != null){
                view.tv_status_profile.setText(Utils.getBackendLessOtherUser(context!!).properties["status"].toString())

            }

            if(Utils.getBackendLessOtherUser(context!!).properties["phone"] != null){
                view.tv_phone_profile.setText(Utils.getBackendLessOtherUser(context!!).properties["phone"].toString())

            }
            view.tv_email_profile.setText(Utils.getBackendLessOtherUser(context!!).properties["email"].toString())

        }


    }





}
