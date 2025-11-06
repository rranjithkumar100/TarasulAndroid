package com.tcc.tarasulandroid.core.network.interceptors

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnauthorizedInterceptor @Inject constructor(
    // In the future, you can inject a session manager here
    // private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            // TODO: Emit an event to the session manager to handle token refresh or logout.
            Log.e("UnauthorizedInterceptor", "401 Unauthorized response")
        }
        return response
    }
}
