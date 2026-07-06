package com.example.ai.repository

import android.content.Context
import com.example.ai.common.AppResult
import com.example.ai.database.FavoriteDao
import com.example.ai.database.HistoryDao
import com.example.ai.database.UserDao
import com.example.ai.model.Favorite
import com.example.ai.model.History
import com.example.ai.model.UserEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

abstract class BaseRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    protected suspend fun <T> emptyResult(): AppResult<T> = withContext(dispatcher) {
        AppResult.Empty
    }
}

data class UserProfileInput(
    val phone: String,
    val password: String,
    val nickname: String,
    val avatar: String,
    val gender: String,
    val birthday: String?,
    val signature: String?,
    val city: String?,
    val travelPreferences: List<String>
)

@Singleton
class SessionRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferences = context.getSharedPreferences("account_session", Context.MODE_PRIVATE)
    private val _currentPhone = MutableStateFlow(preferences.getString(KEY_CURRENT_PHONE, "").orEmpty())
    val currentPhone: StateFlow<String> = _currentPhone.asStateFlow()

    fun rememberedPhone(): String = if (preferences.getBoolean(KEY_REMEMBER_LOGIN, false)) {
        preferences.getString(KEY_CURRENT_PHONE, "").orEmpty()
    } else {
        ""
    }

    fun setLoggedIn(phone: String, rememberLogin: Boolean) {
        val normalizedPhone = phone.trim()
        preferences.edit()
            .putString(KEY_CURRENT_PHONE, normalizedPhone)
            .putBoolean(KEY_REMEMBER_LOGIN, rememberLogin)
            .apply()
        _currentPhone.value = normalizedPhone
    }

    fun clearLogin() {
        preferences.edit()
            .remove(KEY_CURRENT_PHONE)
            .putBoolean(KEY_REMEMBER_LOGIN, false)
            .apply()
        _currentPhone.value = ""
    }

    fun clearTransientLoginWhenNotRemembered() {
        if (!preferences.getBoolean(KEY_REMEMBER_LOGIN, false)) {
            preferences.edit().remove(KEY_CURRENT_PHONE).apply()
            _currentPhone.value = ""
        }
    }

    private companion object {
        const val KEY_CURRENT_PHONE = "current_phone"
        const val KEY_REMEMBER_LOGIN = "remember_login"
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionRepository: SessionRepository
) {
    fun observeCurrentUser(): Flow<UserEntity?> = sessionRepository.currentPhone.flatMapLatest { phone ->
        if (phone.isBlank()) flowOf(null) else userDao.observeByPhone(phone)
    }

    suspend fun getCurrentUser(): UserEntity? = withContext(Dispatchers.IO) {
        sessionRepository.currentPhone.value.takeIf { it.isNotBlank() }?.let { userDao.getByPhone(it) }
    }

    suspend fun getUserByPhone(phone: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getByPhone(phone.trim())
    }

    suspend fun saveUser(input: UserProfileInput): UserEntity = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val existing = userDao.getByPhone(input.phone.trim())
        val user = UserEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            phone = input.phone.trim(),
            password = input.password.takeIf { it.isNotBlank() } ?: existing?.password.orEmpty(),
            nickname = input.nickname.trim(),
            avatar = input.avatar.trim(),
            gender = input.gender,
            birthday = input.birthday?.takeIf { it.isNotBlank() },
            signature = input.signature?.trim()?.takeIf { it.isNotBlank() },
            city = input.city?.trim()?.takeIf { it.isNotBlank() },
            travelPreference = input.travelPreferences.joinToString(","),
            createTime = existing?.createTime ?: now,
            updateTime = now
        )
        userDao.upsert(user)
        sessionRepository.setLoggedIn(user.phone, rememberLogin = false)
        user
    }

    suspend fun updateProfile(user: UserEntity, input: UserProfileInput): UserEntity = withContext(Dispatchers.IO) {
        val updated = user.copy(
            nickname = input.nickname.trim(),
            avatar = input.avatar.trim(),
            gender = input.gender,
            birthday = input.birthday?.takeIf { it.isNotBlank() },
            signature = input.signature?.trim()?.takeIf { it.isNotBlank() },
            city = input.city?.trim()?.takeIf { it.isNotBlank() },
            travelPreference = input.travelPreferences.joinToString(","),
            updateTime = System.currentTimeMillis()
        )
        userDao.upsert(updated)
        updated
    }

    suspend fun updateAvatar(id: String, avatar: String) = withContext(Dispatchers.IO) {
        userDao.updateAvatar(id, avatar.trim(), System.currentTimeMillis())
    }

    suspend fun updateNickname(id: String, nickname: String) = withContext(Dispatchers.IO) {
        userDao.updateNickname(id, nickname.trim(), System.currentTimeMillis())
    }

    fun logout() = sessionRepository.clearLogin()
}

