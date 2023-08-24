package com.kv.connectify.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.kv.connectify.R
import com.kv.connectify.databinding.ActivityPostViewBinding

class PostViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        val action = intent.action
        val uri = intent.data
        uri?.let {
            val scheme = it.scheme
            val host = it.host
            val path = it.path
            val query = it.query
            it.lastPathSegment?.let { it1 -> FirebaseStorage.getInstance().reference.child(it1)
                .downloadUrl.addOnSuccessListener { it2 -> {
                    Glide.with(this)
                        .load(it2.toString())
                        .timeout(6500)
                        .into(binding.imageView)
                }}}
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, FragmentReplacerActivity::class.java))
        }
    }
}