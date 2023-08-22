package com.kv.connectify.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatUserModel(val id: String, val lastMessage: String, val uid: MutableList<String>, @ServerTimestamp val time: Date) {
    constructor(): this("", "", mutableListOf(), Date())
}
