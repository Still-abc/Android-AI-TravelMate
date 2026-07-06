package com.example.ai.ui.scenic

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ai.component.BannerImages
import com.example.ai.component.BudgetCard
import com.example.ai.component.CommonCard
import com.example.ai.component.NetworkImage
import com.example.ai.component.PrimaryButton
import com.example.ai.component.TravelTopBar
import com.example.ai.config.ApiConfig
import com.example.ai.thirdparty.model.Location
import com.example.ai.thirdparty.model.Poi
import com.example.ai.theme.TravelAmber
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelCoral
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal

@Composable
fun ScenicDetailScreen(
    navController: NavHostController,
    name: String? = null,
    address: String? = null,
    telephone: String? = null,
    longitude: Double? = null,
    latitude: Double? = null,
    rating: String? = null,
    photoUrl: String? = null,
    poiType: String? = null,
    category: String? = null
) {
    val context = LocalContext.current
    val detailCategory = DetailCategory.from(category)
    val displayName = name.orEmpty().ifBlank { "地点详情" }
    val displayAddress = address.orEmpty().ifBlank { "暂无地址" }
    val displayType = poiType.orEmpty().ifBlank { detailCategory.defaultType }
    val poi = name?.takeIf { it.isNotBlank() }?.let {
        Poi(
            id = it,
            name = it,
            type = displayType,
            address = displayAddress,
            telephone = telephone.orEmpty(),
            location = Location(longitude ?: 0.0, latitude ?: 0.0),
            rating = rating.orEmpty(),
            photoUrl = photoUrl.orEmpty()
        )
    }
    val imageUrl = photoUrl.orEmpty().ifBlank { poi?.staticMapUrl().orEmpty() }.ifBlank { BannerImages.first() }
    val fallbackImageUrl = detailCategory.fallbackImage(displayName, displayType)

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TravelTopBar(detailCategory.title, true, { navController.navigateUp() })
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(TravelSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                NetworkImage(imageUrl, displayName, Modifier.fillMaxSize(), fallbackImageUrl)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.58f)))))
                IconButton(onClick = {}, modifier = Modifier.align(Alignment.TopEnd).padding(TravelSpacing.medium)) {
                    Icon(Icons.Filled.Favorite, contentDescription = "收藏", tint = Color.White)
                }
                Column(Modifier.align(Alignment.BottomStart).padding(TravelSpacing.medium)) {
                    Text(displayName, style = MaterialTheme.typography.headlineMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(displayAddress, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.9f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small), modifier = Modifier.fillMaxWidth()) {
                BudgetCard("评分", rating.orEmpty().ifBlank { "--" }, Modifier.weight(1f), TravelAmber)
                BudgetCard(detailCategory.middleLabel, detailCategory.middleValue(displayType), Modifier.weight(1f), TravelTeal)
                BudgetCard(detailCategory.rightLabel, detailCategory.rightValue, Modifier.weight(1f), TravelBlue)
            }

            CommonCard {
                Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
                    Text(detailCategory.infoTitle, style = MaterialTheme.typography.titleLarge)
                    DetailLine("地址", displayAddress)
                    DetailLine("电话", telephone.orEmpty().ifBlank { "暂无" })
                    DetailLine("经纬度", if (longitude != null && latitude != null) "$longitude, $latitude" else "暂无")
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
                        Row(Modifier.padding(TravelSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = TravelAmber)
                            Column(Modifier.padding(start = TravelSpacing.medium)) {
                                Text("AI 介绍", style = MaterialTheme.typography.titleMedium)
                                Text(detailCategory.advice(displayName), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccessTime, contentDescription = null, tint = TravelCoral, modifier = Modifier.size(20.dp))
                        Text(detailCategory.timeHint, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            PrimaryButton("在地图查看", onClick = { poi?.let { context.openMapForPoi(it) } ?: navController.navigateUp() }, icon = Icons.Filled.Map)
            Spacer(Modifier.height(TravelSpacing.large))
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(0.28f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
    }
}

private enum class DetailCategory(
    val title: String,
    val infoTitle: String,
    val defaultType: String,
    val middleLabel: String,
    val rightLabel: String,
    val rightValue: String,
    val timeHint: String
) {
    Scenic("景点详情", "景点信息", "景点", "门票", "开放", "全天", " 推荐游玩 1.5 - 2.5 小时"),
    Hotel("酒店详情", "酒店信息", "酒店", "类型", "营业", "全天", " 建议结合位置、评分和电话确认入住信息"),
    Food("美食详情", "美食信息", "美食", "类型", "营业", "详情", " 建议结合评分、距离和营业状态安排用餐"),
    Place("地点详情", "地点信息", "地点", "类型", "状态", "详情", " 建议打开地图确认营业状态与路线");

    fun middleValue(type: String): String = when (this) {
        Scenic -> "免费"
        else -> type.substringBefore(';').ifBlank { defaultType }
    }

    fun advice(name: String): String = when (this) {
        Scenic -> "建议提前查看天气与交通，图片与地点信息优先来自高德 Web API。"
        Hotel -> "建议优先比较位置、评分、交通和电话确认信息，图片与地点信息优先来自高德 Web API。"
        Food -> "建议结合评分、排队情况和当前位置安排用餐，图片与地点信息优先来自高德 Web API。"
        Place -> "已为你载入$name 的地点信息，可打开地图查看路线与周边情况。"
    }

    fun fallbackImage(name: String, type: String): String {
        val images = when (this) {
            Scenic, Place -> ScenicFallbackImages
            Hotel -> HotelFallbackImages
            Food -> FoodFallbackImages
        }
        return images[(name.length + type.length) % images.size]
    }

    companion object {
        fun from(value: String?): DetailCategory = when (value) {
            "Scenic" -> Scenic
            "Hotel" -> Hotel
            "Food" -> Food
            else -> Place
        }
    }
}

private val ScenicFallbackImages = listOf(
    "https://images.unsplash.com/photo-1548919973-5cef591cdbc9?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1523731407965-2430cd12f5e4?auto=format&fit=crop&w=1200&q=80"
)

private val HotelFallbackImages = listOf(
    "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80"
)

private val FoodFallbackImages = listOf(
    "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=1200&q=80"
)

private fun android.content.Context.openMapForPoi(poi: Poi) {
    val amapUri = Uri.parse("androidamap://viewMap?sourceApplication=AITravelMate&poiname=${Uri.encode(poi.name)}&lat=${poi.location.latitude}&lon=${poi.location.longitude}&dev=0")
    val amapIntent = Intent(Intent.ACTION_VIEW, amapUri)
    val webUri = Uri.parse("https://uri.amap.com/marker?position=${poi.location.longitude},${poi.location.latitude}&name=${Uri.encode(poi.name)}")
    val intent = if (amapIntent.resolveActivity(packageManager) != null) amapIntent else Intent(Intent.ACTION_VIEW, webUri)
    startActivity(intent)
}

private fun Poi.staticMapUrl(): String {
    if (!ApiConfig.hasAmapConfig() || location.longitude == 0.0 || location.latitude == 0.0) return ""
    val position = "${location.longitude},${location.latitude}"
    return "https://restapi.amap.com/v3/staticmap?location=$position&zoom=16&size=400*240&markers=mid,,A:$position&key=${ApiConfig.AMAP_WEB_KEY}"
}
