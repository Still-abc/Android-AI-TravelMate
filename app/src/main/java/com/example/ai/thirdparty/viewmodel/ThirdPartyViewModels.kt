package com.example.ai.thirdparty.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ai.model.TravelPlan
import com.example.ai.ai.model.WeatherAdvice
import com.example.ai.ai.repository.AIRepository
import com.example.ai.common.AppResult
import com.example.ai.thirdparty.model.Photo
import com.example.ai.thirdparty.model.Poi
import com.example.ai.thirdparty.model.WeatherForecast
import com.example.ai.thirdparty.repository.ImageRepository
import com.example.ai.thirdparty.repository.LocationRepository
import com.example.ai.thirdparty.repository.MapRepository
import com.example.ai.thirdparty.repository.WeatherRepository
import com.example.ai.repository.UserRepository
import com.example.ai.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val aiRepository: AIRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun loadWeather(city: String, date: String = "today") {
        viewModelScope.launch {
            _uiState.update { it.copy(city = city, loading = true, errorMessage = null) }
            when (val result = weatherRepository.getWeather(city, date)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(loading = false, forecast = result.data) }
                    loadAiAdvice(result.data)
                }
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private suspend fun loadAiAdvice(forecast: WeatherForecast) {
        _uiState.update { it.copy(aiLoading = true) }
        when (val result = aiRepository.generateWeatherAdvice(
            weather = forecast.weather,
            temperature = forecast.temperature,
            humidity = forecast.humidity,
            wind = forecast.wind,
            airQuality = forecast.airQuality
        )) {
            is AppResult.Success -> _uiState.update { it.copy(aiLoading = false, advice = result.data) }
            is AppResult.Error -> _uiState.update { it.copy(aiLoading = false, adviceError = result.message) }
            else -> _uiState.update { it.copy(aiLoading = false) }
        }
    }
}

data class WeatherUiState(
    val city: String = "",
    val forecast: WeatherForecast? = null,
    val advice: WeatherAdvice? = null,
    val loading: Boolean = false,
    val aiLoading: Boolean = false,
    val errorMessage: String? = null,
    val adviceError: String? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapRepository: MapRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun loadCity(city: String) {
        viewModelScope.launch {
            loadCityData(city.trim())
        }
    }

    fun loadCurrentCity() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = locationRepository.detectCurrentCity()) {
                is AppResult.Success -> loadCityData(result.data)
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun showLocationUnavailable(message: String) {
        _uiState.update { it.copy(loading = false, errorMessage = message) }
    }

    private suspend fun loadCityData(city: String) {
        if (city.isBlank()) {
            _uiState.update { it.copy(loading = false, errorMessage = "请输入城市") }
            return
        }
        _uiState.update { it.copy(city = city, loading = true, errorMessage = null) }
        val (scenicResult, hotelsResult, foodsResult) = coroutineScope {
            val scenic = async { mapRepository.searchScenic(city) }
            val hotels = async { mapRepository.searchHotels(city) }
            val foods = async { mapRepository.searchFood(city) }
            Triple(scenic.await(), hotels.await(), foods.await())
        }
        _uiState.update { state ->
            state.copy(
                loading = false,
                scenic = scenicResult.successOrEmpty(),
                hotels = hotelsResult.successOrEmpty(),
                foods = foodsResult.successOrEmpty(),
                errorMessage = listOf(scenicResult, hotelsResult, foodsResult)
                    .filterIsInstance<AppResult.Error>()
                    .firstOrNull()?.message
            )
        }
    }
}

data class MapUiState(
    val city: String = "",
    val scenic: List<Poi> = emptyList(),
    val hotels: List<Poi> = emptyList(),
    val foods: List<Poi> = emptyList(),
    val loading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ImageUiState())
    val uiState: StateFlow<ImageUiState> = _uiState.asStateFlow()

    fun loadImage(keyword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = imageRepository.searchImage(keyword)) {
                is AppResult.Success -> _uiState.update { it.copy(loading = false, images = it.images + (keyword to result.data)) }
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }
}

