package com.example.ai.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.ai.component.EmptyView

@Composable
fun PlaceholderScreenContent(
    title: String,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    EmptyView(message = title, modifier = modifier)
}
