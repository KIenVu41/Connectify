package com.kv.connectify.ui.fragments

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.kv.connectify.R
import com.kv.connectify.databinding.FragmentProfileBinding

class Profile : Fragment() {

    private var isMyProfile = true
    private lateinit var userUID: String
//    private lateinit var adapter:FirestoreRecyclerAdapter<>
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
        loadBasicData()
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
}