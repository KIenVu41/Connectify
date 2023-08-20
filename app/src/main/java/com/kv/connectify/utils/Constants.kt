package com.kv.connectify.utils

import java.util.regex.Pattern

object Constants {
    val VALID_EMAIL_ADDRESS_REGEX: Pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
    val COLLECTION_NAME = "Users"
    val POST_IMAGES = "Post Images"
    val PROFILE_IMAGES = "Profile Images"
    val STORIES = "Stories"
    val NOTIFICATIONS = "Notifications"
    val DEFAULT_AVT = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRwp--EwtYaxkfsSPIpoSPucdbxAo6PancQX1gw6ETSKI6_pGNCZY4ts1N6BV5ZcN3wPbA&usqp=CAU"
    val PREF_STORED = "connectify_store"
    val PREF_URL = "connectify_url"
    val PREF_DIRECTORY = "connectify_direct"
}