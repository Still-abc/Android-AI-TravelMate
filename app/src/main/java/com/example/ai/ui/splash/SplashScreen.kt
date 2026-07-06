package com.example.ai.ui.splash

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.example.ai.component.LogoMark
import com.example.ai.navigation.AppRoute
import com.example.ai.repository.SplashRepository
import com.example.ai.theme.TravelBlue
import com.example.ai.theme.TravelSpacing
import com.example.ai.theme.TravelTeal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: SplashRepository
) : ViewModel() {
    suspend fun startDestination(): String {
        val rememberedPhone = repository.rememberedDestination()
        return if (rememberedPhone.isNullOrBlank()) {
            repository.clearTransientLoginWhenNotRemembered()
            AppRoute.Login.route
        } else {
            AppRoute.Home.route
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController, viewModel: SplashViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        delay(1200)
        val destination = viewModel.startDestination()
        navController.navigate(destination) {
            popUpTo(AppRoute.Splash.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    val transition = rememberInfiniteTransition(label = "splashLoading")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(720), RepeatMode.Reverse),
        label = "loadingAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(TravelSpacing.large)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoMark(Modifier.size(96.dp))
            Spacer(Modifier.height(TravelSpacing.large))
            Text("AI TravelMate", style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(TravelSpacing.small))
            Text(
                "Explore The World With AI",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(TravelSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == 1) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == 1) TravelBlue.copy(alpha = alpha) else TravelTeal.copy(alpha = alpha * 0.78f)
                        )
                )
            }
            Text("Preparing smart journey", style = MaterialTheme.typography.bodyMedium, color = Color.Gray.copy(alpha = alpha))
        }
    }
}