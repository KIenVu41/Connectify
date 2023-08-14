package com.kv.connectify.utils

import java.util.regex.Pattern

object Constants {
    val VALID_EMAIL_ADDRESS_REGEX: Pattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
    val COLLECTION_NAME = "Users"
    val POST_IMAGES = "Post Images"
}