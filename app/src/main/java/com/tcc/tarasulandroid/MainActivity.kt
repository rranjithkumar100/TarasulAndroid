package com.tcc.tarasulandroid

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.tcc.tarasulandroid.core.designsystem.components.PreviewGallery
import com.tcc.tarasulandroid.core.designsystem.theme.TarasulTheme
import com.tcc.tarasulandroid.ui.base.BaseActivity
import com.tcc.tarasulandroid.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TarasulTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PreviewGallery(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
