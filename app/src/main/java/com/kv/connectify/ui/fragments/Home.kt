package com.kv.connectify.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.kv.connectify.R
import com.kv.connectify.adapter.HomeAdapter
import com.kv.connectify.adapter.StoriesAdapter
import com.kv.connectify.databinding.FragmentHomeBinding
import com.kv.connectify.databinding.HomeItemsBinding
import com.kv.connectify.model.HomeModel
import com.kv.connectify.model.StoriesModel
import com.kv.connectify.ui.activities.ChatUsersActivity
import com.kv.connectify.utils.Constants

class Home : Fragment() {

    private val commentCount:MutableLiveData<Int> = MutableLiveData<Int>()
    private lateinit var binding: FragmentHomeBinding
    private var adapter: HomeAdapter? = null
    private var storiesAdapter: StoriesAdapter? = null
    private var list: MutableList<HomeModel>? = null
    private var storiesModelList: MutableList<StoriesModel>? = null
    private lateinit var user: FirebaseUser
    lateinit var onDataPass: Search.OnDataPass
    private val barcodeLauncer = registerForActivityResult(ScanContract()) {
        if (it.contents != null && it.contents != user.uid) {
            onDataPass.onChange(it.contents)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onDataPass = context as Search.OnDataPass
    }

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
            val intent = Intent(activity, ChatUsersActivity::class.java )
            startActivity(intent)
        }
        binding.qrScanBtn.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("Scan a barcode")
            options.setCameraId(0)
            options.setBeepEnabled(false)
            options.setBarcodeImageEnabled(true)
            options.setOrientationLocked(true);
            options.setTimeout(60000)
            barcodeLauncer.launch(options)
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
        storiesAdapter = StoriesAdapter(requireActivity(), storiesModelList!!)
        binding.storiesRecyclerView.adapter = storiesAdapter

        val auth = FirebaseAuth.getInstance()
        auth.currentUser?.let {
            user = it
        }
    }

    private fun loadDataFromFirestore() {
        if (!::user.isInitialized) {
            return
        }
        val reference: DocumentReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME)
            .document(user.uid)

        val collectionReference: CollectionReference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME)

        reference.addSnapshotListener { value, error ->
            if (error != null) {
                Log.d("Error: ", error.message ?: "")
                return@addSnapshotListener
            }

            if (value == null)
                return@addSnapshotListener

            val uidList = value.get("following") as? List<String>

            if (uidList == null || uidList.isEmpty())
                return@addSnapshotListener

            collectionReference.whereIn("uid", uidList)
                .addSnapshotListener { value1, error1 ->
                    if (error1 != null) {
                        Log.d("Error: ", error1.message ?: "")
                    }

                    if (value1 == null)
                        return@addSnapshotListener

                    for (snapshot in value1) {
                        snapshot.getReference().collection(Constants.POST_IMAGES)
                            .addSnapshotListener { value11, error11 ->
                                if (error11 != null) {
                                    Log.d("Error: ", error11.message ?: "")
                                }

                                if (value11 == null)
                                    return@addSnapshotListener

                                list?.clear()

                                for (snapshot1 in value11) {
                                    if (!snapshot1.exists())
                                        return@addSnapshotListener

                                    val model = snapshot1.toObject(HomeModel::class.java)

                                    list?.add(
                                        HomeModel(
                                            model.name,
                                            model.profileImage,
                                            model.imageUrl,
                                            model.uid,
                                            model.description,
                                            model.id,
                                            model.timestamp,
                                            model.likes
                                        )
                                    )

                                    snapshot1.getReference().collection(Constants.COMMENTS).get()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                var map:MutableMap<String, Any> = mutableMapOf<String, Any>()
                                                for (commentSnapshot in task.result) {
                                                    map = commentSnapshot.data
                                                }

                                                commentCount.value = map.size
                                            }
                                        }
                                }
                                adapter?.notifyDataSetChanged()
                            }
                    }
                }
            loadStories(uidList)
        }
    }

    private fun loadStories(followingList: List<String>) {
        val query = FirebaseFirestore.getInstance().collection(Constants.STORIES)
        query.whereIn("udi", followingList).addSnapshotListener { value, _ ->
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
        binding.qrScanBtn.setOnClickListener(null)
        binding.sendBtn.setOnClickListener(null)
    }
}