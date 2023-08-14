package com.kv.connectify.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kv.connectify.databinding.ImageItemsBinding
import com.kv.connectify.model.GalleryImages

class GalleryAdapter(val list: List<GalleryImages>): RecyclerView.Adapter<GalleryAdapter.GalleryHolder>() {

    private lateinit var onSendImages: SendImage

    interface SendImage {
        fun onSend(picUri: Uri)
    }

    fun SendImage(sendImage: SendImage) {
        this.onSendImages = sendImage
    }

    private fun chooseImage(picUri: Uri) {
        onSendImages.onSend(picUri)
    }

    inner class GalleryHolder(val binding: ImageItemsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryHolder {
        val binding = ImageItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: GalleryHolder, position: Int) {
        Glide.with(holder.binding.imageView.context.applicationContext)
            .load(list.get(position).picUri)
            .into(holder.binding.imageView)
        holder.binding.imageView.setOnClickListener {
            chooseImage(list[holder.adapterPosition].picUri)
        }
    }
}