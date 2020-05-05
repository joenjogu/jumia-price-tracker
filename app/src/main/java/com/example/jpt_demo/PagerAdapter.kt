package com.example.jpt_demo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PagerAdapter (fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager){
    override fun getItem(position: Int): Fragment {
        return when (position){
            0 -> FragmentWebview()
            1 -> FragmentTracklist()
            else -> return FragmentMe()
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position){
            0 -> "SHOP"
            1 -> "MY TRACKLIST"
            else -> return "ME"
        }
    }
}
