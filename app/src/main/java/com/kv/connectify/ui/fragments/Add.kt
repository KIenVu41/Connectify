package com.kv.connectify.ui.fragments

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kv.connectify.R
import com.kv.connectify.adapter.GalleryAdapter
import com.kv.connectify.databinding.FragmentAddBinding
import com.kv.connectify.model.GalleryImages
import com.kv.connectify.ui.activities.MainActivity
import com.kv.connectify.utils.Constants
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.Console
import java.io.File
import java.util.jar.Manifest

class Add : Fragment() {

    private lateinit var binding: FragmentAddBinding
    private lateinit var list: MutableList<GalleryImages>
    private lateinit var adapter: GalleryAdapter
    private lateinit var imageUri: Uri
    private lateinit var dialog: Dialog
    private var user: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        binding.recyclerView.layoutManager = GridLayoutManager(activity, 3)
        binding.recyclerView.setHasFixedSize(true)
        list = mutableListOf<GalleryImages>()
        adapter = GalleryAdapter(list)
        binding.recyclerView.adapter = adapter

        clickListener()
    }

    private fun init() {
        user = FirebaseAuth.getInstance().currentUser
        activity?.let {
            dialog = Dialog(it)
            dialog.setContentView(R.layout.loading_dialog)
            dialog.window?.setBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.dialog_bg, null))
            dialog.setCancelable(false)
        }
    }

    private fun clickListener() {
        adapter.SendImage(object : GalleryAdapter.SendImage {
            override fun onSend(picUri: Uri) {
                activity?.let {
                    CropImage.activity(picUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(4, 3)
                        .start(it, this@Add)
                }
            }
        })
        binding.nextBtn.setOnClickListener {
            val storage = FirebaseStorage.getInstance()
            val storageReference = storage.reference.child(Constants.POST_IMAGES + "/" + System.currentTimeMillis())
            if(::dialog.isInitialized) {
                dialog.show()
            }

            storageReference.putFile(imageUri)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        storageReference.downloadUrl.addOnSuccessListener { uploadData(it.toString()) }
                    } else {
                        if (::dialog.isInitialized) {
                            if (dialog.isShowing && !requireActivity().isFinishing) {
                                dialog.dismiss()
                            }
                        }
                        Toast.makeText(activity, activity?.resources?.getString(R.string.upload_failed) + it.exception?.message , Toast.LENGTH_SHORT).show()
                    }
                }
        }
        binding.backBtn.setOnClickListener {
            (activity as? MainActivity)?.binding?.tabLayout?.getTabAt(0)?.select()
        }
    }

    private fun uploadData(imageURL: String?) {
        val reference = FirebaseFirestore.getInstance().collection(Constants.COLLECTION_NAME)
            .document(user!!.uid).collection(Constants.POST_IMAGES)

        val id = reference.document().id
        val description = binding.descriptionET.text.toString()
        val likes = listOf<String>()
        val map: MutableMap<String, Any> = mutableMapOf()
        map.put("id", id)
        map.put("description", description)
        map.put("imageUrl", imageURL ?: " ")
        map.put("timestamp", FieldValue.serverTimestamp())

        map.put("name", user?.displayName ?: " ")
        map.put("profileImage", user?.photoUrl.toString() ?: " ")

        map.put("likes", likes)
        map.put("uid", user?.uid ?: " ")

        reference.document(id).set(map)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(activity, activity?.resources?.getString(R.string.uploaded), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(activity, activity?.resources?.getString(R.string.error) + it.exception?.message , Toast.LENGTH_SHORT).show()
                }
                if (::dialog.isInitialized) {
                    if (dialog.isShowing && !requireActivity().isFinishing) {
                        dialog.dismiss()
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        activity?.runOnUiThread {
            Dexter.withContext(activity)
                .withPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object: MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        p0?.let {
                            val file = File(Environment.getExternalStorageDirectory().toString() + "/Download")
                            if (file.exists()) {
                                val files = file.listFiles()
                                list.clear()

                                for (file1 in files) {
                                    if (file1.absolutePath.endsWith(".jpg") || file1.absolutePath.endsWith(".png")) {
                                        list.add(GalleryImages(Uri.fromFile(file1)))
                                        adapter.notifyDataSetChanged()
                                    }
                                }
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                    }
                }).check()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                result?.let {
                    imageUri = it.uri
                    activity?.let { it1 ->
                        Glide.with(it1)
                            .load(imageUri)
                            .into(binding.imageView)
                    }
                    binding.imageView.visibility = View.VISIBLE
                    binding.nextBtn.visibility= View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.nextBtn.setOnClickListener(null)
        binding.backBtn.setOnClickListener(null)
    }
}