package com.example.ai.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ai.component.TravelBottomBar
import com.example.ai.ui.chat.AIChatScreen
import com.example.ai.ui.favorite.FavoriteScreen
import com.example.ai.ui.history.HistoryScreen
import com.example.ai.ui.home.HomeScreen
import com.example.ai.ui.itinerary.ItineraryScreen
import com.example.ai.ui.login.LoginScreen
import com.example.ai.ui.login.RegisterScreen
import com.example.ai.ui.map.MapScreen
import com.example.ai.ui.planner.PlannerScreen
import com.example.ai.ui.profile.ProfileEditScreen
import com.example.ai.ui.profile.ProfileScreen
import com.example.ai.ui.profile.ProfileSetupScreen
import com.example.ai.ui.scenic.ScenicDetailScreen
import com.example.ai.ui.settings.SettingsScreen
import com.example.ai.ui.splash.SplashScreen
import com.example.ai.ui.weather.WeatherScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destinationRoute = backStackEntry?.destination?.route
    val currentRoute = allRoutes.firstOrNull { route ->
        destinationRoute == route.route || destinationRoute?.startsWith("${route.route}?") == true ||
            destinationRoute?.substringBefore("/") == route.route.substringBefore("/")
    } ?: AppRoute.Splash

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute.showBottomBar) {
                TravelBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Splash.route,
            modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            enterTransition = {
                fadeIn(tween(240)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(240)
                )
            },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = {
                fadeIn(tween(240)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(240)
                )
            },
            popExitTransition = { fadeOut(tween(180)) }
        ) {
            composable(AppRoute.Splash.route) { SplashScreen(navController) }
            composable(AppRoute.Login.route) { LoginScreen(navController) }
            composable(AppRoute.Register.route) { RegisterScreen(navController) }
            composable(
                route = AppRoute.CompleteProfile.ROUTE_WITH_ARGS,
                arguments = listOf(
                    navArgument(AppRoute.CompleteProfile.PHONE_ARG) { type = NavType.StringType },
                    navArgument(AppRoute.CompleteProfile.PASSWORD_ARG) { type = NavType.StringType }
                )
            ) { entry ->
                ProfileSetupScreen(
                    navController = navController,
                    phone = entry.arguments?.getString(AppRoute.CompleteProfile.PHONE_ARG).orEmpty(),
                    password = entry.arguments?.getString(AppRoute.CompleteProfile.PASSWORD_ARG).orEmpty()
                )
            }
            composable(AppRoute.Home.route) { HomeScreen(navController) }
            composable(AppRoute.Planner.route) { PlannerScreen(navController) }
            composable(AppRoute.Itinerary.route) { ItineraryScreen(navController) }
            composable(
                route = AppRoute.Scenic.ROUTE_WITH_ARGS,
                arguments = listOf(
                    navArgument(AppRoute.Scenic.NAME_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument(AppRoute.Scenic.ADDRESS_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument(AppRoute.Scenic.TEL_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument(AppRoute.Scenic.LON_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument(AppRoute.Scenic.LAT_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument(AppRoute.Scenic.RATING_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument(AppRoute.Scenic.PHOTO_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument(AppRoute.Scenic.TYPE_ARG) { type = NavType.StringType; nullable = true; defaultValue = null },
                    navArgument(AppRoute.Scenic.CATEGORY_ARG) { type = NavType.StringType; nullable = true; defaultValue = null }
                )
            ) { entry ->
                ScenicDetailScreen(
                    navController = navController,
                    name = entry.arguments?.getString(AppRoute.Scenic.NAME_ARG),
                    address = entry.arguments?.getString(AppRoute.Scenic.ADDRESS_ARG),
                    telephone = entry.arguments?.getString(AppRoute.Scenic.TEL_ARG),
                    longitude = entry.arguments?.getString(AppRoute.Scenic.LON_ARG)?.toDoubleOrNull(),
                    latitude = entry.arguments?.getString(AppRoute.Scenic.LAT_ARG)?.toDoubleOrNull(),
                    rating = entry.arguments?.getString(AppRoute.Scenic.RATING_ARG),
                    photoUrl = entry.arguments?.getString(AppRoute.Scenic.PHOTO_ARG),
                    poiType = entry.arguments?.getString(AppRoute.Scenic.TYPE_ARG),
                    category = entry.arguments?.getString(AppRoute.Scenic.CATEGORY_ARG)
                )
            }
            composable(
                route = AppRoute.Weather.ROUTE_WITH_CITY,
                arguments = listOf(navArgument(AppRoute.Weather.CITY_ARG) { defaultValue = ""; nullable = true })
            ) { WeatherScreen(navController) }
            composable(
                route = AppRoute.Map.ROUTE_WITH_CITY,
                arguments = listOf(
                    navArgument(AppRoute.Map.CITY_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                MapScreen(navController, initialCity = entry.arguments?.getString(AppRoute.Map.CITY_ARG))
            }
            composable(AppRoute.Favorite.route) { FavoriteScreen(navController) }
            composable(AppRoute.History.route) { HistoryScreen(navController) }
            composable(AppRoute.Profile.route) { ProfileScreen(navController) }
            composable(AppRoute.EditProfile.route) { ProfileEditScreen(navController) }
            composable(AppRoute.Settings.route) { SettingsScreen(navController) }
            composable(AppRoute.Chat.route) { AIChatScreen(navController) }
        }
    }
}