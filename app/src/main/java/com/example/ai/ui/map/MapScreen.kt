package com.example.ai.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ai.component.CommonCard
import com.example.ai.component.PrimaryButton
import com.example.ai.component.SectionHeader
import com.example.ai.component.TravelTopBar
import com.example.ai.thirdparty.model.Poi
import com.example.ai.thirdparty.viewmodel.MapViewModel
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelCoral
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal

@Composable
fun MapScreen(navController: NavHostController, initialCity: String? = null, viewModel: MapViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            viewModel.loadCurrentCity()
        } else {
            viewModel.showLocationUnavailable("未授予定位权限，请从首页搜索城市后打开地图")
        }
    }

    LaunchedEffect(initialCity) {
        val city = initialCity?.trim().orEmpty()
        when {
            city.isNotBlank() -> viewModel.loadCity(city)
            context.hasLocationPermission() -> viewModel.loadCurrentCity()
            else -> locationPermissionLauncher.launch(LocationPermissions)
        }
    }

    val displayCity = uiState.city.ifBlank { if (uiState.loading) "正在定位" else "当前位置" }
    val totalPois = uiState.scenic.size + uiState.hotels.size + uiState.foods.size

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TravelTopBar("地图查看", true, { navController.navigateUp() })
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(TravelSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
        ) {
            item {
                MapSummaryCard(displayCity = displayCity, totalPois = totalPois)
            }
            if (uiState.loading) {
                item { CommonCard { Text("正在加载高德 POI...", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
            }
            uiState.errorMessage?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
            item { PoiSection("热门景点", uiState.scenic, TravelBlue, context::openMapForPoi) }
            item { PoiSection("附近酒店", uiState.hotels, TravelTeal, context::openMapForPoi) }
            item { PoiSection("附近美食", uiState.foods, TravelCoral, context::openMapForPoi) }
            item { Spacer(Modifier.height(TravelSpacing.large)) }
        }
    }
}

@Composable
private fun MapSummaryCard(displayCity: String, totalPois: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(TravelBlue.copy(alpha = 0.18f), TravelTeal.copy(alpha = 0.16f), TravelCoral.copy(alpha = 0.12f))))
    ) {
        Icon(Icons.Filled.Map, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), modifier = Modifier.size(132.dp).align(Alignment.Center))
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(TravelSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TravelSpacing.extraSmall)
        ) {
            Text("$displayCity POI", style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("已整理 $totalPois 个地点，点开前先看清名称、类型和地址。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PoiSection(title: String, pois: List<Poi>, accent: Color, onOpenMap: (Poi) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
        SectionHeader("$title · ${pois.size}处")
        if (pois.isEmpty()) {
            CommonCard { Text("暂无数据，请确认高德 Key 与网络可用。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            pois.take(8).forEachIndexed { index, poi ->
                PoiCard(
                    index = index + 1,
                    poi = poi,
                    accent = accent,
                    onOpenMap = onOpenMap
                )
            }
        }
    }
}

@Composable
private fun PoiCard(index: Int, poi: Poi, accent: Color, onOpenMap: (Poi) -> Unit) {
    CommonCard {
        Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(index.toString(), style = MaterialTheme.typography.titleMedium, color = accent)
                }
                Column(
                    modifier = Modifier.weight(1f).padding(start = TravelSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(TravelSpacing.extraSmall)
                ) {
                    Text(poi.name, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(poi.displayType(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(poi.address.ifBlank { "暂无详细地址" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(poi.metaText(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            PrimaryButton("打开地图路线", onClick = { onOpenMap(poi) }, icon = Icons.Filled.Map)
        }
    }
}

private fun Poi.displayType(): String = type.split(';', '|').firstOrNull { it.isNotBlank() } ?: "地点"

private fun Poi.metaText(): String = listOfNotNull(
    rating.takeIf { it.isNotBlank() }?.let { "评分 $it" },
    telephone.takeIf { it.isNotBlank() }?.let { "电话 $it" }
).joinToString(" · ").ifBlank { "暂无评分和电话" }

private fun Context.openMapForPoi(poi: Poi) {
    val hasLocation = poi.location.longitude != 0.0 && poi.location.latitude != 0.0
    val name = poi.name.ifBlank { poi.address }.ifBlank { "目的地" }
    val intent = if (hasLocation) {
        val amapUri = Uri.parse("androidamap://viewMap?sourceApplication=AITravelMate&poiname=${Uri.encode(name)}&lat=${poi.location.latitude}&lon=${poi.location.longitude}&dev=0")
        val amapIntent = Intent(Intent.ACTION_VIEW, amapUri)
        if (amapIntent.resolveActivity(packageManager) != null) {
            amapIntent
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://uri.amap.com/marker?position=${poi.location.longitude},${poi.location.latitude}&name=${Uri.encode(name)}"))
        }
    } else {
        Intent(Intent.ACTION_VIEW, Uri.parse("https://uri.amap.com/search?keyword=${Uri.encode(name)}"))
    }
    startActivity(intent)
}

private val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
