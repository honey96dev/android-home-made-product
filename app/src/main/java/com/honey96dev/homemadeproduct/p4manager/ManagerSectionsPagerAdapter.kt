package com.honey96dev.homemadeproduct.p4manager


import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log

import java.util.ArrayList

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class ManagerSectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    internal var sections: ArrayList<Fragment>

    init {
        sections = ArrayList()
        sections.add(ManagerProductListFragment.newInstance())
        sections.add(ManagerOrderListFragment.newInstance())
    }

    override fun getItem(position: Int): Fragment {
        Log.e("position", position.toString())
        return sections[position]
    }

    override fun getCount(): Int {
        return sections.size
    }
}
