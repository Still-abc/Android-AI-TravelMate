package com.example.ai.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.ai.navigation.AppRoute
import com.example.ai.theme.TravelAmber
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelCoral
import com.example.ai.theme.TravelSky
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal

val BannerImages = listOf(
    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1200&q=80",
    "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=1200&q=80"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelTopBar(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    actionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            }
        },
        actions = {
            if (actionIcon != null && onActionClick != null) {
                IconButton(onClick = onActionClick) {
                    Icon(actionIcon, contentDescription = null)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun TravelBottomBar(navController: NavHostController, modifier: Modifier = Modifier) {
    val destinations = listOf(
        BottomNavDestination("首页", AppRoute.Home, Icons.Filled.Home),
        BottomNavDestination("AI规划", AppRoute.Planner, Icons.Filled.TravelExplore),
        BottomNavDestination("收藏", AppRoute.Favorite, Icons.Filled.Favorite),
        BottomNavDestination("历史", AppRoute.History, Icons.Filled.History),
        BottomNavDestination("我的", AppRoute.Profile, Icons.Filled.AccountCircle)
    )
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    Surface(shadowElevation = 18.dp, tonalElevation = 0.dp) {
        NavigationBar(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            destinations.forEach { destination ->
                val selected = currentDestination.isRouteSelected(destination.route)
                val scale by animateFloatAsState(if (selected) 1.12f else 1f, label = "bottomBarScale")
                NavigationBarItem(
                    selected = selected,
                    onClick = { navController.navigateSingleTop(destination.route) },
                    icon = {
                        Icon(
                            destination.icon,
                            contentDescription = destination.label,
                            modifier = Modifier.scale(scale)
                        )
                    },
                    label = { Text(destination.label, maxLines = 1) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "buttonPress")

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale),
        interactionSource = interactionSource,
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp, pressedElevation = 1.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(TravelSpacing.small))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(TravelSpacing.small))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun TravelSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索目的地、景点、攻略"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        placeholder = { Text(placeholder) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun TravelTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = singleLine,
        shape = MaterialTheme.shapes.large,
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null) } },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    SkeletonLoading(modifier = modifier)
}

@Composable
fun SkeletonLoading(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(TravelSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)
    ) {
        repeat(4) { index ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(if (index == 0) 170.dp else 96.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun EmptyView(message: String, modifier: Modifier = Modifier, actionText: String = "去探索", onAction: () -> Unit = {}) {
    StateView(
        title = message,
        description = "新的目的地、攻略和灵感会在这里等你。",
        icon = Icons.Outlined.Image,
        actionText = actionText,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
fun ErrorView(message: String, modifier: Modifier = Modifier, onRetry: () -> Unit = {}) {
    StateView(
        title = "出了一点问题",
        description = message,
        icon = Icons.Filled.ErrorOutline,
        actionText = "重试",
        onAction = onRetry,
        modifier = modifier
    )
}

@Composable
fun CommonCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Box(Modifier.padding(TravelSpacing.medium)) { content() }
    }
}

@Composable
fun NetworkImage(url: String, contentDescription: String?, modifier: Modifier = Modifier, fallbackUrl: String = BannerImages.first()) {
    var useFallback by remember(url) { mutableStateOf(false) }
    val canUseFallback = fallbackUrl.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://") || url.isBlank())
    val model = if (useFallback && canUseFallback) fallbackUrl else url.ifBlank { fallbackUrl }
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        onError = { if (canUseFallback && model != fallbackUrl) useFallback = true }
    )
}

@Composable
fun Avatar(imageUrl: String, modifier: Modifier = Modifier, initials: String = "") {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.AccountCircle, contentDescription = "头像", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
        if (imageUrl.isNotBlank()) {
            NetworkImage(imageUrl, "头像", Modifier.fillMaxSize())
        } else {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Text(initials, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TravelBanner(
    imageUrl: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badge: String = "AI精选"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            NetworkImage(imageUrl, title, Modifier.fillMaxSize())
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.66f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(TravelSpacing.medium),
                verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)
            ) {
                Surface(color = TravelTeal.copy(alpha = 0.92f), shape = RoundedCornerShape(50)) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (action != null) {
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun ScenicCard(
    title: String,
    location: String,
    imageUrl: String,
    rating: String,
    modifier: Modifier = Modifier,
    fallbackImageUrl: String = BannerImages.first(),
    onClick: () -> Unit = {}
) {
    ElevatedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)
    ) {
        Column {
            NetworkImage(
                imageUrl,
                title,
                Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                fallbackImageUrl
            )
            Column(Modifier.padding(TravelSpacing.medium), verticalArrangement = Arrangement.spacedBy(TravelSpacing.extraSmall)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = TravelAmber)
                    Text(rating, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun TravelCard(
    title: String,
    subtitle: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {}
) {
    ElevatedCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(Modifier.padding(TravelSpacing.small), verticalAlignment = Alignment.CenterVertically) {
            NetworkImage(
                imageUrl,
                title,
                Modifier
                    .size(78.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(Modifier.width(TravelSpacing.medium))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TravelSpacing.extraSmall)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }
    }
}

@Composable
fun HotelCard(title: String, subtitle: String, imageUrl: String, price: String, modifier: Modifier = Modifier) {
    CommonCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NetworkImage(imageUrl, title, Modifier.size(88.dp).clip(MaterialTheme.shapes.medium))
            Spacer(Modifier.width(TravelSpacing.medium))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TravelSpacing.extraSmall)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(price, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun WeatherCard(
    city: String,
    date: String,
    weather: String,
    temperature: String,
    modifier: Modifier = Modifier,
    details: List<Pair<String, String>> = emptyList(),
    advice: String = "今天适合轻薄外套、防晒和舒适步行鞋。"
) {
    CommonCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(city, style = MaterialTheme.typography.titleLarge)
                    Text(date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(TravelSky.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Cloud, contentDescription = weather, tint = TravelSky, modifier = Modifier.size(36.dp))
                }
            }
            Text(temperature, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
            Text(weather, style = MaterialTheme.typography.titleMedium)
            if (details.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                    details.take(4).chunked(2).forEach { rowItems ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowItems.forEach { item ->
                                WeatherDetailTile(
                                    label = item.first,
                                    value = item.second,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            Surface(color = TravelTeal.copy(alpha = 0.12f), shape = MaterialTheme.shapes.large) {
                Row(Modifier.fillMaxWidth().padding(TravelSpacing.medium), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = TravelTeal)
                    Spacer(Modifier.width(TravelSpacing.small))
                    Text(advice, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WeatherDetailTile(label: String, value: String, modifier: Modifier = Modifier) {
    val accent = weatherDetailAccent(label, value)
    Surface(
        modifier = modifier.defaultMinSize(minHeight = 76.dp),
        shape = MaterialTheme.shapes.medium,
        color = accent.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun weatherDetailAccent(label: String, value: String): Color = when {
    label.contains("空气") && value.contains("优") -> TravelTeal
    label.contains("空气") && value.contains("良") -> TravelBlue
    label.contains("空气") && value.contains("轻度") -> TravelAmber
    label.contains("空气") && (value.contains("中度") || value.contains("重度") || value.contains("严重")) -> TravelCoral
    label.contains("空气") -> MaterialTheme.colorScheme.primary
    label.contains("湿度") -> TravelSky
    label.contains("风") -> TravelBlue
    label.contains("雨") -> TravelTeal
    else -> MaterialTheme.colorScheme.primary
}

@Composable
fun BudgetCard(title: String, value: String, modifier: Modifier = Modifier, color: Color = TravelBlue) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = color.copy(alpha = 0.12f)
    ) {
        Column(Modifier.padding(TravelSpacing.medium), verticalArrangement = Arrangement.spacedBy(TravelSpacing.extraSmall)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, color = color)
        }
    }
}

@Composable
fun InterestChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        modifier = modifier
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TravelTimeline(items: List<TimelineItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)) {
        items.forEachIndexed { index, item ->
            var expanded by remember { mutableStateOf(item.expanded) }
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(item.color.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(19.dp))
                    }
                    if (index != items.lastIndex) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(74.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                        )
                    }
                }
                Spacer(Modifier.width(TravelSpacing.medium))
                val cardModifier = Modifier.weight(1f).then(
                    if (item.onClick != null || item.onLongClick != null) {
                        Modifier.combinedClickable(
                            onClick = item.onClick ?: {},
                            onLongClick = item.onLongClick ?: {}
                        )
                    } else {
                        Modifier
                    }
                )
                CommonCard(cardModifier) {
                    Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.time, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                            }
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
                            }
                            IconButton(onClick = { item.onClick?.invoke() }, enabled = item.onClick != null) {
                                Icon(Icons.Filled.Edit, contentDescription = "编辑")
                            }
                        }
                        AnimatedVisibility(expanded) {
                            Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                                Text(item.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (item.imageUrl.isNotBlank()) {
                                    NetworkImage(
                                        item.imageUrl,
                                        item.title,
                                        Modifier
                                            .fillMaxWidth()
                                            .height(128.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StateView(
    title: String,
    description: String,
    icon: ImageVector,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(TravelSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(112.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), MaterialTheme.shapes.extraLarge),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(TravelSpacing.large))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(TravelSpacing.small))
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(TravelSpacing.large))
        PrimaryButton(text = actionText, onClick = onAction, modifier = Modifier.fillMaxWidth(0.72f))
    }
}

@Composable
fun LogoMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(76.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(TravelBlue, TravelTeal))),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.TravelExplore, contentDescription = "AI TravelMate", tint = Color.White, modifier = Modifier.size(38.dp))
    }
}

data class TimelineItem(
    val time: String,
    val title: String,
    val description: String,
    val icon: ImageVector = Icons.Filled.LocationOn,
    val color: Color = TravelBlue,
    val imageUrl: String = "",
    val expanded: Boolean = true,
    val onClick: (() -> Unit)? = null,
    val onLongClick: (() -> Unit)? = null
)

data class BottomNavDestination(val label: String, val route: AppRoute, val icon: ImageVector)

private fun NavDestination?.isRouteSelected(route: AppRoute): Boolean =
    this?.hierarchy?.any { it.route == route.route } == true

private fun NavHostController.navigateSingleTop(route: AppRoute) {
    navigate(route.route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}