package com.kv.connectify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kv.connectify.databinding.CommentItemsBinding
import com.kv.connectify.model.CommentModel

class CommentAdapter(val context: Context, val list: MutableList<CommentModel>): RecyclerView.Adapter<CommentAdapter.CommentHolder>() {

    inner class CommentHolder(val binding: CommentItemsBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val binding = CommentItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        with(holder) {
            Glide.with(context)
                .load(list.get(position).profileImageUrl)
                .into(this.binding.profileImage)
            this.binding.nameTV.text = list.get(position).name
            this.binding.commentTV.text = list.get(position).comment
        }
    }
}