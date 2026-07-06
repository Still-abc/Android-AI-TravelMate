package com.example.ai.ai.network

import com.example.ai.config.ApiConfig
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Singleton
class AIClientFactory @Inject constructor() {
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    fun create(): OpenAIChatApi {
        val safeBaseUrl = ApiConfig.AI_BASE_URL.trim().trimEnd('/') + "/"
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .addInterceptor(NetworkInterceptor())
            .build()

        return RetrofitFactory.create(safeBaseUrl, client, json).create(OpenAIChatApi::class.java)
    }
}

object RetrofitFactory {
    fun create(baseUrl: String, okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}

class NetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request().newBuilder()
            .header("Authorization", "Bearer ${ApiConfig.AI_API_KEY}")
            .header("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}

class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause)

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : ApiResult<Nothing>
    data object Loading : ApiResult<Nothing>
}

object LoggingInterceptor {
    fun disabled(): Interceptor = Interceptor { chain -> chain.proceed(chain.request()) }
}

object AIErrorMapper {
    fun toChineseMessage(throwable: Throwable): String = when (throwable) {
        is HttpException -> when (throwable.code()) {
            401 -> "API Key错误或未授权"
            403 -> "API Key无权限访问当前模型"
            404 -> "Base URL错误或Model不存在"
            400 -> "AI请求参数错误，请检查模型名称是否支持"
            408 -> "请求超时，请稍后重试"
            429 -> "请求过于频繁，已触发限流"
            502, 503 -> "AI上游服务暂不可用，请稍后重试或更换模型"
            in 500..599 -> "AI服务器错误，请稍后重试"
            else -> "AI请求失败：HTTP ${throwable.code()}"
        }
        is SocketTimeoutException -> "请求超时，请检查网络或稍后重试"
        is UnknownHostException -> "网络错误或Base URL无法访问"
        is SSLException -> "SSL证书校验失败，请检查Base URL"
        is SerializationException -> "AI返回内容格式不符合要求，请稍后重试或更换模型"
        is IllegalArgumentException -> "AI返回内容无法解析，请稍后重试或更换模型"
        is ApiException -> throwable.message ?: "AI配置错误"
        else -> throwable.message ?: "AI请求失败，请稍后重试"
    }
}
