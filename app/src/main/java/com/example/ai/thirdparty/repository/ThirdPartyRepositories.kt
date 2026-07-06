package com.example.ai.thirdparty.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location as AndroidLocation
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.ai.common.AppResult
import com.example.ai.config.ApiConfig
import com.example.ai.database.CacheDao
import com.example.ai.database.CachedImageEntity
import com.example.ai.database.CachedPoiEntity
import com.example.ai.database.CachedWeatherEntity
import com.example.ai.thirdparty.model.AMapPoi
import com.example.ai.thirdparty.model.Location
import com.example.ai.thirdparty.model.Photo
import com.example.ai.thirdparty.model.Poi
import com.example.ai.thirdparty.model.WeatherForecast
import com.example.ai.thirdparty.network.ThirdPartyApiException
import com.example.ai.thirdparty.network.ThirdPartyClientFactory
import com.example.ai.thirdparty.network.ThirdPartyErrorMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

private const val WEATHER_CACHE_MS = 30L * 60L * 1000L
private const val POI_CACHE_MS = 24L * 60L * 60L * 1000L
private const val IMAGE_CACHE_MS = 24L * 60L * 60L * 1000L

@Singleton
class WeatherRepository @Inject constructor(
    private val clientFactory: ThirdPartyClientFactory,
    private val cacheDao: CacheDao
) {
    suspend fun getWeather(city: String, date: String = "today"): AppResult<WeatherForecast> = runCatching {
        if (!ApiConfig.hasWeatherConfig()) throw ThirdPartyApiException("请先配置天气API Key")
        val cacheKey = "${city.trim()}-$date"
        cacheDao.getWeather(cacheKey)
            ?.takeIf { it.isFresh(WEATHER_CACHE_MS) && !it.airQuality.contains("暂未接入") }
            ?.toWeatherForecast()
            ?.let { return AppResult.Success(it) }

        val forecast = runCatching { fetchQWeather(city, date) }
            .getOrElse { fetchAmapWeather(city, date) }
        cacheDao.upsertWeather(forecast.toEntity(cacheKey))
        AppResult.Success(forecast)
    }.getOrElse { AppResult.Error(ThirdPartyErrorMapper.toChineseMessage(it), it) }

    private suspend fun resolveWeatherLocation(city: String): String {
        if (ApiConfig.hasAmapConfig()) {
            val geocode = clientFactory.mapApi().geocode(ApiConfig.AMAP_WEB_KEY, city, city)
            val location = geocode.geocodes.firstOrNull()?.location.orEmpty()
            if (location.isNotBlank()) return location
        }
        return city
    }

    private suspend fun fetchQWeather(city: String, date: String): WeatherForecast {
        val location = resolveWeatherLocation(city)
        val nowResponse = clientFactory.weatherApi().now(location, ApiConfig.WEATHER_API_KEY)
        if (nowResponse.code != "200") throw ThirdPartyApiException("天气API返回异常：${nowResponse.code}")
        val now = nowResponse.now ?: throw ThirdPartyApiException("天气API未返回实时天气")
        val daily = runCatching { clientFactory.weatherApi().forecast(location, ApiConfig.WEATHER_API_KEY).daily.firstOrNull() }.getOrNull()
        val airQuality = runCatching { fetchQWeatherAirQuality(location) }.getOrDefault("暂无")
        return WeatherForecast(
            city = city,
            date = daily?.fxDate?.ifBlank { date } ?: date,
            weather = now.text,
            temperature = "${now.temp}℃",
            tempMax = daily?.tempMax?.let { "${it}℃" }.orEmpty(),
            tempMin = daily?.tempMin?.let { "${it}℃" }.orEmpty(),
            humidity = "${now.humidity}%",
            wind = "${now.windDir} ${now.windScale}级",
            airQuality = airQuality,
            precipitation = if (now.precip.isBlank()) daily?.precip.orEmpty() else "${now.precip}mm",
            icon = now.icon
        )
    }

    private suspend fun fetchQWeatherAirQuality(location: String): String {
        val coordinates = location.toLocation().takeIf { it.latitude != 0.0 && it.longitude != 0.0 }
            ?: throw ThirdPartyApiException("空气质量API需要经纬度")
        val response = clientFactory.weatherApi().airQuality(
            latitude = coordinates.latitude.toString(),
            longitude = coordinates.longitude.toString(),
            key = ApiConfig.WEATHER_API_KEY
        )
        val air = response.indexes.firstOrNull { it.code == "cn-mee" }
            ?: response.indexes.firstOrNull()
            ?: throw ThirdPartyApiException("空气质量API未返回实时数据")
        return buildString {
            append(air.category.ifBlank { "AQI" })
            val aqi = air.aqiDisplay.ifBlank { air.aqi.takeIf { it > 0 }?.toString().orEmpty() }
            if (aqi.isNotBlank()) append(" $aqi")
        }.ifBlank { "暂无" }
    }

    private suspend fun fetchAmapWeather(city: String, date: String): WeatherForecast {
        if (!ApiConfig.hasAmapConfig()) throw ThirdPartyApiException("天气API不可用，且未配置高德Web API Key")
        val geocode = clientFactory.mapApi().geocode(ApiConfig.AMAP_WEB_KEY, city, city)
        val adcode = geocode.geocodes.firstOrNull()?.adcode.orEmpty().ifBlank { city }
        val response = clientFactory.mapApi().weather(ApiConfig.AMAP_WEB_KEY, adcode)
        if (response.status != "1") throw ThirdPartyApiException("高德天气返回异常：${response.info}")
        val live = response.lives.firstOrNull() ?: throw ThirdPartyApiException("高德天气未返回实时天气")
        return WeatherForecast(
            city = live.city.ifBlank { city },
            date = live.reporttime.ifBlank { date },
            weather = live.weather,
            temperature = "${live.temperature}℃",
            tempMax = "--",
            tempMin = "--",
            humidity = "${live.humidity}%",
            wind = "${live.winddirection} ${live.windpower}级",
            airQuality = "暂未接入",
            precipitation = "暂无",
            icon = ""
        )
    }
}

