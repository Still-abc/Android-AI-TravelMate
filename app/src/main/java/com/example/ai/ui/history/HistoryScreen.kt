package com.example.ai.ui.history

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
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.ai.navigation.AppRoute
import com.example.ai.ai.repository.GeneratedPlanHistory
import com.example.ai.ai.repository.GeneratedPlanRepository
import com.example.ai.component.CommonCard
import com.example.ai.component.SectionHeader
import com.example.ai.component.TimelineItem
import com.example.ai.component.TravelTimeline
import com.example.ai.repository.HistoryRepository
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelSpacing
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val generatedPlanRepository: GeneratedPlanRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {
    val generatedPlans: StateFlow<List<GeneratedPlanHistory>> = generatedPlanRepository.observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun openPlan(id: String, onOpened: () -> Unit) {
        viewModelScope.launch {
            if (generatedPlanRepository.openHistoryPlan(id)) onOpened()
        }
    }

    fun deletePlan(id: String) {
        viewModelScope.launch {
            historyRepository.deletePlanHistory(id)
        }
    }
}

@Composable
fun HistoryScreen(navController: NavHostController, viewModel: HistoryViewModel = hiltViewModel()) {
    val generatedPlans by viewModel.generatedPlans.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var planToDelete by remember { mutableStateOf<String?>(null) }

    val timelineItems = generatedPlans.map { history ->
        history.toTimelineItem(
            onOpen = { viewModel.openPlan(history.id) { navController.navigate(AppRoute.Itinerary.route) } },
            onLongPress = {
                planToDelete = history.id
                showDeleteDialog = true
            }
        )
    }

    if (showDeleteDialog && planToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; planToDelete = null },
            title = { Text("删除记录") },
            text = { Text("确定要删除这条旅行历史记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    planToDelete?.let { viewModel.deletePlan(it) }
                    showDeleteDialog = false
                    planToDelete = null
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; planToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(TravelSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(TravelSpacing.large)
    ) {
        Spacer(Modifier.height(TravelSpacing.small))
        Text("旅行历史", style = MaterialTheme.typography.headlineMedium)
        Text("长按记录可删除，点击可查看完整规划。", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionHeader("最近生成")
        if (timelineItems.isEmpty()) {
            CommonCard {
                Text("暂无旅行历史", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            TravelTimeline(timelineItems)
        }
        Spacer(Modifier.height(TravelSpacing.large))
    }
}

private fun GeneratedPlanHistory.toTimelineItem(onOpen: () -> Unit, onLongPress: () -> Unit): TimelineItem = TimelineItem(
    time = generatedAt.historyTimeText(),
    title = "生成${destination} ${days}日规划",
    description = "$title · 包含 ${scheduleCount} 个行程、${hotelCount} 家酒店和 ${foodCount} 条美食建议。",
    icon = Icons.Filled.TravelExplore,
    color = TravelBlue,
    expanded = true,
    onClick = if (hasFullPlan) onOpen else null,
    onLongClick = onLongPress
)

private fun Long.historyTimeText(): String = SimpleDateFormat("今天 HH:mm", Locale.CHINA).format(Date(this))