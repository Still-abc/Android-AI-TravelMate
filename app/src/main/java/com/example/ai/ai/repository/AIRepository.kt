package com.example.ai.ai.repository

import com.example.ai.ai.model.BudgetAnalysis
import com.example.ai.ai.model.ChatMessage
import com.example.ai.ai.model.ChatRequest
import com.example.ai.ai.model.GuideSummary
import com.example.ai.ai.model.ResponseFormat
import com.example.ai.ai.model.TravelPlan
import com.example.ai.ai.model.WeatherAdvice
import com.example.ai.ai.network.AIClientFactory
import com.example.ai.ai.network.AIErrorMapper
import com.example.ai.ai.network.ApiException
import com.example.ai.ai.prompt.PromptManager
import com.example.ai.common.AppResult
import com.example.ai.config.ApiConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.Json

@Singleton
class AIRepository @Inject constructor(
    private val clientFactory: AIClientFactory
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun hasValidConfig(): Boolean = ApiConfig.hasValidAIConfig()

    suspend fun testConnection(): AppResult<String> {
        if (!hasValidConfig()) return AppResult.Error("请先配置AI接口")
        return sendRaw(listOf(PromptManager.chatPrompt(), ChatMessage("user", "Hello")))
            .mapSuccess { "AI连接成功" }
    }

    suspend fun sendMessage(messages: List<ChatMessage>): AppResult<String> {
        if (!hasValidConfig()) return AppResult.Error("请先配置AI接口")
        return sendRaw(listOf(PromptManager.chatPrompt()) + messages)
    }

    suspend fun generateTravelPlan(
        departure: String,
        destination: String,
        startDate: String,
        endDate: String,
        budget: String,
        people: Int,
        interests: List<String>
    ): AppResult<TravelPlan> = sendRaw(
        PromptManager.travelPlanPrompt(departure, destination, "$startDate - $endDate", budget, people, interests),
        jsonMode = true,
        maxTokens = TRAVEL_PLAN_MAX_TOKENS
    ).mapSuccess { content -> decodeTravelPlan(content) }

    suspend fun generateWeatherAdvice(
        weather: String,
        temperature: String,
        humidity: String,
        wind: String,
        airQuality: String
    ): AppResult<WeatherAdvice> = sendJson(PromptManager.weatherPrompt(weather, temperature, humidity, wind, airQuality))

    suspend fun analyzeBudget(budget: String, city: String, people: Int): AppResult<BudgetAnalysis> =
        sendJson(PromptManager.budgetPrompt(budget, city, people))

    suspend fun summarizeGuide(guide: String): AppResult<GuideSummary> =
        sendJson(PromptManager.guideSummaryPrompt(guide))

    private suspend inline fun <reified T> sendJson(messages: List<ChatMessage>): AppResult<T> {
        if (!hasValidConfig()) return AppResult.Error("请先配置AI接口")
        return sendRaw(messages, jsonMode = true).mapSuccess { content -> json.decodeFromString<T>(content.cleanJson()) }
    }

    private suspend fun sendRaw(messages: List<ChatMessage>, jsonMode: Boolean = false, maxTokens: Int = ApiConfig.AI_MAX_TOKENS): AppResult<String> = runCatching {
        if (!ApiConfig.hasValidAIConfig()) throw ApiException("请先配置AI接口")
        val response = clientFactory.create().chatCompletions(
            ChatRequest(
                model = ApiConfig.AI_MODEL,
                messages = messages,
                temperature = ApiConfig.AI_TEMPERATURE,
                maxTokens = maxTokens,
                responseFormat = if (jsonMode) ResponseFormat("json_object") else null,
                stream = false
            )
        )
        response.choices.firstOrNull()?.message?.content?.takeIf { it.isNotBlank() }
            ?: throw ApiException("AI没有返回有效内容")
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(AIErrorMapper.toChineseMessage(it), it) }
    )

    private inline fun <T, R> AppResult<T>.mapSuccess(transform: (T) -> R): AppResult<R> = when (this) {
        is AppResult.Success -> runCatching { AppResult.Success(transform(data)) }
            .getOrElse { AppResult.Error(AIErrorMapper.toChineseMessage(it), it) }
        is AppResult.Error -> AppResult.Error(message, cause)
        AppResult.Empty -> AppResult.Empty
        AppResult.Loading -> AppResult.Loading
    }

    private fun decodeTravelPlan(content: String): TravelPlan {
        val cleaned = content.cleanJson()
        return runCatching { json.decodeFromString<TravelPlan>(cleaned) }
            .getOrElse { json.decodeFromString(normalizeTravelPlanJson(cleaned)) }
    }

    private fun normalizeTravelPlanJson(content: String): String {
        val root = json.parseToJsonElement(content) as? JsonObject
            ?: throw SerializationException("AI返回的旅行方案不是JSON对象")
        val normalized = root.toMutableMap()
        normalized["title"] = root.stringValue("title", fallback = "AI旅行方案")
        normalized["departure"] = root.stringValue("departure", fallback = "当前位置")
        normalized["destination"] = root.stringValue("destination", fallback = "目的地")
        normalized["days"] = normalizeDays(root["days"])
        normalized["budget"] = normalizeBudget(root["budget"])
        normalized["hotels"] = normalizeHotels(root["hotels"])
        normalized["foods"] = normalizeFoods(root["foods"])
        normalized["tips"] = normalizeTips(root["tips"])
        return JsonObject(normalized).toString()
    }

    private fun normalizeDays(element: JsonElement?): JsonArray = when (element) {
        is JsonArray -> JsonArray(element.mapIndexedNotNull { index, item ->
            val day = item as? JsonObject ?: return@mapIndexedNotNull null
            JsonObject(
                mapOf(
                    "day" to day.integerValue("day", fallback = index + 1),
                    "date" to day.stringValue("date", fallback = "第${index + 1}天"),
                    "theme" to day.stringValue("theme", "title", "summary", fallback = "第${index + 1}天行程"),
                    "schedules" to normalizeSchedules(day["schedules"])
                )
            )
        })
        else -> JsonArray(emptyList())
    }

    private fun normalizeSchedules(element: JsonElement?): JsonArray = normalizeObjectArray(element) { item ->
        JsonObject(
            mapOf(
                "time" to item.stringValue("time", "startTime", fallback = "待定"),
                "title" to item.stringValue("title", "name", fallback = "行程安排"),
                "type" to item.stringValue("type", "category", fallback = "activity"),
                "location" to item.stringValue("location", "address", fallback = ""),
                "description" to item.stringValue("description", "reason", "detail", fallback = ""),
                "cost" to item.numberValue("cost", "price", "fee"),
                "durationMinutes" to item.integerValue("durationMinutes", "duration", "minutes")
            )
        )
    }

    private fun normalizeBudget(element: JsonElement?): JsonObject {
        val item = element as? JsonObject ?: JsonObject(emptyMap())
        return JsonObject(
            mapOf(
                "total" to item.numberValue("total", "amount"),
                "currency" to item.stringValue("currency", fallback = "CNY"),
                "traffic" to item.numberValue("traffic", "transport", "transportation"),
                "hotel" to item.numberValue("hotel", "lodging", "accommodation"),
                "food" to item.numberValue("food", "dining", "restaurant"),
                "ticket" to item.numberValue("ticket", "tickets", "attraction"),
                "other" to item.numberValue("other", "misc")
            )
        )
    }

    private fun normalizeHotels(element: JsonElement?): JsonArray = normalizeObjectArray(element) { item ->
        JsonObject(
            mapOf(
                "name" to item.stringValue("name"),
                "location" to item.stringValue("location"),
                "pricePerNight" to item.numberValue("pricePerNight", "price", "averageCost"),
                "reason" to item.stringValue("reason", "description")
            )
        )
    }

    private fun normalizeFoods(element: JsonElement?): JsonArray = normalizeObjectArray(element) { item ->
        JsonObject(
            mapOf(
                "name" to item.stringValue("name"),
                "location" to item.stringValue("location"),
                "averageCost" to item.numberValue("averageCost", "price", "cost"),
                "reason" to item.stringValue("reason", "description")
            )
        )
    }

    private fun normalizeTips(element: JsonElement?): JsonArray = when (element) {
        is JsonArray -> JsonArray(element.mapIndexed { index, item ->
            when (item) {
                is JsonObject -> JsonObject(
                    mapOf(
                        "title" to item.stringValue("title", fallback = "提示${index + 1}"),
                        "content" to item.stringValue("content", "description", fallback = item.toPlainText())
                    )
                )
                else -> JsonObject(mapOf("title" to JsonPrimitive("提示${index + 1}"), "content" to JsonPrimitive(item.toPlainText())))
            }
        })
        else -> JsonArray(emptyList())
    }

    private fun normalizeObjectArray(element: JsonElement?, transform: (JsonObject) -> JsonObject): JsonArray = when (element) {
        is JsonArray -> JsonArray(element.mapNotNull { (it as? JsonObject)?.let(transform) })
        else -> JsonArray(emptyList())
    }

    private fun JsonObject.stringValue(vararg keys: String, fallback: String = ""): JsonPrimitive =
        JsonPrimitive(keys.firstNotNullOfOrNull { key -> this[key]?.toPlainText()?.takeIf { it.isNotBlank() } } ?: fallback)

    private fun JsonObject.numberValue(vararg keys: String, fallback: Double = 0.0): JsonPrimitive =
        JsonPrimitive(keys.firstNotNullOfOrNull { key -> this[key]?.numericValue() } ?: fallback)

    private fun JsonObject.integerValue(vararg keys: String, fallback: Int = 0): JsonPrimitive =
        JsonPrimitive(keys.firstNotNullOfOrNull { key -> this[key]?.numericValue()?.toInt() } ?: fallback)

    private fun JsonElement.numericValue(): Double? =
        (this as? JsonPrimitive)?.content?.toDoubleOrNull()
            ?: Regex("-?\\d+(\\.\\d+)?").find(toPlainText())?.value?.toDoubleOrNull()

    private fun JsonElement.toPlainText(): String = when (this) {
        is JsonPrimitive -> content
        else -> toString()
    }

    private fun String.cleanJson(): String = trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()

    private companion object {
        const val TRAVEL_PLAN_MAX_TOKENS = 2400
    }
}
