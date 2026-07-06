package com.example.ai.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY updateTime DESC LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    fun observeByPhone(phone: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY updateTime DESC LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
    suspend fun getByPhoneAndPassword(phone: String, password: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("UPDATE users SET avatar = :avatar, updateTime = :updateTime WHERE id = :id")
    suspend fun updateAvatar(id: String, avatar: String, updateTime: Long)

    @Query("UPDATE users SET nickname = :nickname, updateTime = :updateTime WHERE id = :id")
    suspend fun updateNickname(id: String, nickname: String, updateTime: Long)
}

@Dao interface CityDao { @Query("SELECT * FROM cities") suspend fun getAll(): List<City> }
@Dao interface TravelPlanDao { @Query("SELECT * FROM travel_plans") suspend fun getAll(): List<TravelPlan> }
@Dao interface DayPlanDao { @Query("SELECT * FROM day_plans") suspend fun getAll(): List<DayPlan> }
@Dao interface ScenicDao { @Query("SELECT * FROM scenic_spots") suspend fun getAll(): List<Scenic> }
@Dao interface HotelDao { @Query("SELECT * FROM hotels") suspend fun getAll(): List<Hotel> }
@Dao interface WeatherDao { @Query("SELECT * FROM weather") suspend fun getAll(): List<Weather> }
@Dao interface FoodDao { @Query("SELECT * FROM foods") suspend fun getAll(): List<Food> }
@Dao interface BudgetDao { @Query("SELECT * FROM budgets") suspend fun getAll(): List<Budget> }
@Dao interface ChatMessageDao { @Query("SELECT * FROM chat_messages") suspend fun getAll(): List<ChatMessage> }
@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE userPhone = :userPhone AND targetType = :targetType ORDER BY createdAt DESC")
    fun observeByUserAndType(userPhone: String, targetType: String): Flow<List<Favorite>>

    @Query("SELECT * FROM favorites WHERE userPhone = :userPhone AND targetType = :targetType AND targetId = :targetId LIMIT 1")
    suspend fun getByUserTypeAndTarget(userPhone: String, targetType: String, targetId: String): Favorite?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteById(id: String)
}
@Dao
interface HistoryDao {
    @Query("SELECT * FROM history WHERE userPhone = :userPhone AND targetType = :targetType ORDER BY visitedAt DESC")
    fun observeByUserAndType(userPhone: String, targetType: String): Flow<List<History>>

    @Query("SELECT * FROM history WHERE id = :id AND userPhone = :userPhone AND targetType = :targetType LIMIT 1")
    suspend fun getByIdForUser(id: String, userPhone: String, targetType: String): History?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(history: History)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface CacheDao {
    @Query("SELECT * FROM cached_weather WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getWeather(cacheKey: String): CachedWeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeather(entity: CachedWeatherEntity)

    @Query("SELECT * FROM cached_poi WHERE cacheKey = :cacheKey AND category = :category")
    suspend fun getPois(cacheKey: String, category: String): List<CachedPoiEntity>

    @Query("DELETE FROM cached_poi WHERE cacheKey = :cacheKey AND category = :category")
    suspend fun deletePois(cacheKey: String, category: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPois(items: List<CachedPoiEntity>)

    @Query("SELECT * FROM cached_images WHERE keyword = :keyword LIMIT 1")
    suspend fun getImage(keyword: String): CachedImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImage(entity: CachedImageEntity)
}