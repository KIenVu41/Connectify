package com.kv.connectify.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class PostImageModel(val imageUrl: String, val id: String, val description: String,
val uid: String, @ServerTimestamp val timestamp: Date)
