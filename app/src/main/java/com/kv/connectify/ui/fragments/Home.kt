package com.kv.connectify.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kv.connectify.R
import com.kv.connectify.adapter.HomeAdapter
import com.kv.connectify.databinding.FragmentHomeBinding
import com.kv.connectify.databinding.HomeItemsBinding
import com.kv.connectify.model.HomeModel

class Home : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var adapter: HomeAdapter? = null
    private var list: List<HomeModel>? = null
    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        val onPressed = object : HomeAdapter.OnPressed {
            override fun setCommentCount(textView: TextView) {

            }

            override fun onLiked(
                position: Int,
                id: String,
                uid: String,
                likeList: List<String>,
                isChecked: Boolean
            ) {

            }
        }
        adapter = list?.let { activity?.let { it1 -> HomeAdapter(it, it1, onPressed) } }
        binding.recyclerView.adapter = adapter

        loadDataFromFirestore()

    }

    private fun init() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)

        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.let {
            user = it
        }
    }

    private fun loadDataFromFirestore() {

    }
}