package com.kv.connectify.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import com.kv.connectify.R
import com.kv.connectify.adapter.NotificationAdapter
import com.kv.connectify.databinding.FragmentNotificationBinding
import com.kv.connectify.model.NotificationModel
import com.kv.connectify.utils.Constants

class Notification : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var user: FirebaseUser
    private var list:MutableList<NotificationModel>? = null
    private var adapter: NotificationAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun init() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        list = mutableListOf()
        adapter = NotificationAdapter(requireContext(), list!!)
        binding.recyclerView.adapter = adapter
        user = FirebaseAuth.getInstance().currentUser!!

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        loadNoti()
    }

    private fun loadNoti() {
        val reference = FirebaseFirestore.getInstance().collection(Constants.NOTIFICATIONS)
        reference.whereEqualTo("uid", user.uid)
            .orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null) {
                    if (value.isEmpty) {
                        return@addSnapshotListener
                    }
                }
                list?.clear()
                if (value != null) {
                    for (snapshot: QueryDocumentSnapshot in value) {
                        val model = snapshot.toObject<NotificationModel>(NotificationModel::class.java)
                        list?.add(model)
                    }
                    adapter?.notifyDataSetChanged()
                }
            }
    }
}