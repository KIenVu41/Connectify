package com.kv.connectify.ui.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.gowtham.library.utils.CompressOption
import com.gowtham.library.utils.TrimType
import com.gowtham.library.utils.TrimVideo
import com.kv.connectify.R
import com.kv.connectify.databinding.ActivityStoryAddBinding
import com.kv.connectify.utils.Constants
import com.marsad.stylishdialogs.StylishAlertDialog
import java.io.File

class StoryAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryAddBinding
    private lateinit var user: FirebaseUser
    private lateinit var alertDialog: StylishAlertDialog
    private val SELECT_VIDEO = 101
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.data))

            binding.videoView.setVideoURI(uri)
            binding.videoView.start()

            binding.uploadStoryBtn.visibility = View.VISIBLE
            binding.uploadStoryBtn.setOnClickListener {
                binding.uploadStoryBtn.visibility = View.GONE

                binding.videoView.pause()

                uploadFileToStorage(uri, "video")
            }
        } else {
            Toast.makeText(this, resources?.getString(R.string.data_null), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            user = auth.currentUser!!
        }
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/* video/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, "image/* video/*")
        startActivityForResult(intent, SELECT_VIDEO)
    }

    private fun uploadFileToStorage(uri: Uri, type: String) {
        alertDialog = StylishAlertDialog(this, StylishAlertDialog.PROGRESS)
        alertDialog.setTitleText(this.resources?.getString(R.string.upload_title))
            .setCancelable(false)
        alertDialog.show()
        var fileName: String
        if (type.contains("image")) {
            fileName = "${System.currentTimeMillis()}.png"
            uploadImageToStorage(fileName, uri, type)
        } else {
            fileName = "${System.currentTimeMillis()}.mp4"
            uploadVideoToStorage(fileName, uri, type)
        }

    }

    private fun uploadVideoToStorage(fileName: String, uri: Uri, type: String) {
        val file = File(uri.path)
        if (!file.exists()) {
            return
        }
        val storageReference = FirebaseStorage.getInstance().reference
            .child("Stories/" + fileName)
        storageReference.putFile(Uri.fromFile(file)).addOnCompleteListener {
            if (it.isSuccessful) {
                it.result?.let { it1 -> {
                  it1.storage.downloadUrl.addOnSuccessListener { it2 -> {
                      uploadVideoDataToFirestore(it2.toString(), type)
                  } }
                } }
            } else {
                alertDialog.dismissWithAnimation()
                val error: String = it.exception?.message ?: ""
                Toast.makeText(
                    this@StoryAddActivity,
                    this.resources.getString(R.string.error) + error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun uploadImageToStorage(fileName: String, uri: Uri, type: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child("Stories/" + fileName)
        val addOnCompleteListener = storageReference.putFile(uri).addOnCompleteListener {
            if (it.isSuccessful) {
                it.result?.let {
                    it.storage.downloadUrl.addOnSuccessListener { it1 ->
                        {
                            uploadVideoDataToFirestore(it1.toString(), type)
                        }
                    }
                }
            } else {
                alertDialog.dismissWithAnimation()
                val error: String = it.exception?.message ?: ""
                Toast.makeText(
                    this@StoryAddActivity,
                    this.resources.getString(R.string.error) + error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun uploadVideoDataToFirestore(url: String, type: String) {
        val reference = FirebaseFirestore.getInstance().collection(Constants.STORIES)
        val id = reference.document().id
        val map:MutableMap<String, Any> = mutableMapOf()
        map.put("url", url)
        map.put("id", id)
        map.put("uid", user.uid)
        map.put("type", type)
        map.put("name", user.displayName ?: "")

        reference.document(id)
            .set(map)

        alertDialog.dismissWithAnimation()

        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == SELECT_VIDEO) {
            val uri = data?.data
            uri?.let {
                if (it.toString().contains("image")) {
                    binding.videoView.visibility = View.GONE
                    binding.imageView.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(it)
                        .into(binding.imageView)
                    binding.uploadStoryBtn.visibility = View.VISIBLE
                    binding.uploadStoryBtn.setOnClickListener {
                        binding.uploadStoryBtn.visibility = View.GONE
                        uploadFileToStorage(uri, "image")
                    }
                } else if (it.toString().contains("video")) {
                    TrimVideo.activity(uri.toString())
                        .setCompressOption(CompressOption())
                        .setTrimType(TrimType.MIN_MAX_DURATION)
                        .setMinToMax(5, 30)
                        .setHideSeekBar(true)
                        .start(this, startForResult)
                }
            }
        }
    }
}