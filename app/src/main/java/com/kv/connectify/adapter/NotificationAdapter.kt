package com.kv.connectify.adapter

import android.content.Context
import android.os.Build
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.kv.connectify.databinding.NotificationItemsBinding
import com.kv.connectify.model.NotificationModel
import java.util.Date

class NotificationAdapter(val context: Context, val list: MutableList<NotificationModel>) : RecyclerView.Adapter<NotificationAdapter.NotificationHolder>() {


    inner class NotificationHolder(val binding: NotificationItemsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {
        val binding = NotificationItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
        with(holder) {
            this.binding.notification.text = list[position].notification
            this.binding.timeTv.text = calculateTime(list[position].time)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateTime(date: Date): String {
        val millis = date.toInstant().toEpochMilli()
        return DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), 60000, DateUtils.FORMAT_ABBREV_TIME).toString()
    }
}