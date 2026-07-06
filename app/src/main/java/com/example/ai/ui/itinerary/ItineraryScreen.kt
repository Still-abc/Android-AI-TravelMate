package com.example.ai.ui.itinerary

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.ai.ai.model.Budget
import com.example.ai.ai.model.Schedule
import com.example.ai.ai.model.TravelPlan
import com.example.ai.ai.repository.GeneratedPlanRepository
import com.example.ai.component.BudgetCard
import com.example.ai.component.CommonCard
import com.example.ai.component.PrimaryButton
import com.example.ai.component.SectionHeader
import com.example.ai.component.TimelineItem
import com.example.ai.component.TravelBanner
import com.example.ai.component.TravelTimeline
import com.example.ai.component.TravelTopBar
import com.example.ai.navigation.AppRoute
import com.example.ai.theme.TravelAmber
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelCoral
import com.example.ai.theme.TravelSky
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal
import com.example.ai.thirdparty.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ItineraryViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    generatedPlanRepository: GeneratedPlanRepository
) : ViewModel() {
    val latestPlan: StateFlow<TravelPlan?> = generatedPlanRepository.latestPlan

    private val _weatherForecast = MutableStateFlow<WeatherForecastView?>(null)
    val weatherForecast: StateFlow<WeatherForecastView?> = _weatherForecast.asStateFlow()

    private var lastWeatherCity = ""

    fun fetchWeatherForPlan(plan: TravelPlan) {
        val city = plan.destination.trim()
        if (city.isBlank()) {
            _weatherForecast.value = null
            return
        }
        if (city == lastWeatherCity && _weatherForecast.value != null) return
        lastWeatherCity = city
        _weatherForecast.value = null
        viewModelScope.launch {
            val firstDay = plan.days.firstOrNull()?.date ?: "today"
            when (val result = weatherRepository.getWeather(city, firstDay)) {
                is com.example.ai.common.AppResult.Success -> {
                    val w = result.data
                    _weatherForecast.value = WeatherForecastView(
                        weather = w.weather,
                        temperature = w.temperature,
                        tempMax = w.tempMax,
                        tempMin = w.tempMin,
                        humidity = w.humidity,
                        wind = w.wind
                    )
                }
                is com.example.ai.common.AppResult.Error -> {
                    _weatherForecast.value = null
                }
                else -> {
                    _weatherForecast.value = null
                }
            }
        }
    }
}

data class WeatherForecastView(
    val weather: String,
    val temperature: String,
    val tempMax: String,
    val tempMin: String,
    val humidity: String,
    val wind: String
)

