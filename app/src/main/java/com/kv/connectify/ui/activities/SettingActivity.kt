package com.kv.connectify.ui.activities

import android.app.UiModeManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.kv.connectify.R
import com.kv.connectify.databinding.ActivitySettingBinding
import com.kv.connectify.databinding.DialogQrBinding
import com.kv.connectify.utils.Constants
import com.kv.connectify.utils.SharedPrefs
import com.kv.connectify.utils.SharedPrefs.set
import java.lang.Exception

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        val isDarkMode = SharedPrefs.customPrefs(this).getBoolean(Constants.DARKMODE_KEY + auth.currentUser?.uid, false)
        if (isDarkMode) {
            setTheme(R.style.darkTheme)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (isDarkMode) {
            binding.switchDarkmode.isChecked = true
        }
        setListener()
    }

    private fun setListener() {
        binding.exitBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
        }
        binding.switchDarkmode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                SharedPrefs.customPrefs(this)[Constants.DARKMODE_KEY + auth.currentUser?.uid] = true
                recreate()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                SharedPrefs.customPrefs(this)[Constants.DARKMODE_KEY + auth.currentUser?.uid] = false
            }
        }
        binding.ivBack.setOnClickListener {
            super.onBackPressed()
        }
        binding.ivQr.setOnClickListener {
            genQR()
        }
        binding.tvQr.setOnClickListener {
            binding.ivQr.performClick()
        }
    }

    private fun genQR() {
        auth.currentUser?.let {
            val uid = it.uid
            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            val binding = DialogQrBinding.inflate(LayoutInflater.from(this))
            builder.setView(binding.root)
            val qrDialog = builder.create()
            qrDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            qrDialog.setCanceledOnTouchOutside(true)
            try {
                val barcode = BarcodeEncoder()
                val bitmap = barcode.encodeBitmap(uid, BarcodeFormat.QR_CODE, 300, 300)
                Glide.with(this@SettingActivity).load(bitmap).into(binding.idIVQrcode)
            } catch (_: Exception) {}
            qrDialog.show()
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