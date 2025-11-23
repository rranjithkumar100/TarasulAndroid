package com.tcc.tarasulandroid.feature.login.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor() : ViewModel() {

    private val _otpResult = MutableSharedFlow<Boolean>()
    val otpResult = _otpResult.asSharedFlow()

    // Configurable OTP for testing (default is 1234)
    companion object {
        const val TEST_OTP = "1234"
    }

    fun verifyOtp(enteredOtp: String) {
        viewModelScope.launch {
            // Simulate network delay
            kotlinx.coroutines.delay(500)

            // Verify OTP - for testing, hardcoded to 1234
            // In production, you would send this to your backend
            val isValid = enteredOtp == TEST_OTP
            _otpResult.emit(isValid)
        }
    }
}

