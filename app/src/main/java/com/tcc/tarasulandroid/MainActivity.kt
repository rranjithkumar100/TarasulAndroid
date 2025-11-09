package com.tcc.tarasulandroid

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tcc.tarasulandroid.core.designsystem.theme.TarasulTheme
import com.tcc.tarasulandroid.data.LanguageManager
import com.tcc.tarasulandroid.ui.base.BaseActivity
import com.tcc.tarasulandroid.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var languageManager: LanguageManager

    override fun attachBaseContext(newBase: Context) {
        // Apply language before activity is created
        // Use regular SharedPreferences to read language before encryption is available
        val prefs = newBase.getSharedPreferences("app_language_prefs", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "en") ?: "en"
        
        val locale = when (language) {
            "ar" -> java.util.Locale("ar", "SA")
            else -> java.util.Locale("en", "US")
        }
        
        java.util.Locale.setDefault(locale)
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLayoutDirection(locale)
        }
        
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check login state before setting up UI
        val isLoggedIn = checkLoginState()
        
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            TarasulTheme(darkTheme = isDarkTheme) {
                NavGraph(
                    startDestination = if (isLoggedIn) "home" else "login",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    private fun checkLoginState(): Boolean {
        // Read login state from encrypted preferences
        // Using MasterKey early, before Hilt injection
        return try {
            val masterKey = androidx.security.crypto.MasterKey.Builder(this)
                .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            val securePrefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                this,
                "secure_prefs",
                masterKey,
                androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            securePrefs.getBoolean("is_logged_in", false)
        } catch (e: Exception) {
            // If there's any error reading secure prefs, default to login screen
            false
        }
    }
}