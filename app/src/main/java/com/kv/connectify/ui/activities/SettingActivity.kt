package com.kv.connectify.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.kv.connectify.R
import com.kv.connectify.databinding.ActivitySettingBinding

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListener()
    }

    private fun setListener() {
        binding.exitBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
        }
        binding.switchDarkmode.setOnCheckedChangeListener { buttonView, isChecked ->

        }
        binding.ivBack.setOnClickListener {
            super.onBackPressed()
        }
        binding.ivQr.setOnClickListener {

        }
        binding.tvQr.setOnClickListener {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeListener()
    }

    private fun removeListener() {
        binding.exitBtn.setOnClickListener(null)
        binding.ivQr.setOnClickListener(null)
        binding.tvQr.setOnClickListener(null)
        binding.ivBack.setOnClickListener(null)
        binding.switchDarkmode.setOnCheckedChangeListener(null)
    }
}