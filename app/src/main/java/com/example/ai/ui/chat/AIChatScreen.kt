package com.example.ai.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ai.ai.model.ChatMessage
import com.example.ai.ai.viewmodel.AIViewModel
import com.example.ai.component.Avatar
import com.example.ai.component.TravelTextField
import com.example.ai.component.TravelTopBar
import com.example.ai.repository.AIChatRepository
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class AIChatUiState(val title: String = "AI Chat")

@HiltViewModel
class AIChatViewModel @Inject constructor(private val repository: AIChatRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState
}

@Composable
fun AIChatScreen(
    navController: NavHostController,
    viewModel: AIChatViewModel = hiltViewModel(),
    aiViewModel: AIViewModel = hiltViewModel()
) {
    viewModel.uiState.collectAsStateWithLifecycle().value
    val aiState by aiViewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }
    val messages = aiState.currentConversation?.messages.orEmpty()

    LaunchedEffect(messages.size, aiState.loading) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).imePadding()) {
        TravelTopBar(
            title = "AI Travel Assistant",
            canNavigateBack = true,
            onNavigateBack = { navController.navigateUp() },
            actionIcon = Icons.Filled.Refresh,
            onActionClick = { aiViewModel.regenerateLastResponse() }
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = TravelSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(TravelSpacing.medium)
        ) {
            item { Spacer(Modifier.height(TravelSpacing.small)) }
            if (messages.isEmpty()) item { AssistantIntro(aiState.hasValidConfig) }
            itemsIndexed(messages) { index, message -> ChatBubble(message, index, aiViewModel) }
            if (aiState.loading) item { TypingBubble() }
            if (!aiState.errorMessage.isNullOrBlank()) {
                item { Text(aiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            }
            item { Spacer(Modifier.height(TravelSpacing.small)) }
        }
        Surface(shadowElevation = 16.dp, color = MaterialTheme.colorScheme.surface) {
            Row(Modifier.fillMaxWidth().padding(TravelSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
                TravelTextField(input, { input = it }, "Ask AI TravelMate", modifier = Modifier.weight(1f), singleLine = true)
                IconButton(
                    onClick = {
                        aiViewModel.sendMessage(input)
                        input = ""
                    },
                    modifier = Modifier.padding(start = TravelSpacing.small),
                    enabled = !aiState.loading
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun AssistantIntro(hasValidConfig: Boolean) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large, shadowElevation = 4.dp) {
        Row(Modifier.fillMaxWidth().padding(TravelSpacing.medium), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(TravelTeal.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = TravelTeal)
            }
            Column(Modifier.padding(start = TravelSpacing.medium)) {
                Text("AI TravelMate", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (hasValidConfig) "Ask for routes, budget analysis, weather advice, or guide summaries."
                    else "Please configure AI values in ApiConfig.kt before sending requests.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, index: Int, aiViewModel: AIViewModel) {
    val context = LocalContext.current
    val mine = message.role == "user"
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!mine) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(TravelTeal.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = TravelTeal, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.padding(TravelSpacing.extraSmall))
        }
        Box {
            Surface(
                color = if (mine) TravelBlue else MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                shadowElevation = if (mine) 0.dp else 4.dp,
                modifier = Modifier.fillMaxWidth(0.78f).combinedClickable(onClick = {}, onLongClick = { menuExpanded = true })
            ) {
                Column(Modifier.padding(TravelSpacing.medium), verticalArrangement = Arrangement.spacedBy(TravelSpacing.extraSmall)) {
                    Text(message.content, color = if (mine) Color.White else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Start)
                    Text(if (mine) "You" else "AI", color = if (mine) Color.White.copy(alpha = 0.72f) else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Copy") },
                    leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                    onClick = {
                        context.copyToClipboard(message.content)
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                    onClick = {
                        aiViewModel.deleteChatMessage(index)
                        menuExpanded = false
                    }
                )
            }
        }
        if (mine) {
            Spacer(Modifier.padding(TravelSpacing.extraSmall))
            Avatar("", Modifier.size(36.dp), "Me")
        }
    }
}

@Composable
private fun TypingBubble() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(CircleShape).background(TravelTeal.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = TravelTeal, modifier = Modifier.size(20.dp))
        }
        Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large, shadowElevation = 4.dp, modifier = Modifier.padding(start = TravelSpacing.small)) {
            Text("AI is typing...", Modifier.padding(TravelSpacing.medium), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("AI TravelMate", text))
}