class SplashRepository @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userDao: UserDao
) {
    suspend fun rememberedDestination(): String? {
        val phone = sessionRepository.rememberedPhone()
        return phone.takeIf { it.isNotBlank() }?.let { p ->
            withContext(Dispatchers.IO) {
                p.takeIf { userDao.getByPhone(p) != null }
            }
        }
    }

    fun clearTransientLoginWhenNotRemembered() {
        sessionRepository.clearTransientLoginWhenNotRemembered()
    }
}

class LoginRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionRepository: SessionRepository
) {
    suspend fun login(phone: String, password: String, rememberLogin: Boolean): UserEntity? = withContext(Dispatchers.IO) {
        val normalizedPhone = phone.trim()
        val user = userDao.getByPhoneAndPassword(normalizedPhone, password) ?: userDao.getByPhone(normalizedPhone)
            ?.takeIf { it.password.isBlank() }
            ?.copy(password = password, updateTime = System.currentTimeMillis())
            ?.also { userDao.upsert(it) }
        if (user != null) sessionRepository.setLoggedIn(user.phone, rememberLogin)
        user
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val sessionRepository: SessionRepository
) {
    fun observeCityFavorites(): Flow<List<Favorite>> = sessionRepository.currentPhone.flatMapLatest { phone ->
        if (phone.isBlank()) flowOf(emptyList()) else favoriteDao.observeByUserAndType(phone, FavoriteTypeCity)
    }

    suspend fun isCityFavorite(city: String): Boolean = withContext(Dispatchers.IO) {
        val phone = sessionRepository.currentPhone.value
        phone.isNotBlank() && favoriteDao.getByUserTypeAndTarget(phone, FavoriteTypeCity, city.trim()) != null
    }

    suspend fun toggleCityFavorite(city: String, imageUrl: String): Boolean = withContext(Dispatchers.IO) {
        val phone = sessionRepository.currentPhone.value
        val normalizedCity = city.trim()
        if (phone.isBlank() || normalizedCity.isBlank()) return@withContext false
        val existing = favoriteDao.getByUserTypeAndTarget(phone, FavoriteTypeCity, normalizedCity)
        if (existing == null) {
            favoriteDao.upsert(
                Favorite(
                    id = UUID.randomUUID().toString(),
                    userPhone = phone,
                    targetId = normalizedCity,
                    targetType = FavoriteTypeCity,
                    title = normalizedCity,
                    subtitle = "City favorite",
                    imageUrl = imageUrl,
                    createdAt = System.currentTimeMillis()
                )
            )
            true
        } else {
            favoriteDao.deleteById(existing.id)
            false
        }
    }
}

class RegisterRepository : BaseRepository()
class HomeRepository : BaseRepository()
class PlannerRepository : BaseRepository()
class ItineraryRepository : BaseRepository()
class ScenicDetailRepository : BaseRepository()
class WeatherRepository : BaseRepository()
class MapRepository : BaseRepository()
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao,
    private val sessionRepository: SessionRepository
) {
    fun observePlanHistory(): Flow<List<History>> = sessionRepository.currentPhone.flatMapLatest { phone ->
        if (phone.isBlank()) flowOf(emptyList()) else historyDao.observeByUserAndType(phone, HistoryTypePlan)
    }

    suspend fun addPlanHistory(
        targetId: String,
        title: String,
        description: String,
        days: Int,
        scheduleCount: Int,
        hotelCount: Int,
        foodCount: Int,
        planJson: String = ""
    ) = withContext(Dispatchers.IO) {
        val phone = sessionRepository.currentPhone.value
        if (phone.isBlank()) return@withContext
        historyDao.upsert(
            History(
                id = UUID.randomUUID().toString(),
                userPhone = phone,
                targetId = targetId,
                targetType = HistoryTypePlan,
                title = title,
                description = description,
                days = days,
                scheduleCount = scheduleCount,
                hotelCount = hotelCount,
                foodCount = foodCount,
                planJson = planJson,
                visitedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getPlanHistoryById(id: String): History? = withContext(Dispatchers.IO) {
        val phone = sessionRepository.currentPhone.value
        if (phone.isBlank()) null else historyDao.getByIdForUser(id, phone, HistoryTypePlan)
    }

    suspend fun deletePlanHistory(id: String) = withContext(Dispatchers.IO) {
        historyDao.deleteById(id)
    }
}
class ProfileRepository : BaseRepository()
class SettingsRepository : BaseRepository()
class AIChatRepository : BaseRepository()

private const val FavoriteTypeCity = "city"
private const val HistoryTypePlan = "plan"