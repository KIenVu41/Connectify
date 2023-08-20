package com.kv.connectify.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class NotificationModel(val id: String, val notification: String, @ServerTimestamp val time: Date)
