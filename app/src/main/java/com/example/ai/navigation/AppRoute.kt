package com.example.ai.navigation

import android.net.Uri

sealed class AppRoute(val route: String, val title: String, val showBottomBar: Boolean = false) {
    data object Splash : AppRoute("splash", "Splash")
    data object Login : AppRoute("login", "Login")
    data object Register : AppRoute("register", "Register")
    data object CompleteProfile : AppRoute("completeProfile/{phone}/{password}", "Complete Profile") {
        const val PHONE_ARG = "phone"
        const val PASSWORD_ARG = "password"
        const val ROUTE_WITH_ARGS = "completeProfile/{phone}/{password}"
        fun createRoute(phone: String, password: String): String = "completeProfile/${Uri.encode(phone)}/${Uri.encode(password)}"
    }
    data object Home : AppRoute("home", "Home", true)
    data object Planner : AppRoute("planner", "AI Planner", true)
    data object Itinerary : AppRoute("itinerary", "Itinerary")
    data object Scenic : AppRoute("scenic", "Scenic") {
        const val NAME_ARG = "name"
        const val ADDRESS_ARG = "address"
        const val TEL_ARG = "tel"
        const val LON_ARG = "lon"
        const val LAT_ARG = "lat"
        const val RATING_ARG = "rating"
        const val PHOTO_ARG = "photo"
        const val TYPE_ARG = "type"
        const val CATEGORY_ARG = "category"
        const val ROUTE_WITH_ARGS = "scenic?name={name}&address={address}&tel={tel}&lon={lon}&lat={lat}&rating={rating}&photo={photo}&type={type}&category={category}"

        fun createRoute(
            name: String,
            address: String,
            tel: String,
            lon: Double,
            lat: Double,
            rating: String,
            photo: String,
            type: String,
            category: String
        ): String = "scenic?name=${Uri.encode(name)}&address=${Uri.encode(address)}&tel=${Uri.encode(tel)}&lon=$lon&lat=$lat&rating=${Uri.encode(rating)}&photo=${Uri.encode(photo)}&type=${Uri.encode(type)}&category=${Uri.encode(category)}"
    }
    data object Weather : AppRoute("weather", "Weather") {
        const val CITY_ARG = "city"
        const val ROUTE_WITH_CITY = "weather?city={city}"

        fun createRoute(city: String?): String =
            city?.trim()?.takeIf { it.isNotBlank() }?.let { "weather?city=${Uri.encode(it)}" } ?: route
    }
    data object Map : AppRoute("map", "Map") {
        const val CITY_ARG = "city"
        const val ROUTE_WITH_CITY = "map?city={city}"

        fun createRoute(city: String?): String =
            city?.trim()?.takeIf { it.isNotBlank() }?.let { "map?city=${Uri.encode(it)}" } ?: route
    }
    data object Favorite : AppRoute("favorite", "Favorite", true)
    data object History : AppRoute("history", "History", true)
    data object Profile : AppRoute("profile", "Profile", true)
    data object EditProfile : AppRoute("editProfile", "Edit Profile")
    data object Settings : AppRoute("settings", "Settings")
    data object Chat : AppRoute("chat", "AI Chat")
}

val allRoutes = listOf(
    AppRoute.Splash,
    AppRoute.Login,
    AppRoute.Register,
    AppRoute.CompleteProfile,
    AppRoute.Home,
    AppRoute.Planner,
    AppRoute.Itinerary,
    AppRoute.Scenic,
    AppRoute.Weather,
    AppRoute.Map,
    AppRoute.Favorite,
    AppRoute.History,
    AppRoute.Profile,
    AppRoute.EditProfile,
    AppRoute.Settings,
    AppRoute.Chat
)