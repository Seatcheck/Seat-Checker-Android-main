package app.rubbickcube.seatcheck.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import app.rubbickcube.seatcheck.fragments.AboutFragment
import app.rubbickcube.seatcheck.fragments.ReviewFragments

class SimpleFragmentPagerAdapter(private val mContext: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    // This determines the fragment for each tab
    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            ReviewFragments()
        } else if (position == 1) {
            AboutFragment()
        } else {
            ReviewFragments()
        }
    }

    // This determines the number of tabs
    override fun getCount(): Int {
        return 2
    }

    // This determines the title for each tab
    override fun getPageTitle(position: Int): CharSequence? {
        // Generate title based on item position
        when (position) {
            0 -> return "Review"
            1 -> return "About"
            2 -> return null
            else -> return null
        }
    }

}