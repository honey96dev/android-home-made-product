package com.honey96dev.homemadeproduct.p4customer


import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log

import java.util.ArrayList

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class CustomerSectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    internal var sections: ArrayList<Fragment>

    init {
        sections = ArrayList()
        sections.add(CustomerProductListFragment.newInstance())
        sections.add(CustomerCartListFragment.newInstance())
    }

    override fun getItem(position: Int): Fragment {
        Log.e("position", position.toString())
        return sections[position]
    }

    override fun getCount(): Int {
        return sections.size
    }
}
