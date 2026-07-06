package com.example.ai.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val cacheKey: String,
    val city: String,
    val date: String,
    val weather: String,
    val temperature: String,
    val tempMax: String,
    val tempMin: String,
    val humidity: String,
    val wind: String,
    val airQuality: String,
    val precipitation: String,
    val icon: String,
    val cachedAt: Long
)

@Entity(tableName = "cached_poi")
data class CachedPoiEntity(
    @PrimaryKey val id: String,
    val cacheKey: String,
    val category: String,
    val city: String,
    val name: String,
    val type: String,
    val address: String,
    val telephone: String,
    val longitude: Double,
    val latitude: Double,
    val rating: String,
    val photoUrl: String,
    val cachedAt: Long
)

@Entity(tableName = "cached_images")
data class CachedImageEntity(
    @PrimaryKey val keyword: String,
    val imageUrl: String,
    val photographer: String,
    val cachedAt: Long
)
