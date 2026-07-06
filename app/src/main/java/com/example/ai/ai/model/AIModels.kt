package com.example.ai.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String = ""
)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    @SerialName("max_tokens") val maxTokens: Int = 4096,
    @SerialName("response_format") val responseFormat: ResponseFormat? = null,
    val stream: Boolean = false
)

@Serializable
data class ResponseFormat(
    val type: String
)

@Serializable
data class ChatResponse(
    val id: String? = null,
    val choices: List<Choice> = emptyList(),
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int = 0,
    val message: ChatMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int = 0,
    @SerialName("completion_tokens") val completionTokens: Int = 0,
    @SerialName("total_tokens") val totalTokens: Int = 0
)

@Serializable
data class Conversation(
    val id: String,
    val title: String = "New travel conversation",
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class TravelPlan(
    val title: String,
    val departure: String,
    val destination: String,
    val days: List<TravelDay>,
    val budget: Budget,
    val hotels: List<Hotel> = emptyList(),
    val foods: List<Food> = emptyList(),
    val tips: List<Tips> = emptyList()
)

@Serializable
data class TravelDay(
    val day: Int,
    val date: String,
    val theme: String,
    val schedules: List<Schedule>
)

@Serializable
data class Schedule(
    val time: String,
    val title: String,
    val type: String,
    val location: String,
    val description: String,
    val cost: Double = 0.0,
    val durationMinutes: Int = 0
)

typealias TravelSchedule = Schedule

@Serializable
data class Budget(
    val total: Double,
    val currency: String = "CNY",
    val traffic: Double = 0.0,
    val hotel: Double = 0.0,
    val food: Double = 0.0,
    val ticket: Double = 0.0,
    val other: Double = 0.0
)

@Serializable
data class Hotel(
    val name: String,
    val location: String,
    val pricePerNight: Double,
    val reason: String
)

@Serializable
data class Food(
    val name: String,
    val location: String,
    val averageCost: Double,
    val reason: String
)

@Serializable
data class Tips(
    val title: String,
    val content: String
)

@Serializable
data class WeatherAdvice(
    val outfit: String,
    val umbrella: String,
    val sunscreen: String,
    val outdoor: String
)

@Serializable
data class BudgetAnalysis(
    val reasonable: Boolean,
    val summary: String,
    val hotelAdvice: String,
    val trafficAdvice: String,
    val foodAdvice: String,
    val savingAdvice: String
)

@Serializable
data class GuideSummary(
    val scenicSpots: List<String>,
    val routes: List<String>,
    val foods: List<String>,
    val warnings: List<String>
)
