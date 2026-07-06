package com.example.ai.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ai.component.CommonCard
import com.example.ai.component.SectionHeader
import com.example.ai.component.TravelBanner
import com.example.ai.component.TravelCard
import com.example.ai.component.TravelTopBar
import com.example.ai.component.WeatherCard
import com.example.ai.navigation.AppRoute
import com.example.ai.thirdparty.viewmodel.WeatherViewModel
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal

@Composable
fun WeatherScreen(navController: NavHostController, viewModel: WeatherViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cityArg = navController
        .currentBackStackEntry
        ?.arguments
        ?.getString(AppRoute.Weather.CITY_ARG)
        ?.trim()
        ?.takeIf { it.isNotBlank() }

    LaunchedEffect(cityArg) { viewModel.loadWeather(cityArg ?: "上海") }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TravelTopBar("旅行天气", true, { navController.navigateUp() })
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(TravelSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
        ) {
            TravelBanner(
                imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                title = "${uiState.city} · 实时天气",
                subtitle = uiState.forecast?.let { "${it.weather}，${it.temperature}，风力 ${it.wind}" } ?: "正在获取真实天气与 AI 建议",
                badge = "Weather AI"
            )
            when {
                uiState.loading -> CommonCard { Text("正在获取天气数据...", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                uiState.errorMessage != null -> Text(uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                uiState.forecast != null -> {
                    val forecast = uiState.forecast!!
                    WeatherCard(
                        city = forecast.city,
                        date = forecast.date,
                        weather = forecast.weather,
                        temperature = forecast.temperature,
                        details = listOf(
                            "空气质量" to forecast.airQuality,
                            "湿度" to forecast.humidity,
                            "风力" to forecast.wind,
                            "降雨" to forecast.precipitation.ifBlank { "暂无" }
                        ),
                        advice = uiState.advice?.let { advice ->
                            "穿搭：${advice.outfit}\n雨伞：${advice.umbrella}\n防晒：${advice.sunscreen}\n户外：${advice.outdoor}"
                        } ?: if (uiState.aiLoading) "AI 正在生成天气建议..." else "AI 建议将在天气获取成功后自动生成。"
                    )
                }
            }
            SectionHeader("AI 旅行提醒")
            TravelCard(
                title = "穿搭与雨伞",
                subtitle = uiState.advice?.let { "${it.outfit}；${it.umbrella}" } ?: uiState.adviceError ?: "等待 AI 建议生成",
                imageUrl = "https://images.unsplash.com/photo-1518005020951-eccb494ad742?auto=format&fit=crop&w=900&q=80",
                accent = TravelBlue
            )
            TravelCard(
                title = "防晒与户外",
                subtitle = uiState.advice?.let { "${it.sunscreen}；${it.outdoor}" } ?: "结合实时天气自动生成",
                imageUrl = "https://images.unsplash.com/photo-1538428494232-9c0d8a3ab403?auto=format&fit=crop&w=900&q=80",
                accent = TravelTeal
            )
            CommonCard {
                Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                    Icon(if (uiState.aiLoading) Icons.Filled.AutoAwesome else Icons.Filled.Umbrella, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(if (uiState.aiLoading) "AI 正在分析天气" else "天气与 AI 建议已接入", style = MaterialTheme.typography.titleMedium)
                    Text("天气来自 ApiConfig.WEATHER_API_KEY，AI 建议来自已接入的大模型。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(TravelSpacing.large))
        }
    }
}