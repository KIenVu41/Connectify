package com.kv.connectify.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import com.kv.connectify.R
import com.kv.connectify.adapter.UserAdapter
import com.kv.connectify.databinding.FragmentSearchBinding
import com.kv.connectify.model.Users
import com.kv.connectify.utils.Constants

class Search : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: UserAdapter
    private lateinit var reference: CollectionReference
    lateinit var onDataPass: OnDataPass
    private var list:MutableList<Users>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onDataPass = context as OnDataPass
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        reference = FirebaseFirestore.getInstance().collection(Constants.USERS)

        loadUserData()
        searchUser()
        clickListener()
    }

    private fun init() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        list = mutableListOf()
        adapter = UserAdapter(list!!)
        binding.recyclerView.adapter = adapter
    }

    private fun loadUserData() {
        reference.addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (value == null) {
                return@addSnapshotListener
            }

            list?.clear()
            for (snapshot: QueryDocumentSnapshot in value) {
                val users = snapshot.toObject<Users>(Users::class.java)
                list?.add(users)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun searchUser() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                reference.orderBy("search").startAt(query).endAt(query + "\uf8ff")
                    .get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            list?.clear()
                            for (snapshot: DocumentSnapshot in it.result) {
                                if (!snapshot.exists()) {
                                    return@addOnCompleteListener
                                }
                                val users = snapshot.toObject<Users>(Users::class.java)
                                users?.let { it1 -> list?.add(it1) }
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.equals("")) {
                    loadUserData()
                }
                return false
            }
        })
    }

    private fun clickListener() {
        adapter.OnUserClicked(object : UserAdapter.OnUserClicked {
            override fun onClicked(uid: String) {
                onDataPass.onChange(uid)
            }
        })
    }

    interface OnDataPass {
        fun onChange(uid: String)
    }
}