package com.example.ai.ai.network

object ApiClient {
    fun create(): OpenAIChatApi = AIClientFactory().create()
}

typealias AIApi = OpenAIChatApi
