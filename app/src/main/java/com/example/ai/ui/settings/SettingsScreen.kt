package com.example.ai.ui.settings

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
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ai.component.CommonCard
import com.example.ai.component.TravelTopBar
import com.example.ai.repository.SettingsRepository
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelCoral
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsUiState(val title: String = "Settings")

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState
}

@Composable
fun SettingsScreen(navController: NavHostController, viewModel: SettingsViewModel = hiltViewModel()) {
    viewModel.uiState.collectAsStateWithLifecycle().value
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TravelTopBar("Settings", true, { navController.navigateUp() })
        Column(Modifier.verticalScroll(rememberScrollState()).padding(TravelSpacing.medium), verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)) {
            CommonCard {
                Column(verticalArrangement = Arrangement.spacedBy(TravelSpacing.small)) {
                    SettingRow(Icons.Filled.DarkMode, "Dark Mode", "Follow system or switch manually", TravelBlue) { Switch(darkMode, { darkMode = it }) }
                    SettingRow(Icons.Filled.Notifications, "Notifications", "Trip reminders and weather alerts", TravelTeal) { Switch(notifications, { notifications = it }) }
                    SettingRow(Icons.Filled.Cached, "Cache", "Images and offline data 86MB", TravelCoral) { Text("Clear", color = MaterialTheme.colorScheme.primary) }
                    SettingRow(Icons.Filled.Language, "Language", "Simplified Chinese", TravelBlue) { Text("Switch", color = MaterialTheme.colorScheme.primary) }
                    SettingRow(Icons.Filled.Info, "About", "AI TravelMate", TravelTeal) { Text("View", color = MaterialTheme.colorScheme.primary) }
                    SettingRow(Icons.Filled.Feedback, "Feedback", "Help us improve", TravelCoral) { Text("Send", color = MaterialTheme.colorScheme.primary) }
                }
            }
            Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = TravelSpacing.medium))
            Spacer(Modifier.height(TravelSpacing.large))
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String, color: Color, trailing: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = TravelSpacing.small), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).background(color.copy(alpha = 0.12f), MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = color)
        }
        Column(Modifier.weight(1f).padding(horizontal = TravelSpacing.medium)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing()
    }
}
