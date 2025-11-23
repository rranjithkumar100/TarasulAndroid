package com.tcc.tarasulandroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.tarasulandroid.data.SecurePreferencesManager
import com.tcc.tarasulandroid.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val securePreferencesManager: SecurePreferencesManager
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean?> = settingsRepository.isDarkTheme
        .map { it as Boolean? }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )
    
    private val _logoutEvent = MutableStateFlow(false)
    val logoutEvent: StateFlow<Boolean> = _logoutEvent

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(isDark)
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            securePreferencesManager.logout()
            _logoutEvent.value = true
        }
    }
    
    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }
}