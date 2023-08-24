package com.kv.connectify.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.kv.connectify.R
import com.kv.connectify.databinding.ActivityViewStoryBinding

class ViewStoryActivity : AppCompatActivity() {

    companion object {
        val VIDEO_URL_KEY = "videoURL"
        val FILE_TYPE = "file type"
    }

    private lateinit var binding: ActivityViewStoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra(VIDEO_URL_KEY)
        val type = intent.getStringExtra(FILE_TYPE)
        if (url.isNullOrEmpty()) {
            finish()
        }

        if (type != null) {
            if (type.contains("image")) {
                binding.imageView.visibility = View.VISIBLE
                binding.videoView.visibility = View.GONE
                Glide.with(applicationContext).load(url).into(binding.imageView)
            } else {
                binding.videoView.visibility = View.VISIBLE
                binding.imageView.visibility = View.GONE

                val item = url?.let { MediaItem.fromUri(it) }
                val player = SimpleExoPlayer.Builder(this).build()
                item?.let {
                  player.setMediaItem(it)
                  binding.videoView.player = player
                  player.play()
                }
            }
        }
    }
}