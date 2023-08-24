package com.kv.connectify.adapter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.kv.connectify.R
import com.kv.connectify.databinding.StoriesLayoutBinding
import com.kv.connectify.model.StoriesModel
import com.kv.connectify.ui.activities.StoryAddActivity
import com.kv.connectify.ui.activities.ViewStoryActivity

class StoriesAdapter(val activity: Activity, val list:List<StoriesModel>) : RecyclerView.Adapter<StoriesAdapter.StoriesHolder>() {


    inner class StoriesHolder(val binding: StoriesLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesHolder {
        val binding = StoriesLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoriesHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: StoriesHolder, position: Int) {
        if (position == 0) {
            Glide.with(activity)
                .load(activity.resources?.getDrawable(R.drawable.ic_add))
                .into(holder.binding.imageView)
            holder.binding.imageView.setOnClickListener {
                activity.startActivity(Intent(activity, StoryAddActivity::class.java))
            }
        } else {
            Glide.with(activity)
                .load(list.get(position).url)
                .timeout(6500)
                .into(holder.binding.imageView)

            holder.binding.imageView.setOnClickListener {
                if (holder.adapterPosition == 0) {
                    Dexter.withContext(activity)
                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                                p0?.let {
                                    if (it.areAllPermissionsGranted()) {
                                        activity.startActivity(Intent(activity, StoryAddActivity::class.java))
                                    } else {
                                        Toast.makeText(activity, activity.resources?.getString(R.string.allow_per), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                p0: MutableList<PermissionRequest>?,
                                p1: PermissionToken?
                            ) {
                                p1?.continuePermissionRequest()
                            }
                        }).check()
                } else {
                    val intent = Intent(activity, ViewStoryActivity::class.java)
                    intent.putExtra(ViewStoryActivity.VIDEO_URL_KEY, list.get(position).url)
                    intent.putExtra(ViewStoryActivity.FILE_TYPE, list.get(position).type)
                    activity.startActivity(intent)
                }
            }
        }
    }
}