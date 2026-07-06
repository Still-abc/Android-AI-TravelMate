package com.example.ai.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ai.ai.viewmodel.AIViewModel
import java.time.LocalDate
import com.example.ai.component.Avatar
import com.example.ai.component.BannerImages
import com.example.ai.component.BudgetCard
import com.example.ai.component.CommonCard
import com.example.ai.component.PrimaryButton
import com.example.ai.component.ScenicCard
import com.example.ai.component.SectionHeader
import com.example.ai.component.TravelBanner
import com.example.ai.component.TravelCard
import com.example.ai.component.TravelTextField
import com.example.ai.component.LoadingView
import com.example.ai.config.ApiConfig
import com.example.ai.navigation.AppRoute
import com.example.ai.thirdparty.model.Poi
import com.example.ai.thirdparty.viewmodel.HomeDataViewModel
import com.example.ai.theme.TravelAmber
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelCoral
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal

@Composable
fun HomeScreen(navController: NavHostController, viewModel: HomeDataViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val aiViewModel: AIViewModel = hiltViewModel()
    val aiState by aiViewModel.uiState.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.loadCurrentCity()
        } else {
            viewModel.showLocationUnavailable("未授予定位权限，请搜索城市")
        }
    }

    LaunchedEffect(Unit) {
        if (context.hasLocationPermission()) {
            viewModel.loadCurrentCity()
        } else {
            locationPermissionLauncher.launch(LocationPermissions)
        }
    }

    LaunchedEffect(uiState.city) {
        if (search.isBlank() && uiState.city.isNotBlank()) search = uiState.city
    }

    val displayCity = uiState.city.ifBlank { "当前位置" }
    val isCityFavorite = uiState.city.isNotBlank() && uiState.city in uiState.favoriteCities
    var pendingItineraryNavigation by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = TravelSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
    ) {
        item { Spacer(Modifier.height(TravelSpacing.medium)) }
        item { HomeHeader(uiState.userAvatar, uiState.userNickname) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TravelTextField(search, { search = it }, "搜索城市", modifier = Modifier.weight(1f), leadingIcon = Icons.Filled.Search)
                Surface(
                    onClick = { viewModel.loadCity(search) },
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.padding(start = TravelSpacing.small)
                ) {
                    Text("搜索", Modifier.padding(horizontal = 16.dp, vertical = 14.dp), color = Color.White)
                }
            }
        }
        item {
            TravelBanner(
                imageUrl = uiState.cityImage?.url ?: BannerImages[1],
                title = if (uiState.city.isBlank()) "正在定位你的城市" else "$displayCity 智能旅行灵感",
                subtitle = uiState.weather?.let { "${it.weather} · ${it.temperature} · 为你匹配景点、酒店与美食" }
                    ?: "自动获取天气、景点、酒店、美食与图片",
                badge = if (uiState.loading) "加载中" else "第三方 API"
            )
        }
        if (uiState.city.isNotBlank()) {
            item { CityFavoriteAction(displayCity, isCityFavorite, viewModel::toggleCurrentCityFavorite) }
        }
        item { QuickActions(navController) }
        uiState.errorMessage?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
        item { WeatherSummary(navController, uiState.weather?.city ?: displayCity, uiState.weather?.temperature, uiState.weather?.weather) }
        item { PoiRow("热门景点", PoiImageCategory.Scenic, uiState.scenic, uiState.city, navController) }
        item { PoiRow("酒店推荐", PoiImageCategory.Hotel, uiState.hotels, uiState.city, navController) }
        item { PoiRow("美食推荐", PoiImageCategory.Food, uiState.foods, uiState.city, navController) }
        item {
                SectionHeader("推荐攻略")
            Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
                if (aiState.loading) {
                    LoadingView(modifier = Modifier.height(160.dp))
                }
                aiState.errorMessage?.let { error ->
                    CommonCard { Text(error, color = MaterialTheme.colorScheme.error) }
                }
                TravelCard(
                    "第一次去${displayCity}怎么玩",
                    "结合天气、热门 POI 和图片数据生成下一步攻略",
                    uiState.cityImage?.url ?: BannerImages[0],
                    accent = TravelBlue,
                    onClick = {
                        if (uiState.city.isBlank()) {
                            viewModel.showLocationUnavailable("请先定位城市或搜索城市")
                        } else {
                            val start = LocalDate.now().toString()
                            val end = LocalDate.now().plusDays(2).toString()
                            pendingItineraryNavigation = true
                            aiViewModel.generateTravelPlan("当前位置", uiState.city, start, end, "4500", 1, listOf("景点", "美食"))
                        }
                    }
                )
                TravelCard(
                    "适合周末的轻旅行",
                    "酒店、美食和景点都已从第三方 API 自动补充",
                    BannerImages[2],
                    accent = TravelTeal,
                    onClick = {
                        if (uiState.city.isBlank()) {
                            viewModel.showLocationUnavailable("请先定位城市或搜索城市")
                        } else {
                            val start = LocalDate.now().toString()
                            val end = LocalDate.now().plusDays(1).toString()
                            pendingItineraryNavigation = true
                            aiViewModel.generateTravelPlan("当前位置", uiState.city, start, end, "2000", 1, listOf("休闲", "美食"))
                        }
                    }
                )
            }
        }
        item { Spacer(Modifier.height(TravelSpacing.large)) }
    }

    LaunchedEffect(aiState.travelPlan, pendingItineraryNavigation) {
        if (pendingItineraryNavigation && aiState.travelPlan != null) {
            pendingItineraryNavigation = false
            navController.navigate(AppRoute.Itinerary.route)
        }
    }
}

