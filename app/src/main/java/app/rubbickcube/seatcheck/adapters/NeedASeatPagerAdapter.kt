package app.rubbickcube.seatcheck.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import app.rubbickcube.seatcheck.fragments.*

class NeedASeatPagerAdapter(private val mContext: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    // This determines the fragment for each tab
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FetchPostFragment()
            1 -> FetchPostOnMapFragment()
            else -> FetchPostFragment()
        }
    }

    // This determines the number of tabs
    override fun getCount(): Int {
        return 2
    }



}