@Composable
fun ItineraryScreen(navController: NavHostController, viewModel: ItineraryViewModel = hiltViewModel()) {
    val generatedPlan by viewModel.latestPlan.collectAsStateWithLifecycle()
    val weatherForecast by viewModel.weatherForecast.collectAsStateWithLifecycle()
    val plan = generatedPlan ?: samplePlan
    val planBadge = if (generatedPlan == null) "示例行程" else "AI已生成"

    LaunchedEffect(generatedPlan) {
        if (generatedPlan != null) {
            viewModel.fetchWeatherForPlan(generatedPlan!!)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TravelTopBar(plan.title, canNavigateBack = true, onNavigateBack = { navController.navigateUp() })
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(TravelSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
        ) {
            TravelBanner(
                imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
                title = plan.title,
                subtitle = "${plan.departure} → ${plan.destination} · ${plan.days.size} 日行程",
                badge = planBadge
            )
            CommonCard {
                Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
                    Text("行程概览", style = MaterialTheme.typography.titleLarge)
                    Text(plan.overviewText(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                        BudgetCard("预算", plan.budget.total.currencyText(), Modifier.weight(1f), TravelBlue)
                        BudgetCard("住宿", plan.budget.hotel.currencyText(), Modifier.weight(1f), TravelTeal)
                        BudgetCard("活动", plan.activityCountText(), Modifier.weight(1f), TravelCoral)
                    }
                }
            }
            if (weatherForecast != null) {
                Surface(
                    onClick = { navController.navigate(AppRoute.Weather.createRoute(plan.destination)) },
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    WeatherClothingCard(weatherForecast!!, plan.destination)
                }
            }
            plan.days.forEach { day ->
                SectionHeader("Day ${day.day} · ${day.theme}")
                TravelTimeline(day.schedules.map { it.toTimelineItem() })
            }
            if (plan.hotels.isNotEmpty() || plan.foods.isNotEmpty() || plan.tips.isNotEmpty()) {
                CommonCard {
                    Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                        Text("旅行提示", style = MaterialTheme.typography.titleLarge)
                        plan.hotels.take(1).forEach { hotel ->
                            Text("住宿：${hotel.name}，${hotel.reason}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        plan.foods.take(1).forEach { food ->
                            Text("美食：${food.name}，${food.reason}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        plan.tips.take(2).forEach { tip ->
                            Text("${tip.title}：${tip.content}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            PrimaryButton("打开地图路线", onClick = { navController.navigate(AppRoute.Map.createRoute(plan.destination)) }, icon = Icons.Filled.Map)
            Spacer(Modifier.height(TravelSpacing.large))
        }
    }
}

@Composable
private fun WeatherClothingCard(weather: WeatherForecastView, city: String, navController: NavHostController? = null) {
    val clothingAdvice = generateClothingAdvice(weather.weather, weather.temperature)
    val weatherColor = weatherColor(weather.weather)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = weatherColor.copy(alpha = 0.08f))
    ) {
        Column(Modifier.padding(TravelSpacing.medium), verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(weatherColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = weatherIcon(weather.weather),
                        contentDescription = weather.weather,
                        tint = weatherColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(TravelSpacing.medium))
                Column(Modifier.weight(1f)) {
                    Text("$city 天气", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(weather.weather, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(weather.temperature, style = MaterialTheme.typography.headlineSmall, color = weatherColor, fontWeight = FontWeight.Bold)
            }
            if (weather.tempMax.isNotBlank() || weather.humidity.isNotBlank() || weather.wind.isNotBlank()) {
                Row(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                    if (weather.tempMax.isNotBlank()) {
                        WeatherDetailTile("最高温", weather.tempMax, TravelCoral, Modifier.weight(1f))
                    }
                    if (weather.tempMin.isNotBlank()) {
                        WeatherDetailTile("最低温", weather.tempMin, TravelSky, Modifier.weight(1f))
                    }
                    if (weather.humidity.isNotBlank()) {
                        WeatherDetailTile("湿度", weather.humidity, TravelTeal, Modifier.weight(1f))
                    }
                    if (weather.wind.isNotBlank()) {
                        WeatherDetailTile("风力", weather.wind, TravelBlue, Modifier.weight(1f))
                    }
                }
            }
            Surface(
                color = weatherColor.copy(alpha = 0.14f),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(TravelSpacing.medium),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Filled.Checkroom, contentDescription = null, tint = weatherColor, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(TravelSpacing.small))
                    Text(
                        clothingAdvice,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailTile(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.10f)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
    }
}

private fun weatherIcon(weather: String): ImageVector = when {
    weather.contains("晴") || weather.contains("多云") -> Icons.Filled.WbSunny
    weather.contains("雨") || weather.contains("雷") -> Icons.Filled.WaterDrop
    weather.contains("雪") || weather.contains("冰") -> Icons.Filled.Cloud
    weather.contains("阴") || weather.contains("雾") || weather.contains("霾") -> Icons.Filled.Cloud
    weather.contains("风") -> Icons.Filled.Air
    else -> Icons.Filled.Cloud
}

private fun weatherColor(weather: String): Color = when {
    weather.contains("晴") -> TravelAmber
    weather.contains("多云") -> TravelAmber
    weather.contains("阴") -> TravelSky
    weather.contains("雨") -> TravelBlue
    weather.contains("雪") -> TravelSky
    weather.contains("雾") || weather.contains("霾") -> TravelSky.copy(alpha = 0.7f)
    weather.contains("风") -> TravelTeal
    else -> TravelSky
}

private fun generateClothingAdvice(weather: String, temperature: String): String {
    val temp = temperature.replace("℃", "").toIntOrNull()
    return when {
        temp == null -> "建议根据当天实际天气情况搭配衣物，携带雨具以备不时之需。"
        temp >= 30 -> "天气炎热，建议穿短袖、短裤、裙子等清凉衣物，做好防晒（戴帽子、太阳镜），多补充水分。"
        temp in 25..29 -> "天气温暖，建议穿短袖、薄长裤或连衣裙，可携带薄外套应对早晚温差。"
        temp in 20..24 -> "天气舒适，建议穿T恤搭配薄外套或衬衫，早晚可加一件针织衫。"
        temp in 15..19 -> "天气微凉，建议穿长袖、薄毛衣或卫衣，搭配长裤和运动鞋。"
        temp in 10..14 -> "天气较凉，建议穿外套、夹克或薄羽绒服，搭配长裤和舒适步行鞋。"
        temp in 5..9 -> "天气寒冷，建议穿羽绒服、厚外套、毛衣，佩戴围巾和手套注意保暖。"
        temp < 5 -> "天气严寒，建议穿厚羽绒服、保暖内衣、毛衣、围巾、手套和帽子，注意防寒保暖。"
        else -> "建议根据当天实际天气情况搭配衣物。"
    }.let { advice ->
        if (weather.contains("雨")) "$advice\n\n🌂 当天有雨，建议携带雨伞或雨衣。"
        else if (weather.contains("雪")) "$advice\n\n❄️ 当天有雪，建议穿防滑鞋，注意出行安全。"
        else if (weather.contains("晴") && temp != null && temp >= 25) "$advice\n\n☀️ 紫外线较强，建议涂抹防晒霜、佩戴太阳镜。"
        else advice
    }
}

private fun TravelPlan.overviewText(): String {
    val firstTip = tips.firstOrNull()?.content
    return firstTip ?: "已根据目的地、预算和偏好生成每日路线，建议按天气和体力保留弹性调整时间。"
}

private fun TravelPlan.activityCountText(): String = "${days.sumOf { it.schedules.size }}项"

private fun Schedule.toTimelineItem(): TimelineItem = TimelineItem(
    time = time,
    title = title,
    description = listOf(description, location.takeIf { it.isNotBlank() }?.let { "地点：$it" }).filterNotNull().joinToString("\n"),
    icon = type.toTimelineIcon(),
    color = type.toTimelineColor(),
    expanded = true
)

private fun String.toTimelineIcon(): ImageVector = when (lowercase()) {
    "food", "restaurant" -> Icons.Filled.Restaurant
    "hotel" -> Icons.Filled.Hotel
    "shopping" -> Icons.Filled.ShoppingBag
    "traffic", "scenic", "activity" -> Icons.Filled.TravelExplore
    else -> Icons.Filled.LocationOn
}

private fun String.toTimelineColor() = when (lowercase()) {
    "food", "restaurant", "shopping" -> TravelCoral
    "hotel" -> TravelBlue
    "traffic", "scenic", "activity" -> TravelTeal
    else -> TravelBlue
}

private fun Double.currencyText(): String = "¥%,.0f".format(this)

private val samplePlan = TravelPlan(
    title = "杭州 3 日轻松游",
    departure = "当前位置",
    destination = "杭州",
    days = listOf(
        com.example.ai.ai.model.TravelDay(
            day = 1,
            date = "2026-07-04",
            theme = "西湖与城市初体验",
            schedules = listOf(
                Schedule("09:30", "西湖环湖漫步", "scenic", "西湖", "从断桥出发，避开早高峰人流，沿白堤到平湖秋月。", 0.0, 120),
                Schedule("12:10", "湖滨杭帮菜", "food", "湖滨银泰", "选择靠近湖滨银泰的餐厅，适合午间休整。", 160.0, 75),
                Schedule("15:00", "灵隐寺与飞来峰", "scenic", "灵隐寺", "下午进入景区更从容，建议预留 2.5 小时。", 90.0, 150)
            )
        ),
        com.example.ai.ai.model.TravelDay(
            day = 2,
            date = "2026-07-05",
            theme = "茶园与湖畔休闲",
            schedules = listOf(
                Schedule("10:00", "龙井茶园", "scenic", "龙井村", "茶园步道、村落咖啡和轻徒步，适合拍照。", 80.0, 150),
                Schedule("17:30", "湖畔酒店入住", "hotel", "西湖周边", "选择近地铁与湖区的精品酒店，晚间出行更方便。", 600.0, 60)
            )
        )
    ),
    budget = Budget(total = 4500.0, hotel = 1800.0, food = 800.0, traffic = 900.0, ticket = 500.0, other = 500.0)
)