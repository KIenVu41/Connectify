package com.kv.connectify.ui.fragments

import android.app.Activity.ACTIVITY_SERVICE
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kv.connectify.R
import com.kv.connectify.databinding.FragmentProfileBinding
import com.kv.connectify.databinding.HomeItemsBinding
import com.kv.connectify.databinding.ProfileImageItemsBinding
import com.kv.connectify.model.PostImageModel
import com.kv.connectify.ui.activities.ChatActivity
import com.kv.connectify.ui.activities.MainActivity
import com.kv.connectify.ui.activities.SettingActivity
import com.kv.connectify.utils.Constants
import com.marsad.stylishdialogs.StylishAlertDialog
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView

class Profile : Fragment() {

    private var isMyProfile = true
    private lateinit var userUID: String
    private lateinit var adapter:FirestoreRecyclerAdapter<PostImageModel,PostImageHolder>
    private var followersList: MutableList<String>? = null
    private var followingList: MutableList<String>? = null
    private var followingList_2: MutableList<String>? = null
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

        if (MainActivity.IS_SEARCHED_USER) {
            isMyProfile = false
            userUID = MainActivity.USER_ID
            loadData()
        } else {
            isMyProfile = true
            userUID = user?.uid ?: ""
        }

        if (isMyProfile) {
            binding.editProfileImage.visibility = View.VISIBLE
            binding.followBtn.visibility = View.GONE
            binding.countLayout.visibility = View.VISIBLE
            binding.startChatBtn.visibility = View.GONE
        } else {
            binding.editProfileImage.visibility = View.GONE
            binding.followBtn.visibility = View.VISIBLE
            binding.settingBtn.visibility = View.GONE
        }
        userRef = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME).document(userUID)

        loadBasicData()
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = GridLayoutManager(activity, 3)

        loadPostImages()

        binding.recyclerView.adapter = adapter
        clickListener()
    }

    private fun clickListener() {
        binding.followBtn.setOnClickListener {
            if (isFollowed) {
                user?.let { it1 -> followersList?.remove(it1.uid) }
                followingList_2?.remove(userUID)
                val map_2: MutableMap<String, Any> = mutableMapOf()
                followingList_2?.let { it1 -> map_2.put("following", it1) }

                val map: MutableMap<String, Any> = mutableMapOf()
                followersList?.let { it1 -> map.put("followers", it1) }

                userRef.update(map).addOnCompleteListener {
                    if (it.isSuccessful) {
                        binding.followBtn.text = activity?.resources?.getString(R.string.follow)

                        myRef.update(map_2).addOnCompleteListener { it2 ->
                            if (it2.isSuccessful) {
                                Toast.makeText(
                                    requireActivity(),
                                    requireActivity().resources.getString(R.string.unfollow),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } else {
                createNotification()
                user?.let { it1 -> followersList?.add(it1.uid) }
                followingList_2?.add(userUID)

                val map_2:MutableMap<String, Any> = mutableMapOf()
                followingList_2?.let { it1 -> map_2.put("following", it1) }

                val map:MutableMap<String, Any> = mutableMapOf()
                followersList?.let { it1 -> map.put("followers", it1) }

                userRef.update(map).addOnCompleteListener {
                    if (it.isSuccessful) {
                        binding.followBtn.text = activity?.resources?.getString(R.string.unfollow)

                        myRef.update(map_2).addOnCompleteListener { it2 ->
                            if (it2.isSuccessful) {
                                Toast.makeText(
                                    requireActivity(),
                                    requireActivity().resources.getString(R.string.follow),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }

        binding.editProfileImage.setOnClickListener {
            activity?.let { it1 ->
                CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(it1, this@Profile)
            }
        }
        binding.startChatBtn.setOnClickListener {
            queryChat()
        }
        binding.settingBtn.setOnClickListener {
            val intent = Intent(activity, SettingActivity::class.java)
            startActivity(intent)
        }
    }

    private fun queryChat() {
        val alertDialog = StylishAlertDialog(context, StylishAlertDialog.PROGRESS)
        alertDialog.titleText = activity?.resources?.getString(R.string.start_chat)
        alertDialog.cancellable = false
        alertDialog.show()

        val reference = FirebaseFirestore.getInstance().collection(Constants.MESSAGES)
        reference.whereArrayContains("uid", userUID)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val snapshot = it.result
                    if (snapshot.isEmpty) {
                        startChat(alertDialog)
                    } else {
                        alertDialog.dismissWithAnimation()
                        for (snapshotChat in snapshot) {
                            val intent = Intent(activity, ChatActivity::class.java)
                            intent.putExtra("uid", userUID)
                            intent.putExtra("id", snapshotChat.id)
                            startActivity(intent)
                        }
                    }
                } else {
                    alertDialog.dismissWithAnimation()
                }
            }
    }

    private fun startChat(alertDialog: StylishAlertDialog) {
        var reference = FirebaseFirestore.getInstance().collection(Constants.MESSAGES)
        var list:MutableList<String> = mutableListOf()
        list.add(0, user?.uid ?: "")
        list.add(1, userUID)

        val pushID = reference.document().id
        val map:MutableMap<String, Any> = mutableMapOf()
        map.put("id", pushID)
        map.put("lastMessage", "Hi")
        map.put("time", FieldValue.serverTimestamp())
        map.put("uid", list)

        reference.document(pushID).update(map).addOnCompleteListener {
            if (!it.isSuccessful) {
                reference.document(pushID).set(map)
            }
        }

        val messageRef = FirebaseFirestore.getInstance()
            .collection(Constants.MESSAGES)
            .document(pushID)
            .collection(Constants.MESSAGES)

        val messageID = messageRef.document().id
        val messageMap:MutableMap<String, Any> = mutableMapOf()

        messageMap.put("id", messageID)
        messageMap.put("message", "Hi")
        messageMap.put("senderID", user?.getUid() ?: "")
        messageMap.put("time", FieldValue.serverTimestamp())

        messageRef.document(messageID).set(messageMap)

        Handler().postDelayed(Runnable {
            alertDialog.dismissWithAnimation()
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra("uid", userUID)
            intent.putExtra("id", pushID)
            startActivity(intent)
        }, 3000)
    }

    private fun init() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        val auth = FirebaseAuth.getInstance()
        user = auth.currentUser
    }

    private fun loadData() {
        myRef.addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value == null || !value.exists()) {
                return@addSnapshotListener
            }
            followingList_2 = value.get("following") as MutableList<String>
        }
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

                    followersList = it.get("followers") as? MutableList<String>?
                    followingList = it.get("following") as? MutableList<String>?

                    binding.followersCountTv.text = "${followersList?.size ?: 0}"
                    binding.followingCountTv.text = "${followingList?.size ?: 0}"

                    activity?.let { it1 ->
                        Glide.with(it1.applicationContext)
                            .load(profileURL)
                            .placeholder(R.drawable.img_person)
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
                    user?.uid?.let { it2 ->
                        if (followersList?.contains(it2) == true) {
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

    private fun createNotification() {
        val reference = FirebaseFirestore.getInstance().collection(Constants.NOTIFICATIONS)
        val id = reference.document().id
        val map:MutableMap<String, Any> = mutableMapOf()
        map.put("time", FieldValue.serverTimestamp())
        map.put("notification", user?.displayName + activity?.resources?.getString(R.string.follow_noti) ?: " followed you.")
        map.put("id", id)
        map.put("uid", userUID)

        reference.document(id).set(map)
    }

    private fun removeListener() {
        binding.followBtn.setOnClickListener(null)
        binding.editProfileImage.setOnClickListener(null)
        binding.startChatBtn.setOnClickListener(null)
        binding.settingBtn.setOnClickListener(null)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            result?.let {
                val uri = it.uri
                uploadImage(uri)
            }
        }
    }

    private fun uploadImage(uri: Uri) {
        val reference = FirebaseStorage.getInstance().reference.child(Constants.PROFILE_IMAGES)
        reference.putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    reference.downloadUrl
                        .addOnSuccessListener { it1 ->
                            val imageURL = it1.toString()
                            val request = UserProfileChangeRequest.Builder()
                            request.photoUri = it1
                            user?.updateProfile(request.build())
                            val map:MutableMap<String, Any> = mutableMapOf()
                            map.put("profileImage", imageURL)
                            user?.let { it3 ->
                                FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME)
                                    .document(it3.uid)
                                    .update(map).addOnCompleteListener { it4 ->
                                        if (it4.isSuccessful) {
                                            Toast.makeText(requireActivity(), requireActivity().resources?.getString(R.string.updated_successful), Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(requireActivity(), requireActivity().resources?.getString(R.string.error) + it4.exception?.message , Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }
                } else {
                    Toast.makeText(requireActivity(), requireActivity().resources?.getString(R.string.error) + it.exception?.message , Toast.LENGTH_SHORT).show()
                }
            }
    }

    inner class PostImageHolder(val bindingProfile: ProfileImageItemsBinding): RecyclerView.ViewHolder(bindingProfile.root)
}