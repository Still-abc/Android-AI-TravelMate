package com.example.ai.thirdparty.network

import com.example.ai.config.ApiConfig
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Singleton
class ThirdPartyClientFactory @Inject constructor() {
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun weatherApi(): WeatherApi = create(ApiConfig.WEATHER_BASE_URL).create(WeatherApi::class.java)
    fun mapApi(): MapApi = create(ApiConfig.AMAP_BASE_URL).create(MapApi::class.java)
    fun imageApi(): ImageApi = create(ApiConfig.PEXELS_BASE_URL).create(ImageApi::class.java)

    private fun create(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl.trim().trimEnd('/') + "/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}

class ThirdPartyApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

object ThirdPartyErrorMapper {
    fun toChineseMessage(throwable: Throwable): String = when (throwable) {
        is HttpException -> when (throwable.code()) {
            401 -> "API Key错误或未授权"
            403 -> "API Key无权限访问当前服务"
            404 -> "接口地址或资源不存在"
            408 -> "请求超时，请稍后重试"
            429 -> "请求过于频繁，已触发限流"
            in 500..599 -> "第三方服务器错误，请稍后重试"
            else -> "第三方API请求失败：HTTP ${throwable.code()}"
        }
        is SocketTimeoutException -> "请求超时，请检查网络"
        is UnknownHostException -> "网络不可用或Base URL无法访问"
        is SSLException -> "SSL证书校验失败，请检查Base URL"
        is SerializationException -> "第三方返回格式不兼容，请稍后重试"
        is IllegalArgumentException -> "第三方接口参数或返回格式异常"
        is ThirdPartyApiException -> throwable.message ?: "第三方API配置错误"
        else -> throwable.message ?: "第三方API请求失败"
    }
}
