package com.kv.connectify.adapter

import android.app.Activity
import android.os.Build
import android.provider.SyncStateContract.Constants
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kv.connectify.R
import com.kv.connectify.databinding.ChatUserItemsBinding
import com.kv.connectify.model.ChatUserModel
import java.util.Date

class ChatUserAdapter(val activity: Activity, val list: MutableList<ChatUserModel>): RecyclerView.Adapter<ChatUserAdapter.ChatUserHolder>() {

    lateinit var startChat:OnStartChat

    inner class ChatUserHolder(val binding: ChatUserItemsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUserHolder {
        val binding = ChatUserItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatUserHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ChatUserHolder, position: Int) {
        fetchImageUri(list.get(position).uid, holder)
        holder.binding.timeTv.text = calculateTime(list.get(position).time)
        holder.binding.messageTV.text = list.get(position).lastMessage
        holder.itemView.setOnClickListener {
            startChat.clicked(position, list[position].uid, list[position].id)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateTime(date: Date): String {
        val millis = date.toInstant().toEpochMilli()
        return DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), 60000, DateUtils.FORMAT_ABBREV_TIME).toString()
    }

    private fun fetchImageUri(uids: MutableList<String>, holder: ChatUserHolder) {
        var oppositeUID: String
        val user = FirebaseAuth.getInstance().currentUser
        user?.let { it ->
            if (!uids.get(0).equals(it.uid)) {
                oppositeUID = uids.get(0)
            } else {
                oppositeUID = uids.get(1)
            }

            FirebaseFirestore.getInstance().collection(com.kv.connectify.utils.Constants.COLLECTION_NAME).document(oppositeUID)
                .get().addOnCompleteListener { it1 ->
                    if (it1.isSuccessful) {
                        val snapshot = it1.result
                        Glide.with(activity.applicationContext).load(snapshot.getString("profileImage"))
                        holder.binding.nameTV.text = snapshot.getString("name")
                    } else {
                        Toast.makeText(activity, activity?.resources?.getString(R.string.error) + it1.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun OnStartChat(startChat: OnStartChat) {
        this.startChat = startChat
    }

    interface OnStartChat {
        fun clicked(position: Int, uids: MutableList<String>, chatID: String)
    }
}