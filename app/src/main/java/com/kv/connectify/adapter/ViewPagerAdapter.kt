package com.kv.connectify.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kv.connectify.ui.fragments.Add
import com.kv.connectify.ui.fragments.Home
import com.kv.connectify.ui.fragments.Notification
import com.kv.connectify.ui.fragments.Profile
import com.kv.connectify.ui.fragments.Search

class ViewPagerAdapter(fa: FragmentActivity, numOfTabs: Int): FragmentStateAdapter(fa) {

    var noOfTabs = numOfTabs

    override fun getItemCount(): Int {
        return noOfTabs
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return Home()
            }
            1 -> {
                return Search()
            }
            2 -> {
                return Add()
            }
            3 -> {
                return Notification()
            }
            4 -> {
                return Profile()
            }
            else -> {
                return Home()
            }
        }
    }
}