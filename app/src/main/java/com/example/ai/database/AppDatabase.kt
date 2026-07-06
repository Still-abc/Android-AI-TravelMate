package com.example.ai.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ai.database.CachedImageEntity
import com.example.ai.database.CachedPoiEntity
import com.example.ai.database.CachedWeatherEntity
import com.example.ai.model.Budget
import com.example.ai.model.ChatMessage
import com.example.ai.model.City
import com.example.ai.model.DayPlan
import com.example.ai.model.Favorite
import com.example.ai.model.Food
import com.example.ai.model.History
import com.example.ai.model.Hotel
import com.example.ai.model.Scenic
import com.example.ai.model.TravelPlan
import com.example.ai.model.UserEntity
import com.example.ai.model.Weather

@Database(
    entities = [
        UserEntity::class,
        City::class,
        TravelPlan::class,
        DayPlan::class,
        Scenic::class,
        Hotel::class,
        Weather::class,
        Food::class,
        Budget::class,
        ChatMessage::class,
        Favorite::class,
        History::class,
        CachedWeatherEntity::class,
        CachedPoiEntity::class,
        CachedImageEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cityDao(): CityDao
    abstract fun travelPlanDao(): TravelPlanDao
    abstract fun dayPlanDao(): DayPlanDao
    abstract fun scenicDao(): ScenicDao
    abstract fun hotelDao(): HotelDao
    abstract fun weatherDao(): WeatherDao
    abstract fun foodDao(): FoodDao
    abstract fun budgetDao(): BudgetDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao
    abstract fun cacheDao(): CacheDao
}
