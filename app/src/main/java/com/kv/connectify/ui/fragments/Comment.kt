package com.kv.connectify.ui.fragments

import android.os.Bundle
import android.provider.SyncStateContract.Constants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.kv.connectify.R
import com.kv.connectify.adapter.CommentAdapter
import com.kv.connectify.databinding.FragmentCommentBinding
import com.kv.connectify.model.CommentModel

class Comment : Fragment() {

    private lateinit var binding: FragmentCommentBinding
    private var commentAdapter: CommentAdapter? = null
    private var list: MutableList<CommentModel>? = null
    private lateinit var user:FirebaseUser
    private lateinit var reference: CollectionReference
    private var id = ""
    private var uid = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        reference = FirebaseFirestore.getInstance().collection(com.kv.connectify.utils.Constants.USERS)
            .document(uid)
            .collection(com.kv.connectify.utils.Constants.POST_IMAGES)
            .document(id)
            .collection(com.kv.connectify.utils.Constants.COMMENTS)

        loadCommentData()
        clickListener()
    }

    private fun init() {
        user = FirebaseAuth.getInstance().currentUser!!
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(context)
        list = mutableListOf()
        commentAdapter = CommentAdapter(requireContext(), list!!)
        binding.commentRecyclerView.adapter = commentAdapter

        if (arguments == null) {
            return
        }
        id = arguments?.getString("id").toString() ?: ""
        uid = arguments?.getString("uid").toString() ?: ""
    }

    private fun loadCommentData() {
        reference.addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (value == null) {
                Toast.makeText(activity, activity?.resources?.getString(R.string.no_comment), Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            for (snapshot: DocumentSnapshot in value) {
                val model = snapshot.toObject(CommentModel::class.java)
                if (model != null) {
                    list?.add(model)
                }
            }
            commentAdapter?.notifyDataSetChanged()
        }
    }

    private fun clickListener() {
        binding.sendBtn.setOnClickListener {
            val comment = binding.commentET.text.toString()
            if (comment.isEmpty()) {
                Toast.makeText(activity, activity?.resources?.getString(R.string.comment), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val commentID = reference.document().id

            val map: MutableMap<String, Any> = mutableMapOf()
            map["uid"] = user.uid
            map["comment"] = comment
            map["commentID"] = commentID
            map["postID"] = id

            map["name"] = user.displayName ?: ""
            map["profileImageUrl"] = user.photoUrl.toString()

            reference.document(commentID)
                .set(map)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        binding.commentET.setText("")
                    } else {
                        val exception = it.exception
                        Toast.makeText(activity, activity?.resources?.getString(R.string.comment_failed) + exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.sendBtn.setOnClickListener(null)
    }
}