@Composable
private fun CityFavoriteAction(city: String, isFavorite: Boolean, onToggle: () -> Unit) {
    Surface(
        onClick = onToggle,
        color = if (isFavorite) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = TravelSpacing.medium, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f).padding(start = TravelSpacing.medium)) {
                Text(if (isFavorite) "已收藏 $city" else "收藏 $city", style = MaterialTheme.typography.titleMedium)
                Text("收藏后会出现在我的收藏", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HomeHeader(avatar: String, nickname: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(avatar)
        Column(Modifier.weight(1f).padding(horizontal = TravelSpacing.medium)) {
            Text(if (nickname.isBlank()) "欢迎回来！" else "$nickname，欢迎回来！", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(if (nickname.isBlank()) "今天想去哪里？" else nickname, style = MaterialTheme.typography.titleLarge)
        }
        IconButton(onClick = {}) { Icon(Icons.Filled.Notifications, contentDescription = "通知") }
    }
}

@Composable
private fun QuickActions(navController: NavHostController) {
    CommonCard {
        Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(Brush.linearGradient(listOf(TravelBlue, TravelTeal))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White)
                }
                Column(Modifier.weight(1f).padding(horizontal = TravelSpacing.medium)) {
                    Text("AI 旅行规划", style = MaterialTheme.typography.titleMedium)
                    Text("生成方案后自动补充天气、POI 和图片", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small), modifier = Modifier.fillMaxWidth()) {
                BudgetCard("预算", "¥4,500", Modifier.weight(1f), TravelBlue)
                BudgetCard("天数", "3天2晚", Modifier.weight(1f), TravelTeal)
                BudgetCard("灵感", "实时", Modifier.weight(1f), TravelCoral)
            }
            PrimaryButton("开始规划", onClick = { navController.navigate(AppRoute.Planner.route) }, icon = Icons.Filled.TravelExplore)
        }
    }
}

@Composable
private fun WeatherSummary(navController: NavHostController, city: String, temperature: String?, weather: String?) {
    SectionHeader("今日天气")
    CommonCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(TravelBlue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Cloud, contentDescription = null, tint = TravelBlue, modifier = Modifier.size(32.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = TravelSpacing.medium)) {
                Text("$city · ${temperature ?: "加载中"}", style = MaterialTheme.typography.titleMedium)
                Text(weather ?: "正在获取真实天气", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                onClick = { navController.navigate(AppRoute.Weather.createRoute(city)) },
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text("详情", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun PoiRow(title: String, category: PoiImageCategory, pois: List<Poi>, city: String, navController: NavHostController) {
    SectionHeader(title, action = "地图") { navController.navigate(AppRoute.Map.createRoute(city)) }
    if (pois.isEmpty()) {
        CommonCard { Text("暂无数据，确认 API Key 后下拉搜索城市重试。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
    } else {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
            items(pois.take(8)) { poi ->
                ScenicCard(
                    title = poi.name,
                    location = poi.address.ifBlank { poi.type },
                    imageUrl = poi.imageUrlFor(category),
                    rating = poi.rating.ifBlank { "--" },
                    modifier = Modifier.size(width = 190.dp, height = 238.dp),
                    fallbackImageUrl = category.fallbackImage(poi),
                    onClick = { navController.navigate(poi.detailRoute(category)) }
                )
            }
        }
    }
}

private val unusedPalette = listOf(TravelAmber)

private val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private enum class PoiImageCategory { Scenic, Hotel, Food }

private val ScenicFallbackImages = listOf(
    "https://images.unsplash.com/photo-1548919973-5cef591cdbc9?auto=format&fit=crop&w=900&q=80",
    "https://images.unsplash.com/photo-1523731407965-2430cd12f5e4?auto=format&fit=crop&w=900&q=80"
)

private val HotelFallbackImages = listOf(
    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=900&q=80",
    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=900&q=80"
)

private val FoodFallbackImages = listOf(
    "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=900&q=80",
    "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=900&q=80"
)

private fun Poi.imageUrlFor(category: PoiImageCategory): String =
    photoUrl.ifBlank { staticMapUrl() }.ifBlank {
        category.fallbackImage(this)
    }

private fun PoiImageCategory.fallbackImage(poi: Poi): String {
    val images = when (this) {
        PoiImageCategory.Scenic -> ScenicFallbackImages
        PoiImageCategory.Hotel -> HotelFallbackImages
        PoiImageCategory.Food -> FoodFallbackImages
    }
    return images[(poi.name.length + poi.type.length) % images.size]
}

private fun Poi.detailRoute(category: PoiImageCategory): String =
    AppRoute.Scenic.createRoute(
        name = name,
        address = address.ifBlank { type },
        tel = telephone,
        lon = location.longitude,
        lat = location.latitude,
        rating = rating,
        photo = imageUrlFor(category),
        type = type,
        category = category.name
    )

private fun Poi.staticMapUrl(): String {
    if (!ApiConfig.hasAmapConfig() || location.longitude == 0.0 || location.latitude == 0.0) return ""
    val position = "${location.longitude},${location.latitude}"
    return "https://restapi.amap.com/v3/staticmap?location=$position&zoom=16&size=400*240&markers=mid,,A:$position&key=${ApiConfig.AMAP_WEB_KEY}"
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED