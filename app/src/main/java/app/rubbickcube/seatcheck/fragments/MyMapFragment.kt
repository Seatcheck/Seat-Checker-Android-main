package app.rubbickcube.seatcheck.fragments


import android.annotation.SuppressLint
import android.view.DragEvent
import android.view.View
import android.widget.Toast
import app.rubbickcube.seatcheck.ObservableObject
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.util.*
import com.google.android.gms.maps.CameraUpdate



class MyMapFragment : SupportMapFragment(), OnMapReadyCallback, Observer{

    private var mMap: GoogleMap? = null

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap?) {
        mMap = map as GoogleMap
        map.isMyLocationEnabled = true
        mMap?.getUiSettings()?.isMyLocationButtonEnabled = false
        ObservableObject.getInstance().addObserver(this)
    }

    override fun update(p0: Observable?, p1: Any?) {

        var zoomTo : Int? = null
        if(p1 is JSONObject) {

            val latlngObject = JSONObject(p1.toString())
            val currentLocation = LatLng(latlngObject.getString("lat").toDouble(), latlngObject.getString("lng").toDouble())

            //mMap?.addMarker(MarkerOptions().position(currentLocation).title(latlngObject.getString("address")))
            val update = CameraUpdateFactory.newLatLngZoom(currentLocation, 16f)
            mMap?.animateCamera(update)

        }
    }


}