@Singleton
class MapRepository @Inject constructor(
    private val clientFactory: ThirdPartyClientFactory,
    private val cacheDao: CacheDao
) {
    suspend fun searchScenic(city: String): AppResult<List<Poi>> = searchPoi(city, "景点", "110000")
    suspend fun searchFood(city: String): AppResult<List<Poi>> = searchPoi(city, "美食", "050000")
    suspend fun searchHotels(city: String): AppResult<List<Poi>> = searchPoi(city, "酒店", "100000")

    suspend fun searchPoi(city: String, keyword: String, types: String = ""): AppResult<List<Poi>> = runCatching {
        if (!ApiConfig.hasAmapConfig()) throw ThirdPartyApiException("请先配置高德Web API Key")
        val category = if (types.isBlank()) keyword else types
        val cacheKey = city.trim()
        val cached = cacheDao.getPois(cacheKey, category).takeIf { items -> items.isNotEmpty() && items.all { it.isFresh(POI_CACHE_MS) } }
        if (cached != null) return AppResult.Success(cached.map { it.toPoi() })

        val response = clientFactory.mapApi().searchPoi(
            key = ApiConfig.AMAP_WEB_KEY,
            keywords = keyword,
            city = city,
            types = types
        )
        if (response.status != "1") throw ThirdPartyApiException("高德API返回异常：${response.info}")
        val pois = response.pois.map { it.toPoi(city) }.filter { it.name.isNotBlank() }
        cacheDao.deletePois(cacheKey, category)
        cacheDao.upsertPois(pois.map { it.toEntity(cacheKey, category, city) })
        AppResult.Success(pois)
    }.getOrElse { AppResult.Error(ThirdPartyErrorMapper.toChineseMessage(it), it) }

    suspend fun geocode(city: String, address: String): AppResult<Location> = runCatching {
        if (!ApiConfig.hasAmapConfig()) throw ThirdPartyApiException("请先配置高德Web API Key")
        val response = clientFactory.mapApi().geocode(ApiConfig.AMAP_WEB_KEY, address, city)
        if (response.status != "1") throw ThirdPartyApiException("高德地理编码失败：${response.info}")
        val location = response.geocodes.firstOrNull()?.location.orEmpty().toLocation()
        AppResult.Success(location)
    }.getOrElse { AppResult.Error(ThirdPartyErrorMapper.toChineseMessage(it), it) }

    suspend fun reverseGeocode(location: Location): AppResult<String> = runCatching {
        if (!ApiConfig.hasAmapConfig()) throw ThirdPartyApiException("请先配置高德Web API Key")
        val coordinates = "${location.longitude},${location.latitude}"
        val response = clientFactory.mapApi().reverseGeocode(ApiConfig.AMAP_WEB_KEY, coordinates)
        if (response.status != "1") throw ThirdPartyApiException("高德逆地理编码失败：${response.info}")
        val component = response.regeocode?.addressComponent
        val city = component?.city.orEmpty()
            .ifBlank { component?.province.orEmpty() }
            .ifBlank { component?.district.orEmpty() }
            .trim()
        if (city.isBlank()) throw ThirdPartyApiException("未能识别当前位置城市")
        AppResult.Success(city)
    }.getOrElse { AppResult.Error(ThirdPartyErrorMapper.toChineseMessage(it), it) }
}

