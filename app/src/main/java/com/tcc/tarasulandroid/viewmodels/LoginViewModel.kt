package com.tcc.tarasulandroid.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class LoginViewModel @Inject constructor(
    @Named("dummy_string") private val dummyString: String
) : ViewModel() {

    init {
        Log.d("LoginViewModel", "Injected dummy string: $dummyString")
    }
}
