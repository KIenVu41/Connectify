package com.kv.connectify.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import java.util.Locale

class LocaleContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, languageCode: String): ContextWrapper {
            val config = context.resources.configuration
            val systemLocale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.locales[0]
            } else {
                config.locale
            }

            val newLocale = Locale(languageCode)
            if (systemLocale != newLocale) {
                Locale.setDefault(newLocale)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    config.setLocale(newLocale)
                } else {
                    config.locale = newLocale
                }

                return ContextWrapper(context.createConfigurationContext(config))
            }
            return ContextWrapper(context)
        }
    }
}