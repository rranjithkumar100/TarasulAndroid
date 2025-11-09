package com.tcc.tarasulandroid.feature.login.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.tarasulandroid.core.network.api.AuthApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authApi: AuthApi
) : ViewModel() {

    private val _loginResult = MutableSharedFlow<Unit>()
    val loginResult = _loginResult.asSharedFlow()

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            authApi.login(mapOf("email" to email, "password" to pass))
            _loginResult.emit(Unit)
        }
    }
}
