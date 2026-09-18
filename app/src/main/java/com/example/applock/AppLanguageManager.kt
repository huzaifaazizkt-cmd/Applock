package com.example.applock

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLanguageManager {

    fun setLanguage(languageCode: String) {

        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(
                languageCode
            )
        )
    }
}