package com.kv.connectify.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.widget.Toast
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.kv.connectify.R
import com.kv.connectify.adapter.ViewPagerAdapter
import com.kv.connectify.databinding.ActivityMainBinding
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var pagerAdapter: ViewPagerAdapter? = null
    private var doubleBackToExitPressedOnce = false
    private var mHandler = Handler()
    private val mRunnable = object : Runnable {
        override fun run() {
            doubleBackToExitPressedOnce = false
        }
    }

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

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, this.resources?.getString(R.string.exit_click_twice), Toast.LENGTH_SHORT).show()
        mHandler.postDelayed(mRunnable, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(mRunnable)
    }
}