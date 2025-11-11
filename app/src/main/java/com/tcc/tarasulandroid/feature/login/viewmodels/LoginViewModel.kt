package com.tcc.tarasulandroid.feature.login.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcc.tarasulandroid.data.SecurePreferencesManager
import com.tcc.tarasulandroid.data.api.LoginApi
import com.tcc.tarasulandroid.data.api.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginApi: LoginApi,
    private val securePrefsManager: SecurePreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginResult = MutableSharedFlow<Boolean>()
    val loginResult = _loginResult.asSharedFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            try {
                val response = loginApi.login(LoginRequest(email, password))

                if (response.success) {
                    // Save login state securely
                    securePrefsManager.setLoggedIn(
                        isLoggedIn = true,
                        email = email,
                        token = response.token
                    )

                    _uiState.value = LoginUiState(isSuccess = true)
                    _loginResult.emit(true)
                } else {
                    _uiState.value = LoginUiState(
                        errorMessage = response.message ?: "Login failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState(
                    errorMessage = "Network error. Please try again."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
