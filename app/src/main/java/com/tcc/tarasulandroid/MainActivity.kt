package com.tcc.tarasulandroid

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tcc.tarasulandroid.core.designsystem.theme.TarasulTheme
import com.tcc.tarasulandroid.ui.base.BaseActivity
import com.tcc.tarasulandroid.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            TarasulTheme(darkTheme = isDarkTheme) {
                NavGraph(modifier = Modifier.fillMaxSize())
            }
        }
    }
}