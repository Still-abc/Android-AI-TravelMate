package com.example.ai.ai.repository

import com.example.ai.ai.model.TravelPlan
import com.example.ai.model.History
import com.example.ai.repository.HistoryRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@Singleton
class GeneratedPlanRepository @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val latestPlanHolder = kotlinx.coroutines.flow.MutableStateFlow<TravelPlan?>(null)
    val latestPlan: StateFlow<TravelPlan?> = latestPlanHolder.asStateFlow()

    fun observeHistory(): Flow<List<GeneratedPlanHistory>> = historyRepository.observePlanHistory()
        .map { items -> items.map { it.toGeneratedPlanHistory() } }

    suspend fun updatePlan(plan: TravelPlan) {
        latestPlanHolder.value = plan
        historyRepository.addPlanHistory(
            targetId = plan.destination,
            title = plan.title,
            description = "${plan.departure} -> ${plan.destination}",
            days = plan.days.size,
            scheduleCount = plan.days.sumOf { it.schedules.size },
            hotelCount = plan.hotels.size,
            foodCount = plan.foods.size,
            planJson = json.encodeToString(plan)
        )
    }

    suspend fun openHistoryPlan(id: String): Boolean {
        val history = historyRepository.getPlanHistoryById(id) ?: return false
        val plan = runCatching { json.decodeFromString<TravelPlan>(history.planJson) }.getOrNull() ?: return false
        latestPlanHolder.value = plan
        return true
    }
}

data class GeneratedPlanHistory(
    val id: String,
    val title: String,
    val destination: String,
    val days: Int,
    val scheduleCount: Int,
    val hotelCount: Int,
    val foodCount: Int,
    val hasFullPlan: Boolean,
    val generatedAt: Long
)

private fun History.toGeneratedPlanHistory(): GeneratedPlanHistory = GeneratedPlanHistory(
    id = id,
    title = title,
    destination = targetId,
    days = days,
    scheduleCount = scheduleCount,
    hotelCount = hotelCount,
    foodCount = foodCount,
    hasFullPlan = planJson.isNotBlank(),
    generatedAt = visitedAt
)
