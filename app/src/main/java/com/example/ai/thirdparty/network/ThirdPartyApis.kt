package com.example.ai.thirdparty.network

import com.example.ai.thirdparty.model.AMapGeocodeResponse
import com.example.ai.thirdparty.model.AMapWeatherResponse
import com.example.ai.thirdparty.model.AMapPoiResponse
import com.example.ai.thirdparty.model.AMapRegeocodeResponse
import com.example.ai.thirdparty.model.PexelsImageResponse
import com.example.ai.thirdparty.model.QWeatherAirQualityResponse
import com.example.ai.thirdparty.model.QWeatherForecastResponse
import com.example.ai.thirdparty.model.QWeatherLocationResponse
import com.example.ai.thirdparty.model.QWeatherNowResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {
    @GET("geo/v2/city/lookup")
    suspend fun lookupCity(
        @Query("location") location: String,
        @Query("key") key: String
    ): QWeatherLocationResponse

    @GET("v7/weather/now")
    suspend fun now(
        @Query("location") locationId: String,
        @Query("key") key: String
    ): QWeatherNowResponse

    @GET("v7/weather/3d")
    suspend fun forecast(
        @Query("location") locationId: String,
        @Query("key") key: String
    ): QWeatherForecastResponse

    @GET("airquality/v1/current/{latitude}/{longitude}")
    suspend fun airQuality(
        @Path("latitude") latitude: String,
        @Path("longitude") longitude: String,
        @Query("key") key: String
    ): QWeatherAirQualityResponse
}

interface MapApi {
    @GET("v3/place/text")
    suspend fun searchPoi(
        @Query("key") key: String,
        @Query("keywords") keywords: String,
        @Query("city") city: String,
        @Query("types") types: String = "",
        @Query("offset") offset: Int = 10,
        @Query("page") page: Int = 1,
        @Query("extensions") extensions: String = "all"
    ): AMapPoiResponse

    @GET("v3/geocode/geo")
    suspend fun geocode(
        @Query("key") key: String,
        @Query("address") address: String,
        @Query("city") city: String = ""
    ): AMapGeocodeResponse

    @GET("v3/geocode/regeo")
    suspend fun reverseGeocode(
        @Query("key") key: String,
        @Query("location") location: String,
        @Query("extensions") extensions: String = "base"
    ): AMapRegeocodeResponse

    @GET("v3/weather/weatherInfo")
    suspend fun weather(
        @Query("key") key: String,
        @Query("city") city: String,
        @Query("extensions") extensions: String = "base"
    ): AMapWeatherResponse
}

interface ImageApi {
    @GET("v1/search")
    suspend fun searchImages(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 5,
        @Query("orientation") orientation: String = "landscape",
        @Query("locale") locale: String = "zh-CN"
    ): PexelsImageResponse
}
