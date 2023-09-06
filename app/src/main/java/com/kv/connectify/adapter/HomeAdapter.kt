package com.kv.connectify.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.kv.connectify.R
import com.kv.connectify.databinding.HomeItemsBinding
import com.kv.connectify.model.HomeModel
import com.kv.connectify.ui.activities.FragmentReplacerActivity
import com.kv.connectify.ui.fragments.Home
import java.util.Random

class HomeAdapter(var list: MutableList<HomeModel>, val activity: Activity, val onPressed: OnPressed ): RecyclerView.Adapter<HomeAdapter.HomeHolder>() {

    inner class HomeHolder(val binding: HomeItemsBinding): RecyclerView.ViewHolder(binding.root) {
        fun clickListener(position: Int, id: String, name: String, uid: String, likes: MutableList<String>, imageUrl: String ) {
            binding.commentBtn.setOnClickListener(View.OnClickListener {
                val intent = Intent(activity, FragmentReplacerActivity::class.java)
                intent.putExtra("id", id)
                intent.putExtra("uid", uid)
                intent.putExtra("isComment", true)
                activity.startActivity(intent)
            })

            binding.likeBtn.setOnCheckedChangeListener { buttonView, isChecked -> {
                onPressed.onLiked(position, id, uid, likes, isChecked)
            } }

            binding.shareBtn.setOnClickListener(View.OnClickListener {
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_TEXT, imageUrl);
                intent.type = "text/*";
                activity.startActivity(Intent.createChooser(intent, activity.resources?.getString(R.string.share_link)))
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeHolder {
        val binding = HomeItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HomeHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                val user = FirebaseAuth.getInstance().currentUser
                binding.nameTv.text= this.name
                binding.timeTv.text = this.timestamp.toString()
                val likeList = this.likes
                val count = likes?.size ?: 0

                if (count == 0) {
                    binding.likeCountTv.text = "0 " + activity.resources?.getString(R.string.like)
                } else if (count == 1) {
                    binding.likeCountTv.text = "${count} ${activity.resources?.getString(R.string.like)}"
                } else {
                    binding.likeCountTv.text = "${count} ${activity.resources?.getString(R.string.likes)}"
                }
                user?.let {
                    binding.likeBtn.isChecked = likeList?.contains(it.uid) ?: false
                }
                binding.descTv.text = this.description
                val random = Random()
                val color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
                Glide.with(activity.applicationContext)
                    .load(this.profileImage)
                    .placeholder(R.drawable.img_person)
                    .timeout(6500)
                    .into(binding.profileImage)
                Glide.with(activity.applicationContext)
                    .load(this.imageUrl)
                    .placeholder(ColorDrawable(color))
                    .timeout(7000)
                    .into(binding.imageView)
            }
            this.clickListener(position,
                list.get(position).id,
                list.get(position).name,
                list.get(position).uid,
                list.get(position).likes,
                list.get(position).imageUrl)
        }
    }

    interface OnPressed {
        fun onLiked(position: Int, id: String, uid: String, likeList: MutableList<String>, isChecked: Boolean)
        fun setCommentCount(textView: TextView)
    }
}