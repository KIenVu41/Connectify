package com.kv.connectify.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.kv.connectify.R
import com.kv.connectify.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        Handler().postDelayed(Runnable {
            user?.let { startActivity(Intent(this@SplashActivity, MainActivity::class.java)) } ?: startActivity(Intent(this@SplashActivity, FragmentReplacerActivity::class.java))
            finish()
        }, 2500)
    }
}