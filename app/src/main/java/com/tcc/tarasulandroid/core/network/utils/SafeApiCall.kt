package com.tcc.tarasulandroid.core.network.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> Result.Error(ApiError(message = "Network Error"))
                is HttpException -> {
                    val code = throwable.code()
                    val errorBody = throwable.response()?.errorBody()?.string()
                    Result.Error(ApiError(statusCode = code, message = errorBody))
                }
                else -> {
                    Result.Error(ApiError(message = throwable.message))
                }
            }
        }
    }
}
