package com.kv.connectify.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class HomeModel(val name: String, val profileImage: String,
val imageUrl: String, val uid: String, val description: String, val id: String,
@ServerTimestamp val timestamp: Date, val likes: List<String>)
