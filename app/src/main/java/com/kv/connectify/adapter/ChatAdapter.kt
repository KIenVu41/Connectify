package com.kv.connectify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.kv.connectify.databinding.ActivityChatBinding
import com.kv.connectify.databinding.ChatItemsBinding
import com.kv.connectify.model.ChatModel

class ChatAdapter(val context: Context, val list: MutableList<ChatModel>): RecyclerView.Adapter<ChatAdapter.ChatHolder>() {

    inner class ChatHolder(val binding: ChatItemsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHolder {
        val binding = ChatItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            if (list.get(position).senderID.equals(it.uid)) {
                holder.binding.leftChat.visibility = View.GONE
                holder.binding.rightChat.visibility = View.VISIBLE
                holder.binding.rightChat.text = list.get(position).message
            } else {
                holder.binding.leftChat.visibility = View.VISIBLE
                holder.binding.rightChat.visibility = View.GONE
                holder.binding.leftChat.text = list.get(position).message
            }
        }
    }
}