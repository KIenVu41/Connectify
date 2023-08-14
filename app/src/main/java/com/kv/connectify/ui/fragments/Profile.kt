package com.kv.connectify.ui.fragments

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.kv.connectify.R
import com.kv.connectify.databinding.FragmentProfileBinding
import com.kv.connectify.databinding.HomeItemsBinding
import com.kv.connectify.databinding.ProfileImageItemsBinding
import com.kv.connectify.model.PostImageModel
import com.kv.connectify.utils.Constants

class Profile : Fragment() {

    private var isMyProfile = true
    private lateinit var userUID: String
    private lateinit var adapter:FirestoreRecyclerAdapter<PostImageModel,PostImageHolder>
    private lateinit var followersList: List<String>
    private lateinit var followingList: List<String>
    private lateinit var followingList_2: List<String>
    private var isFollowed = false
    private lateinit var userRef: DocumentReference
    private lateinit var myRef: DocumentReference
    private var count = 0
    private lateinit var binding: FragmentProfileBinding
    private var user: FirebaseUser? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        user?.uid?.let {
            myRef = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME)
                .document(it)
        }

        if (isMyProfile) {
            binding.editProfileImage.visibility = View.VISIBLE
            binding.followBtn.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
            binding.startChatBtn.visibility = View.GONE
        } else {
            binding.editProfileImage.visibility = View.GONE
            binding.followBtn.visibility = View.VISIBLE
        }
        userRef = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME).document(userUID)

        loadBasicData()
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(activity, 3)

        loadPostImages()

        binding.recyclerView.adapter = adapter
    }

    private fun init() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        val auth = FirebaseAuth.getInstance()
        user = auth.currentUser
    }

    private fun loadBasicData() {
        userRef.addSnapshotListener { value, error ->
            error?.let {
                return@addSnapshotListener
            }
            value?.let {
                if (it.exists()) {
                    val name = it.getString("name")
                    val status = it.getString("status")
                    val profileURL = it.getString("profileImage")

                    binding.nameTv.text = name
                    binding.toolbarNameTV.text = name
                    binding.statusTV.text = status

                    followersList = it.get("followers") as List<String>
                    followingList = it.get("following") as List<String>

                    binding.followersCountTv.text = "${followersList.size}"
                    binding.followingCountTv.text = "${followingList.size}"

                    activity?.let {
                        Glide.with(it.applicationContext)
                            .load(profileURL)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    val bimap = (resource as BitmapDrawable).bitmap
                                    return false
                                }
                            })
                            .timeout(6500)
                            .into(binding.profileImage)
                    }
                    user?.uid?.let {
                        if (followersList.contains(it)) {
                            binding.followBtn.text = activity?.resources?.getString(R.string.unfollow)
                            isFollowed = true
                            binding.startChatBtn.visibility = View.VISIBLE
                        } else {
                            isFollowed = false
                            binding.followBtn.text = activity?.resources?.getString(R.string.follow)
                            binding.startChatBtn.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun loadPostImages() {
        val reference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME).document(userUID)
        val query = reference.collection(Constants.POST_IMAGES)
        val options = FirestoreRecyclerOptions.Builder<PostImageModel>()
            .setQuery(query, PostImageModel::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<PostImageModel, PostImageHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostImageHolder {
                val bindingProfile = ProfileImageItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return PostImageHolder(bindingProfile)
            }

            override fun onBindViewHolder(
                holder: PostImageHolder,
                position: Int,
                model: PostImageModel
            ) {
                Glide.with(holder.bindingProfile.imageView.context.applicationContext)
                    .load(model.imageUrl)
                    .timeout(6500)
                    .into(holder.bindingProfile.imageView)
                count = itemCount
                binding.postCountTv.text = "$count"
            }

            override fun getItemCount(): Int {
                return super.getItemCount()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    inner class PostImageHolder(val bindingProfile: ProfileImageItemsBinding): RecyclerView.ViewHolder(bindingProfile.root)
}