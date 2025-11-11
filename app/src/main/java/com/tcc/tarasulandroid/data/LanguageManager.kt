package com.tcc.tarasulandroid.data

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePrefsManager: SecurePreferencesManager
) {
    // Also use regular SharedPreferences for language to avoid encryption issues on app start
    private val languagePrefs = context.getSharedPreferences("app_language_prefs", Context.MODE_PRIVATE)
    
    companion object {
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_ARABIC = "ar"
        private const val KEY_LANGUAGE = "language"
    }

    fun getCurrentLanguage(): String {
        // Read from regular prefs first (used in attachBaseContext)
        return languagePrefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }

    fun setLanguage(language: String, activity: Activity? = null) {
        // Save to both secure and regular prefs
        securePrefsManager.setLanguage(language)
        languagePrefs.edit().putString(KEY_LANGUAGE, language).apply()
        
        applyLanguage(language)
        activity?.recreate()
    }

    fun applyLanguage(language: String) {
        val locale = when (language) {
            LANGUAGE_ARABIC -> Locale("ar", "SA") // Arabic (Saudi Arabia)
            else -> Locale("en", "US") // English (US)
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLayoutDirection(locale)
        }

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun isRTL(): Boolean {
        return getCurrentLanguage() == LANGUAGE_ARABIC
    }

    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language(LANGUAGE_ENGLISH, "English", "🇺🇸"),
            Language(LANGUAGE_ARABIC, "العربية", "🇸🇦")
        )
    }
}

data class Language(
    val code: String,
    val name: String,
    val flag: String
)
