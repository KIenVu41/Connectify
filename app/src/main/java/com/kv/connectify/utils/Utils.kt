package com.kv.connectify.utils

import android.app.Activity
import java.util.regex.Matcher

object Utils {

    fun validateEmail(emailStr: String): Boolean {
        val matcher: Matcher = Constants.VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.matches();
    }
}