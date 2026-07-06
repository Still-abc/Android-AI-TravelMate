package com.example.ai.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phone: String,
    val password: String = "",
    val nickname: String,
    val avatar: String = "",
    val gender: String = "保密",
    val birthday: String? = null,
    val signature: String? = null,
    val city: String? = null,
    val travelPreference: String = "",
    val createTime: Long,
    val updateTime: Long
)

@Entity(tableName = "cities")
data class City(@PrimaryKey val id: String, val name: String, val country: String)

@Entity(tableName = "travel_plans")
data class TravelPlan(@PrimaryKey val id: String, val title: String, val cityId: String, val days: Int)

@Entity(tableName = "day_plans")
data class DayPlan(@PrimaryKey val id: String, val planId: String, val dayIndex: Int, val summary: String)

@Entity(tableName = "scenic_spots")
data class Scenic(@PrimaryKey val id: String, val name: String, val cityId: String, val description: String)

@Entity(tableName = "hotels")
data class Hotel(@PrimaryKey val id: String, val name: String, val cityId: String, val address: String)

@Entity(tableName = "weather")
data class Weather(@PrimaryKey val id: String, val cityId: String, val summary: String, val temperatureCelsius: Int)

@Entity(tableName = "foods")
data class Food(@PrimaryKey val id: String, val name: String, val cityId: String, val description: String)

@Entity(tableName = "budgets")
data class Budget(@PrimaryKey val id: String, val planId: String, val amount: Double, val currency: String)

@Entity(tableName = "chat_messages")
data class ChatMessage(@PrimaryKey val id: String, val content: String, val fromUser: Boolean, val timestamp: Long)

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val id: String,
    val userPhone: String,
    val targetId: String,
    val targetType: String,
    val title: String,
    val subtitle: String = "",
    val imageUrl: String = "",
    val createdAt: Long
)

@Entity(tableName = "history")
data class History(
    @PrimaryKey val id: String,
    val userPhone: String,
    val targetId: String,
    val targetType: String,
    val title: String,
    val description: String = "",
    val days: Int = 0,
    val scheduleCount: Int = 0,
    val hotelCount: Int = 0,
    val foodCount: Int = 0,
    val planJson: String = "",
    val visitedAt: Long
)
