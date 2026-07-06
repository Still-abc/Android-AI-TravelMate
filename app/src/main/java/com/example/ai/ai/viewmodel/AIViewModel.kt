package com.example.ai.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ai.model.BudgetAnalysis
import com.example.ai.ai.model.ChatMessage
import com.example.ai.ai.model.Conversation
import com.example.ai.ai.model.GuideSummary
import com.example.ai.ai.model.TravelPlan
import com.example.ai.ai.model.WeatherAdvice
import com.example.ai.ai.repository.AIRepository
import com.example.ai.ai.repository.GeneratedPlanRepository
import com.example.ai.common.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AIViewModel @Inject constructor(
    private val repository: AIRepository,
    private val generatedPlanRepository: GeneratedPlanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AIUiState(hasValidConfig = repository.hasValidConfig()))
    val uiState: StateFlow<AIUiState> = _uiState.asStateFlow()

    init {
        newConversation()
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repository.testConnection()) {
                is AppResult.Success -> _uiState.update { it.copy(loading = false, statusMessage = result.data, hasValidConfig = true) }
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message, hasValidConfig = false) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        val userMessage = ChatMessage("user", trimmed)
        _uiState.update { state ->
            val conversation = state.currentConversation ?: createConversation()
            state.copy(
                currentConversation = conversation.copy(
                    messages = conversation.messages + userMessage,
                    updatedAt = System.currentTimeMillis()
                ),
                loading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val messages = _uiState.value.currentConversation?.messages.orEmpty()
            when (val result = repository.sendMessage(messages)) {
                is AppResult.Success -> appendAssistantMessage(result.data)
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun regenerateLastResponse() {
        val messages = _uiState.value.currentConversation?.messages.orEmpty()
        val withoutLastAssistant = messages.dropLastWhile { it.role == "assistant" }
        _uiState.update { state ->
            val conversation = state.currentConversation ?: return@update state
            state.copy(currentConversation = conversation.copy(messages = withoutLastAssistant), loading = true, errorMessage = null)
        }
        viewModelScope.launch {
            when (val result = repository.sendMessage(withoutLastAssistant)) {
                is AppResult.Success -> appendAssistantMessage(result.data)
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun deleteChatMessage(index: Int) {
        _uiState.update { state ->
            val conversation = state.currentConversation ?: return@update state
            state.copy(currentConversation = conversation.copy(messages = conversation.messages.filterIndexed { i, _ -> i != index }))
        }
    }

    fun clearConversation() {
        _uiState.update { state ->
            val conversation = state.currentConversation ?: createConversation()
            state.copy(currentConversation = conversation.copy(messages = emptyList(), updatedAt = System.currentTimeMillis()), errorMessage = null)
        }
    }

    fun newConversation() {
        _uiState.update { it.copy(currentConversation = createConversation(), errorMessage = null) }
    }

    fun generateTravelPlan(
        departure: String,
        destination: String,
        startDate: String,
        endDate: String,
        budget: String,
        people: Int,
        interests: List<String>
    ) {
        _uiState.update { it.copy(loading = true, errorMessage = null, travelPlan = null) }
        viewModelScope.launch {
            when (val result = repository.generateTravelPlan(departure, destination, startDate, endDate, budget, people, interests)) {
                is AppResult.Success -> {
                    generatedPlanRepository.updatePlan(result.data)
                    _uiState.update { it.copy(loading = false, travelPlan = result.data) }
                }
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun generateWeatherAdvice(weather: String, temperature: String, humidity: String, wind: String, airQuality: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repository.generateWeatherAdvice(weather, temperature, humidity, wind, airQuality)) {
                is AppResult.Success -> _uiState.update { it.copy(loading = false, weatherAdvice = result.data) }
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun analyzeBudget(budget: String, city: String, people: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repository.analyzeBudget(budget, city, people)) {
                is AppResult.Success -> _uiState.update { it.copy(loading = false, budgetAnalysis = result.data) }
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun summarizeGuide(guide: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repository.summarizeGuide(guide)) {
                is AppResult.Success -> _uiState.update { it.copy(loading = false, guideSummary = result.data) }
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private fun appendAssistantMessage(content: String) {
        _uiState.update { state ->
            val conversation = state.currentConversation ?: createConversation()
            state.copy(
                currentConversation = conversation.copy(
                    messages = conversation.messages + ChatMessage("assistant", content),
                    updatedAt = System.currentTimeMillis()
                ),
                loading = false,
                errorMessage = null
            )
        }
    }

    private fun createConversation(): Conversation = Conversation(id = UUID.randomUUID().toString())
}

data class AIUiState(
    val hasValidConfig: Boolean = false,
    val currentConversation: Conversation? = null,
    val loading: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val travelPlan: TravelPlan? = null,
    val weatherAdvice: WeatherAdvice? = null,
    val budgetAnalysis: BudgetAnalysis? = null,
    val guideSummary: GuideSummary? = null
)