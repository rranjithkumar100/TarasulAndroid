package com.tcc.tarasulandroid.core.network.utils

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val error: ApiError) : Result<Nothing>()
}

data class ApiError(
    val statusCode: Int? = null,
    val message: String? = null
)
