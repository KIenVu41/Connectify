package com.kv.connectify.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ChatModel(val id: String, val message: String, val senderID: String, @ServerTimestamp val date: Date) {
    constructor(): this("", "", "", Date())
}

