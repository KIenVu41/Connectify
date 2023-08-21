package com.kv.connectify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.kv.connectify.R
import com.kv.connectify.databinding.UserItemsBinding
import com.kv.connectify.model.Users

class UserAdapter(val list: List<Users>): RecyclerView.Adapter<UserAdapter.UserHolder>() {

    val user = FirebaseAuth.getInstance().currentUser
    lateinit var onUserClicked: OnUserClicked

    inner class UserHolder(val binding: UserItemsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val binding = UserItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        with(holder) {
            if (list.get(position).uid.equals(user?.uid)) {
                this.binding.relativeLayout.visibility = View.GONE
                this.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            } else {
                this.binding.relativeLayout.visibility = View.VISIBLE
            }
            this.binding.nameTV.text = list.get(position).name
            this.binding.statusTV.text = list.get(position).status

            Glide.with(holder.itemView.context.applicationContext)
                .load(list.get(position).profileImage)
                .placeholder(R.drawable.ic_person)
                .timeout(6500)
                .into(holder.binding.profileImage)

            this.itemView.setOnClickListener {
                onUserClicked.onClicked(list.get(position).uid)
            }
        }
    }

    interface OnUserClicked {
        fun onClicked(uid: String)
    }

    fun OnUserClicked(onUserClicked: OnUserClicked) {
        this.onUserClicked = onUserClicked
    }
}