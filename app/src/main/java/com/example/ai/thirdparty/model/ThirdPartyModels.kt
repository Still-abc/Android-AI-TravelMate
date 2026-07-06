package com.example.ai.thirdparty.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QWeatherLocationResponse(
    val code: String = "",
    val location: List<QWeatherLocation> = emptyList()
)

@Serializable
data class QWeatherLocation(
    val name: String = "",
    val id: String = "",
    val lat: String = "",
    val lon: String = "",
    val adm1: String = "",
    val adm2: String = ""
)

@Serializable
data class QWeatherNowResponse(
    val code: String = "",
    val now: QWeatherNow? = null
)

@Serializable
data class QWeatherNow(
    val obsTime: String = "",
    val temp: String = "",
    val feelsLike: String = "",
    val icon: String = "",
    val text: String = "",
    val windDir: String = "",
    val windScale: String = "",
    val windSpeed: String = "",
    val humidity: String = "",
    val precip: String = ""
)

@Serializable
data class QWeatherForecastResponse(
    val code: String = "",
    val daily: List<QWeatherForecastDay> = emptyList()
)

@Serializable
data class QWeatherForecastDay(
    val fxDate: String = "",
    val tempMax: String = "",
    val tempMin: String = "",
    val iconDay: String = "",
    val textDay: String = "",
    val humidity: String = "",
    val precip: String = ""
)

@Serializable
data class QWeatherAirQualityResponse(
    val indexes: List<QWeatherAirQualityIndex> = emptyList()
)

@Serializable
data class QWeatherAirQualityIndex(
    val code: String = "",
    val name: String = "",
    val aqi: Int = 0,
    val aqiDisplay: String = "",
    val level: String = "",
    val category: String = ""
)

@Serializable
data class AMapPoiResponse(
    val status: String = "",
    val info: String = "",
    val pois: List<AMapPoi> = emptyList()
)

@Serializable
data class AMapPoi(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val address: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val location: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val tel: String = "",
    @SerialName("biz_ext") val bizExt: AMapBizExt? = null,
    val photos: List<AMapPoiPhoto> = emptyList()
)

@Serializable
data class AMapBizExt(
    @Serializable(with = FlexibleStringSerializer::class) val rating: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val cost: String = ""
)

@Serializable
data class AMapPoiPhoto(
    @Serializable(with = FlexibleStringSerializer::class) val title: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val url: String = ""
)

@Serializable
data class AMapGeocodeResponse(
    val status: String = "",
    val info: String = "",
    val geocodes: List<AMapGeocode> = emptyList()
)

@Serializable
data class AMapGeocode(
    @Serializable(with = FlexibleStringSerializer::class) val formatted_address: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val location: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val adcode: String = ""
)

@Serializable
data class AMapRegeocodeResponse(
    val status: String = "",
    val info: String = "",
    val regeocode: AMapRegeocode? = null
)

@Serializable
data class AMapRegeocode(
    @SerialName("formatted_address") val formattedAddress: String = "",
    @SerialName("addressComponent") val addressComponent: AMapAddressComponent? = null
)

@Serializable
data class AMapAddressComponent(
    @Serializable(with = FlexibleStringSerializer::class) val province: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val city: String = "",
    @Serializable(with = FlexibleStringSerializer::class) val district: String = ""
)

@Serializable
data class AMapWeatherResponse(
    val status: String = "",
    val info: String = "",
    val lives: List<AMapLiveWeather> = emptyList()
)

@Serializable
data class AMapLiveWeather(
    val province: String = "",
    val city: String = "",
    val adcode: String = "",
    val weather: String = "",
    val temperature: String = "",
    val winddirection: String = "",
    val windpower: String = "",
    val humidity: String = "",
    val reporttime: String = ""
)

@Serializable
data class PexelsImageResponse(
    val photos: List<PexelsPhoto> = emptyList()
)

@Serializable
data class PexelsPhoto(
    val id: Long = 0,
    val photographer: String = "",
    val src: PexelsPhotoSrc = PexelsPhotoSrc()
)

@Serializable
data class PexelsPhotoSrc(
    val original: String = "",
    val large2x: String = "",
    val large: String = "",
    val medium: String = ""
)

data class WeatherForecast(
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
    val icon: String
)

data class Location(
    val longitude: Double,
    val latitude: Double
)

data class Poi(
    val id: String,
    val name: String,
    val type: String,
    val address: String,
    val telephone: String,
    val location: Location,
    val rating: String,
    val photoUrl: String = ""
)

data class PlaceDetail(
    val name: String,
    val address: String,
    val telephone: String,
    val location: Location,
    val rating: String
)

data class Photo(
    val keyword: String,
    val url: String,
    val photographer: String
)
