package com.example.ai.network

import com.example.ai.config.ApiConfig

object NetworkConfig {
    const val BASE_URL = ApiConfig.DEFAULT_BASE_URL
}

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val exception: ApiException) : ApiResult<Nothing>
}

data class ApiResponse<T>(val data: T? = null, val message: String? = null, val code: Int = 0)

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

interface BaseApi
interface AIApi : BaseApi
interface WeatherApi : BaseApi
interface HotelApi : BaseApi
interface ScenicApi : BaseApi