@Singleton
class ImageRepository @Inject constructor(
    private val clientFactory: ThirdPartyClientFactory,
    private val cacheDao: CacheDao
) {
    suspend fun searchImage(keyword: String): AppResult<Photo> = runCatching {
        if (!ApiConfig.hasPexelsConfig()) throw ThirdPartyApiException("请先配置Pexels API Key")
        val normalizedKeyword = keyword.trim()
        cacheDao.getImage(normalizedKeyword)?.takeIf { it.isFresh(IMAGE_CACHE_MS) }?.toPhoto()?.let { return AppResult.Success(it) }
        val response = clientFactory.imageApi().searchImages(
            apiKey = ApiConfig.PEXELS_API_KEY,
            query = normalizedKeyword.ifBlank { "travel" }
        )
        val photo = response.photos.firstOrNull()?.let {
            Photo(
                keyword = normalizedKeyword,
                url = it.src.large2x.ifBlank { it.src.large.ifBlank { it.src.medium } },
                photographer = it.photographer
            )
        } ?: throw ThirdPartyApiException("Pexels未找到相关图片")
        cacheDao.upsertImage(photo.toEntity())
        AppResult.Success(photo)
    }.getOrElse { AppResult.Error(ThirdPartyErrorMapper.toChineseMessage(it), it) }
}

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mapRepository: MapRepository
) {
    suspend fun detectCurrentCity(): AppResult<String> = runCatching {
        val location = readDeviceLocation() ?: throw ThirdPartyApiException("无法获取当前位置，请搜索城市")
        when (val result = mapRepository.reverseGeocode(Location(location.longitude, location.latitude))) {
            is AppResult.Success -> result.data
            is AppResult.Error -> throw ThirdPartyApiException(result.message, result.cause)
            AppResult.Loading -> throw ThirdPartyApiException("正在定位，请稍后重试")
            AppResult.Empty -> throw ThirdPartyApiException("未能识别当前位置城市")
        }
    }.fold(
        onSuccess = { AppResult.Success(it) },
        onFailure = { AppResult.Error(ThirdPartyErrorMapper.toChineseMessage(it), it) }
    )

    private suspend fun readDeviceLocation(): AndroidLocation? {
        if (!hasLocationPermission()) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        val lastKnown = providers.mapNotNull { provider ->
            runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull { it.time }
        if (lastKnown != null) return lastKnown

        val provider = providers.firstOrNull { provider ->
            runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)
        } ?: return null

        return withTimeoutOrNull(8_000L) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: AndroidLocation) {
                        if (continuation.isActive) continuation.resume(location)
                    }

                    override fun onProviderDisabled(provider: String) = Unit
                    override fun onProviderEnabled(provider: String) = Unit
                }
                runCatching { manager.requestSingleUpdate(provider, listener, Looper.getMainLooper()) }
                    .onFailure { if (continuation.isActive) continuation.resume(null) }
                continuation.invokeOnCancellation { manager.removeUpdates(listener) }
            }
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

object WeatherMapper {
    fun fromCache(entity: CachedWeatherEntity): WeatherForecast = entity.toWeatherForecast()
}

private fun CachedWeatherEntity.toWeatherForecast(): WeatherForecast = WeatherForecast(
    city = city,
    date = date,
    weather = weather,
    temperature = temperature,
    tempMax = tempMax,
    tempMin = tempMin,
    humidity = humidity,
    wind = wind,
    airQuality = airQuality,
    precipitation = precipitation,
    icon = icon
)

private fun WeatherForecast.toEntity(cacheKey: String): CachedWeatherEntity = CachedWeatherEntity(
    cacheKey = cacheKey,
    city = city,
    date = date,
    weather = weather,
    temperature = temperature,
    tempMax = tempMax,
    tempMin = tempMin,
    humidity = humidity,
    wind = wind,
    airQuality = airQuality,
    precipitation = precipitation,
    icon = icon,
    cachedAt = System.currentTimeMillis()
)

private fun CachedPoiEntity.toPoi(): Poi = Poi(
    id = id,
    name = name,
    type = type,
    address = address,
    telephone = telephone,
    location = Location(longitude, latitude),
    rating = rating,
    photoUrl = photoUrl
)

private fun Poi.toEntity(cacheKey: String, category: String, city: String): CachedPoiEntity = CachedPoiEntity(
    id = "$category-$cacheKey-$id",
    cacheKey = cacheKey,
    category = category,
    city = city,
    name = name,
    type = type,
    address = address,
    telephone = telephone,
    longitude = location.longitude,
    latitude = location.latitude,
    rating = rating,
    photoUrl = photoUrl,
    cachedAt = System.currentTimeMillis()
)

private fun AMapPoi.toPoi(city: String): Poi = Poi(
    id = id.ifBlank { "$city-$name-$location" },
    name = name,
    type = type,
    address = address,
    telephone = tel,
    location = location.toLocation(),
    rating = bizExt?.rating.orEmpty(),
    photoUrl = photos.firstOrNull { it.url.isNotBlank() }?.url.orEmpty()
)

private fun CachedImageEntity.toPhoto(): Photo = Photo(keyword = keyword, url = imageUrl, photographer = photographer)

private fun Photo.toEntity(): CachedImageEntity = CachedImageEntity(
    keyword = keyword,
    imageUrl = url,
    photographer = photographer,
    cachedAt = System.currentTimeMillis()
)

private fun CachedWeatherEntity.isFresh(maxAgeMs: Long): Boolean = System.currentTimeMillis() - cachedAt <= maxAgeMs
private fun CachedPoiEntity.isFresh(maxAgeMs: Long): Boolean = System.currentTimeMillis() - cachedAt <= maxAgeMs
private fun CachedImageEntity.isFresh(maxAgeMs: Long): Boolean = System.currentTimeMillis() - cachedAt <= maxAgeMs

private fun String.toLocation(): Location {
    val parts = split(",")
    return Location(
        longitude = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0,
        latitude = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
    )
}
