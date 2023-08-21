package com.kv.connectify.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import com.kv.connectify.R
import com.kv.connectify.adapter.HomeAdapter
import com.kv.connectify.adapter.StoriesAdapter
import com.kv.connectify.databinding.FragmentHomeBinding
import com.kv.connectify.databinding.HomeItemsBinding
import com.kv.connectify.model.HomeModel
import com.kv.connectify.model.StoriesModel
import com.kv.connectify.utils.Constants

class Home : Fragment() {

    private val commentCount:MutableLiveData<Int> = MutableLiveData<Int>()
    private lateinit var binding: FragmentHomeBinding
    private var adapter: HomeAdapter? = null
    private var storiesAdapter: StoriesAdapter? = null
    private var list: MutableList<HomeModel>? = null
    private var storiesModelList: MutableList<StoriesModel>? = null
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
        clickListener()

        val onPressed = object : HomeAdapter.OnPressed {
            override fun setCommentCount(textView: TextView) {
                commentCount.observe(activity as LifecycleOwner) {
                    commentCount.value?.let { it1 ->
                        if (it1 == 0) {
                            textView.visibility = View.GONE
                        } else {
                            textView.visibility = View.VISIBLE
                        }

                        val builder = StringBuffer()
                        builder.append(activity?.resources?.getString(R.string.see_all))
                            .append(it1)
                            .append(activity?.resources?.getString(R.string.comments))
                        textView.text = builder
                    }
                }
            }

            override fun onLiked(
                position: Int,
                id: String,
                uid: String,
                likeList: MutableList<String>,
                isChecked: Boolean
            ) {
                val reference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME)
                    .document(uid)
                    .collection(Constants.POST_IMAGES)
                    .document(id)

                if (likeList.contains(user.uid)) {
                    likeList.remove(user.uid)
                } else {
                    likeList.add(user.uid)
                }

                val map: MutableMap<String, Any> = mutableMapOf()
                map.put("likes", likeList)

                reference.update(map)
            }
        }
        list = mutableListOf<HomeModel>()
        adapter = list?.let { activity?.let { it1 -> HomeAdapter(it, it1, onPressed) } }
        binding.recyclerView.adapter = adapter

        loadDataFromFirestore()

    }

    private fun clickListener() {
        binding.sendBtn.setOnClickListener {
           // val intent = Intent(activity, )
//            startActivity(intent)
        }
    }

    private fun init() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)

        binding.storiesRecyclerView.setHasFixedSize(true)
        binding.storiesRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        storiesModelList = mutableListOf<StoriesModel>()
        storiesModelList!!.add(StoriesModel("","","","",""))
        storiesAdapter = StoriesAdapter(activity!!, storiesModelList!!)
        binding.storiesRecyclerView.adapter = storiesAdapter

        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.let {
            user = it
        }
    }

    private fun loadDataFromFirestore() {
        val reference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME)
            .document(user.uid)
        val collectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME)
        reference.addSnapshotListener { value, error ->
            error?.let {
                return@addSnapshotListener
            }
            if (value == null) {
                return@addSnapshotListener
            }
            val uidList = value.get("following") as? List<String>
            if (uidList == null || uidList.isEmpty()) {
                return@addSnapshotListener
            }
             collectionReference.whereIn("uid", uidList)
                .addSnapshotListener snap1@ { value1, error1 ->
                    if (value1 == null) {
                        return@snap1
                    }
                    list?.clear()
                    for (snapshot1: QueryDocumentSnapshot in value1) {
                        if (!snapshot1.exists()) {
                            return@snap1
                        }
                        val model = snapshot1.toObject<HomeModel>(HomeModel::class.java)
                        list?.add(HomeModel(model.name, model.profileImage, model.imageUrl,
                        model.uid, model.description, model.id, model.timestamp, model.likes))
                        snapshot1.reference.collection("Comments").get()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    var map:MutableMap<String, Any> = mutableMapOf<String, Any>()
                                    for (commentSnapshot in it.result) {
                                        map = commentSnapshot.data
                                    }

                                    commentCount.value = map.size
                                }
                            }
                    }
                    adapter?.notifyDataSetChanged()
                }
            loadStories(uidList)
        }
    }

    private fun loadStories(followingList: List<String>) {
        val query = FirebaseFirestore.getInstance().collection(Constants.STORIES)
        query.whereIn("udi", followingList).addSnapshotListener { value, error ->
            value?.let {
                for (snapshot: QueryDocumentSnapshot in it) {
                    if (!it.isEmpty) {
                        val model = snapshot.toObject<StoriesModel>(StoriesModel::class.java)
                        storiesModelList?.add(model)
                    }
                }
                storiesAdapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.sendBtn.setOnClickListener(null)
    }
}