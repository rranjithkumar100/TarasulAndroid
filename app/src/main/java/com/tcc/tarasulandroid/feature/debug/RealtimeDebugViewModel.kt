
package com.tcc.tarasulandroid.feature.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.tarasulandroid.core.realtime.RealtimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RealtimeDebugViewModel @Inject constructor(
    private val realtimeRepository: RealtimeRepository
) : ViewModel() {

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _lastPong = MutableStateFlow("")
    val lastPong: StateFlow<String> = _lastPong.asStateFlow()

    private val _lastMessage = MutableStateFlow("")
    val lastMessage: StateFlow<String> = _lastMessage.asStateFlow()

    init {
        viewModelScope.launch {
            realtimeRepository.onPong().collect {
                _lastPong.value = it.toString()
            }
        }
        viewModelScope.launch {
            realtimeRepository.onNewMessage().collect {
                _lastMessage.value = it.toString()
            }
        }
    }

    fun connect() {
        realtimeRepository.connect()
        _connectionStatus.value = if (realtimeRepository.isConnected()) "Connected" else "Connecting..."
    }

    fun disconnect() {
        realtimeRepository.disconnect()
        _connectionStatus.value = "Disconnected"
    }

    fun sendPing() {
        realtimeRepository.sendPing()
    }
}
