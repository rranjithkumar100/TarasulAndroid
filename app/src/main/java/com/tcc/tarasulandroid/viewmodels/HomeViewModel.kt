package com.tcc.tarasulandroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.tarasulandroid.data.RealtimeRepository
import com.tcc.tarasulandroid.data.SettingsRepository
import com.tcc.tarasulandroid.feature.home.model.Message
import com.tcc.tarasulandroid.feature.home.ui.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    realtimeRepository: RealtimeRepository
) : ViewModel() {

    private val mockMessages = listOf(
        Message("1", "Alice", "Me", "Hello!", System.currentTimeMillis()),
        Message("2", "Me", "Alice", "Hi! How are you?", System.currentTimeMillis() + 1000)
    )

    val uiState: StateFlow<HomeUiState> = combine(
        settingsRepository.isDarkTheme,
        realtimeRepository.lastEvent
    ) { isDarkTheme, lastEvent ->
        HomeUiState(
            messages = mockMessages,
            isDarkTheme = isDarkTheme,
            lastEvent = lastEvent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun setDarkTheme(isDarkTheme: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(isDarkTheme)
        }
    }
}