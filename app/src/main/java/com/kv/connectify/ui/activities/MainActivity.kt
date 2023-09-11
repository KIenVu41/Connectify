package com.kv.connectify.ui.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kv.connectify.R
import com.kv.connectify.adapter.ViewPagerAdapter
import com.kv.connectify.databinding.ActivityMainBinding
import com.kv.connectify.ui.fragments.Search
import com.kv.connectify.utils.Constants
import com.kv.connectify.utils.SharedPrefs
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), Search.OnDataPass {

    internal lateinit var binding: ActivityMainBinding
    private var pagerAdapter: ViewPagerAdapter? = null
    private var doubleBackToExitPressedOnce = false
    private var mHandler = Handler()
    private val user = FirebaseAuth.getInstance().currentUser
    private val mRunnable = Runnable { doubleBackToExitPressedOnce = false }
    companion object {
        lateinit var USER_ID: String
        var IS_SEARCHED_USER = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.darkTheme)
        } else {
            setTheme(R.style.AppTheme)
        }
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

        val directory = SharedPrefs.customPrefs(this).getString(Constants.PREF_DIRECTORY, "")
        val bitmap: Bitmap? = loadProfileImage(directory ?: "")
        val drawable = BitmapDrawable(resources, bitmap)

        binding.tabLayout.addTab(binding.tabLayout.newTab().setIcon(drawable))
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
            4 -> return R.drawable.baseline_person_24
        }
        return R.drawable.ic_home_fill
    }

    private fun loadProfileImage(directory: String): Bitmap? {
        return try {
            val file = File(directory, "profile.pnd")
            BitmapFactory.decodeStream(FileInputStream(file))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        if (binding.viewPager.currentItem == 4) {
            binding.viewPager.currentItem = 0
            IS_SEARCHED_USER = false
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, this.resources?.getString(R.string.exit_click_twice), Toast.LENGTH_SHORT).show()
        mHandler.postDelayed(mRunnable, 2000)
    }

    override fun onResume() {
        super.onResume()
        updateStatus(true)
    }

    override fun onPause() {
        updateStatus(false)
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacks(mRunnable)
    }

    private fun updateStatus(status: Boolean) {
        val map:MutableMap<String, Any> = mutableMapOf()
        map["online"] = status
        user?.let {
            FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME)
                .document(it.uid).update(map)
        }

    }

    override fun onChange(uid: String) {
        USER_ID = uid
        IS_SEARCHED_USER = true
        binding.viewPager.currentItem = 4
    }
}