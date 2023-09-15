package com.kv.connectify.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.kv.connectify.R
import com.kv.connectify.adapter.ChatAdapter
import com.kv.connectify.databinding.ActivityChatBinding
import com.kv.connectify.model.ChatModel
import com.kv.connectify.utils.Constants
import com.kv.connectify.utils.SharedPrefs

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var user:FirebaseUser
    private var adapter:ChatAdapter? = null
    private var list:MutableList<ChatModel>? = null
    private var chatID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        user = FirebaseAuth.getInstance().currentUser!!
        val isDarkMode = SharedPrefs.customPrefs(this).getBoolean(Constants.DARKMODE_KEY + user?.uid, false)
        if (isDarkMode) {
            setTheme(R.style.darkTheme)
        } else {
            setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        loadUserData()
        loadMessages()

        binding.sendBtn.setOnClickListener {
            val message = binding.chatET.text.toString().trim()
            if (message.isEmpty()) {
                return@setOnClickListener
            }
            val reference = FirebaseFirestore.getInstance().collection(Constants.MESSAGES)
            val map:MutableMap<String, Any> = mutableMapOf()
            map.put("lastMessage", message)
            map.put("time", FieldValue.serverTimestamp())
            reference.document(chatID).update(map)
            val messageID = reference.document(chatID)
                .collection(Constants.MESSAGES)
                .document()
                .id
            val messageMap :MutableMap<String, Any> = mutableMapOf()
            messageMap.put("id", messageID)
            messageMap.put("message", message)
            messageMap.put("senderID", user.getUid())
            messageMap.put("time", FieldValue.serverTimestamp())

            reference.document(chatID).collection("Messages").document(messageID).set(messageMap)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        binding.chatET.setText("");
                    } else {
                        Toast.makeText(this, resources?.getString(R.string.smt_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    private fun init() {
        list = mutableListOf()
        adapter = ChatAdapter(this, list!!)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.nameTV.setOnClickListener {
            val intent = Intent(this, VideoCallActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val oppositeUID = intent.getStringExtra("uid")
        FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME).document(oppositeUID ?: "")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (value == null || !value.exists()) {
                    return@addSnapshotListener
                }

                val isOnline = value.getBoolean("online")
                binding.statusTV.text = if (isOnline == true) "Online" else "Offline"
                Glide.with(applicationContext).load(value.getString("profileImage")).into(binding.profileImage)
                binding.nameTV.text = value.getString("name")
            }
    }

    private fun loadMessages() {
        chatID = intent.getStringExtra("id").toString()

        val reference = FirebaseFirestore.getInstance()
            .collection(Constants.MESSAGES)
            .document(chatID)
            .collection(Constants.MESSAGES)

        reference.orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value == null || value.isEmpty) {
                    return@addSnapshotListener
                }
                list?.clear()
                for (snapshot in value) {
                    val model = snapshot.toObject<ChatModel>(ChatModel::class.java)
                    list?.add(model)
                }
                adapter?.notifyDataSetChanged()
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding.sendBtn.setOnClickListener(null)
    }
}