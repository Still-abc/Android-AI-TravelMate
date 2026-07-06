package com.example.ai.ai.network

import com.example.ai.ai.model.ChatRequest
import com.example.ai.ai.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAIChatApi {
    @POST("chat/completions")
    suspend fun chatCompletions(@Body request: ChatRequest): ChatResponse
}
