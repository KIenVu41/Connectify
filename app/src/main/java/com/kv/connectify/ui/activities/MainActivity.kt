package com.kv.connectify.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kv.connectify.R
import com.kv.connectify.adapter.ViewPagerAdapter
import com.kv.connectify.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var pagerAdapter: ViewPagerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addTabs()
    }

    private fun addTabs() {
        val drawableResList = listOf(R.drawable.ic_home, R.drawable.ic_search, R.drawable.ic_add, R.drawable.ic_heart)

        for (i in 0 until 4) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setIcon(drawableResList[i]))
        }

        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL;
        binding.tabLayout.tabMode = TabLayout.MODE_FIXED;

        pagerAdapter = ViewPagerAdapter(this, binding.tabLayout.tabCount)
        binding.viewPager.adapter = pagerAdapter
        binding.tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_home_fill)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) {
            tab, position -> tab.setIcon(getDrawableByPos(position))
        }.attach()
    }

    private fun getDrawableByPos(position: Int): Int {
        when(position) {
            0 -> return R.drawable.ic_home_fill
            1 -> return R.drawable.ic_search
            2 -> return R.drawable.ic_add
            3 -> return R.drawable.ic_heart_fill
        }
        return R.drawable.ic_home_fill
    }
}