package com.tcc.tarasulandroid.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _isLoggedIn = MutableStateFlow(getIsLoggedIn())
    val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentLanguage = MutableStateFlow(getLanguage())
    val currentLanguage: Flow<String> = _currentLanguage.asStateFlow()

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_LANGUAGE = "language"
        private const val DEFAULT_LANGUAGE = "en"
    }

    fun setLoggedIn(isLoggedIn: Boolean, email: String? = null, token: String? = null) {
        securePrefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            if (email != null) putString(KEY_USER_EMAIL, email)
            if (token != null) putString(KEY_USER_TOKEN, token)
            apply()
        }
        _isLoggedIn.value = isLoggedIn
    }

    fun getIsLoggedIn(): Boolean {
        return securePrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserEmail(): String? {
        return securePrefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserToken(): String? {
        return securePrefs.getString(KEY_USER_TOKEN, null)
    }

    fun setLanguage(language: String) {
        securePrefs.edit().putString(KEY_LANGUAGE, language).apply()
        _currentLanguage.value = language
    }

    fun getLanguage(): String {
        return securePrefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun logout() {
        securePrefs.edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_TOKEN)
            apply()
        }
        _isLoggedIn.value = false
    }
}