data class ImageUiState(
    val images: Map<String, Photo> = emptyMap(),
    val loading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeDataViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val mapRepository: MapRepository,
    private val imageRepository: ImageRepository,
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeDataUiState())
    val uiState: StateFlow<HomeDataUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { user ->
                _uiState.update { it.copy(userNickname = user?.nickname.orEmpty(), userAvatar = user?.avatar.orEmpty()) }
            }
        }
        viewModelScope.launch {
            favoriteRepository.observeCityFavorites().collect { favorites ->
                _uiState.update { state -> state.copy(favoriteCities = favorites.map { it.targetId }.toSet()) }
            }
        }
    }

    fun loadCity(city: String) {
        viewModelScope.launch {
            val trimmedCity = city.trim()
            if (trimmedCity.isBlank()) {
                _uiState.update { it.copy(loading = false, errorMessage = "请输入城市") }
                return@launch
            }
            loadCityData(trimmedCity)
        }
    }

    fun loadCurrentCity() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = locationRepository.detectCurrentCity()) {
                is AppResult.Success -> loadCityData(result.data)
                is AppResult.Error -> _uiState.update { it.copy(loading = false, errorMessage = result.message) }
                else -> _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun showLocationUnavailable(message: String) {
        _uiState.update { it.copy(loading = false, errorMessage = message) }
    }

    fun toggleCurrentCityFavorite() {
        viewModelScope.launch {
            val city = _uiState.value.city.trim()
            if (city.isBlank()) return@launch
            favoriteRepository.toggleCityFavorite(city, _uiState.value.cityImage?.url.orEmpty())
        }
    }

    private suspend fun loadCityData(city: String) {
        _uiState.update { it.copy(city = city, loading = true, errorMessage = null) }
        val (weatherResult, scenicResult, hotelsResult, foodsResult, imageResult) = coroutineScope {
            val weather = async { weatherRepository.getWeather(city) }
            val scenic = async { mapRepository.searchScenic(city) }
            val hotels = async { mapRepository.searchHotels(city) }
            val foods = async { mapRepository.searchFood(city) }
            val image = async { imageRepository.searchImage("$city travel city") }
            Quintuple(weather.await(), scenic.await(), hotels.await(), foods.await(), image.await())
        }
        _uiState.update {
            it.copy(
                loading = false,
                weather = (weatherResult as? AppResult.Success)?.data,
                scenic = scenicResult.successOrEmpty(),
                hotels = hotelsResult.successOrEmpty(),
                foods = foodsResult.successOrEmpty(),
                cityImage = (imageResult as? AppResult.Success)?.data,
                errorMessage = listOf(weatherResult, scenicResult, hotelsResult, foodsResult, imageResult)
                    .filterIsInstance<AppResult.Error>()
                    .firstOrNull()?.message
            )
        }
    }
}

data class HomeDataUiState(
    val city: String = "",
    val userNickname: String = "",
    val userAvatar: String = "",
    val favoriteCities: Set<String> = emptySet(),
    val weather: WeatherForecast? = null,
    val scenic: List<Poi> = emptyList(),
    val hotels: List<Poi> = emptyList(),
    val foods: List<Poi> = emptyList(),
    val cityImage: Photo? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TravelPlanEnrichmentViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val mapRepository: MapRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {
    private val _enrichmentState = MutableStateFlow(EnrichmentState())
    val enrichmentState: StateFlow<EnrichmentState> = _enrichmentState.asStateFlow()

    fun enrichPlan(plan: TravelPlan) {
        viewModelScope.launch {
            val city = plan.destination
            _enrichmentState.update { it.copy(loading = true, city = city, errorMessage = null) }
            try {
                weatherRepository.getWeather(city)
                mapRepository.searchScenic(city)
                mapRepository.searchHotels(city)
                mapRepository.searchFood(city)
                imageRepository.searchImage(city)
                _enrichmentState.update { it.copy(loading = false, enriched = true) }
            } catch (e: Exception) {
                _enrichmentState.update { it.copy(loading = false, errorMessage = e.message) }
            }
        }
    }
}

data class EnrichmentState(
    val city: String = "",
    val loading: Boolean = false,
    val enriched: Boolean = false,
    val errorMessage: String? = null
)

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

private fun AppResult<List<Poi>>.successOrEmpty(): List<Poi> = (this as? AppResult.Success)?.data.orEmpty()