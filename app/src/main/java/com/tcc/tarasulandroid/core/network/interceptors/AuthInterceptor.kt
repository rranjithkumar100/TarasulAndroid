package com.tcc.tarasulandroid.core.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {

    // TODO: Replace with a real token provider
    private fun getToken(): String = "stub_token"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${getToken()}")
            .build()
        return chain.proceed(request)
    }
}
