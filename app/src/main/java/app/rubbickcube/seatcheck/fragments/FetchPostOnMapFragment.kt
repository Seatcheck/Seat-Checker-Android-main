package app.rubbickcube.seatcheck.fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import app.rubbickcube.seatcheck.*

import app.rubbickcube.seatcheck.model.Post
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MapStyleOptions

import java.util.*

class FetchPostOnMapFragment : Fragment(), Observer {
    override fun update(p0: Observable?, p1: Any?) {

        if(p1 is String) {
            if(p1.equals("showList")) {

                var tempList: MutableList<Post>? = arrayListOf()
                tempList?.clear()

                for(tempvar in  AppClass.postList!!) {
                    if(tempvar.shouldGoLive.equals("yes")) {
                        tempList?.add(tempvar)
                    }
                }
                mapFragment.plotMarkers(tempList!!)
            }else if(p1.equals("listIsEmpty")) {
                mapFragment.clearMap()
            }
        }
    }


    override fun onStart() {
        super.onStart()

        ObservableObject.getInstance().addObserver(this)
    }


    private lateinit var mapFragment: MyMapPostFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fetch_post_on_map, container, false)

         mapFragment = this.childFragmentManager
                .findFragmentById(R.id.map) as MyMapPostFragment
        mapFragment.getMapAsync(mapFragment)

        return view
    }


    override fun onDestroy() {
        super.onDestroy()
        ObservableObject.getInstance().deleteObserver(this)

    }








}
