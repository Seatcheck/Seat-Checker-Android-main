package app.rubbickcube.seatcheck.adapters

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.rubbickcube.seatcheck.ObservableObject
import app.rubbickcube.seatcheck.R
import com.google.android.libraries.places.api.model.PlaceLikelihood

class NearbyPlacesAdapter(internal var context: Context,internal var placesList : List<PlaceLikelihood>)
    : RecyclerView.Adapter<NearbyPlacesAdapter.PlacesViewHolder>()
{

    private var selectedItem: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlacesViewHolder {


        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.nearby_places_layout_item, parent, false)
        return PlacesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return placesList.size
    }

    override fun onBindViewHolder(holder: PlacesViewHolder, position: Int) {

        val place = placesList[position]
        holder.tv_placename?.text = place.place.name


        holder.tv_placename?.setOnClickListener {

            selectedItem = position
            ObservableObject.getInstance().updateValue(place.place)
            notifyDataSetChanged()

        }

        if(position == selectedItem) {
            holder.tv_placename?.background = context.getDrawable(R.drawable.nearby_places_selected)
            holder.tv_placename?.setTextColor(Color.parseColor("#ffffff"))
        }else{
            holder.tv_placename?.background = context.getDrawable(R.drawable.nearby_places_drawable)
            holder.tv_placename?.setTextColor(Color.parseColor("#D7533F"))
        }
    }


    inner class PlacesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){


        internal var tv_placename: TextView? = null

        init {


            tv_placename = itemView.findViewById<View>(R.id.tv_places) as TextView?

        }

    }
}