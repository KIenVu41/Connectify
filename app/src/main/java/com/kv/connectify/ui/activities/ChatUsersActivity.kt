package com.kv.connectify.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.kv.connectify.R
import com.kv.connectify.adapter.ChatUserAdapter
import com.kv.connectify.databinding.ActivityChatUsersBinding
import com.kv.connectify.databinding.ChatUserItemsBinding
import com.kv.connectify.model.ChatUserModel
import com.kv.connectify.utils.Constants

class ChatUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatUsersBinding
    private var adapter: ChatUserAdapter? = null
    private lateinit var user:FirebaseUser
    private var list:MutableList<ChatUserModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        fetchUserData()
        clickListener()
    }

    private fun init() {
        list = mutableListOf()
        adapter = ChatUserAdapter(this, list!!)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        user = FirebaseAuth.getInstance().currentUser!!
    }

    private fun fetchUserData() {
        val reference = FirebaseFirestore.getInstance().collection(Constants.MESSAGES)
        reference.whereArrayContains("uid", user.uid)
            .addSnapshotListener { value, error ->
                if (error != null)
                    return@addSnapshotListener
                if (value == null)
                    return@addSnapshotListener
                if (value.isEmpty())
                    return@addSnapshotListener

                list?.clear()
                for (snapshot in value) {
                    if (snapshot.exists()) {
                        val model = snapshot.toObject<ChatUserModel>(ChatUserModel::class.java)
                        list?.add(model)
                    }
                }
                adapter?.notifyDataSetChanged()
            }
    }

    private fun clickListener() {
        adapter?.OnStartChat(object : ChatUserAdapter.OnStartChat {
            override fun clicked(position: Int, uids: MutableList<String>, chatID: String) {
                var oppositeUID: String
                if (!uids.get(0).equals(user.uid)) {
                    oppositeUID = uids.get(0)
                } else {
                    oppositeUID = uids.get(1)
                }

                val intent = Intent(this@ChatUsersActivity, ChatActivity::class.java)
                intent.putExtra("uid", oppositeUID)
                intent.putExtra("id", chatID)
                startActivity(intent)
            }
        })
    }
}