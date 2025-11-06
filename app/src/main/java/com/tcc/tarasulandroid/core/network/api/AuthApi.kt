package com.tcc.tarasulandroid.core.network.api

import com.tcc.tarasulandroid.core.network.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: Map<String, String>): LoginResponse
}
