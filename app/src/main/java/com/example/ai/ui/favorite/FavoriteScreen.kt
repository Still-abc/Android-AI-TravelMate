package com.example.ai.ui.favorite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.ai.component.CommonCard
import com.example.ai.component.ScenicCard
import com.example.ai.component.SectionHeader
import com.example.ai.model.Favorite
import com.example.ai.navigation.AppRoute
import com.example.ai.repository.FavoriteRepository
import com.example.ai.theme.TravelSpacing
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    repository: FavoriteRepository
) : ViewModel() {
    val cityFavorites: StateFlow<List<Favorite>> = repository.observeCityFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun FavoriteScreen(navController: NavHostController, viewModel: FavoriteViewModel = hiltViewModel()) {
    val cityFavorites by viewModel.cityFavorites.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(TravelSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
    ) {
        item { Spacer(Modifier.height(TravelSpacing.small)) }
        item {
            Text("我的收藏", style = MaterialTheme.typography.headlineMedium)
            Text("这里只展示你主动收藏的城市。", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { SectionHeader("收藏城市") }
        if (cityFavorites.isEmpty()) {
            item {
                CommonCard {
                    Text("暂无收藏城市", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(cityFavorites, key = { it.id }) { favorite ->
                ScenicCard(
                    title = favorite.title,
                    location = favorite.subtitle.ifBlank { "城市收藏" },
                    imageUrl = favorite.imageUrl,
                    rating = "已收藏",
                    onClick = { navController.navigate(AppRoute.Map.createRoute(favorite.title)) }
                )
            }
        }
        item { Spacer(Modifier.height(TravelSpacing.large)) }
    }
}
