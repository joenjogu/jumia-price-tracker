package com.example.jpt_demo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PagerAdapter (fragmanager: FragmentManager) : FragmentPagerAdapter(fragmanager){
    override fun getItem(position: Int): Fragment {
        return when (position){
            0 -> Fragment_one()
            1 -> Fragment_two()
            else -> return Fragment_three()
        }
    }

    override fun getCount(): Int {
        return 3
    }


    override fun getPageTitle(position: Int): CharSequence? {
        return when (position){
            0 -> "BROWSER"
            1 -> "MY TRACKLIST"
            else -> return "ME"
        }
    }

}