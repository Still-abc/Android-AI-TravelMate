package com.example.ai.di

import android.content.Context
import androidx.room.Room
import com.example.ai.database.AppDatabase
import com.example.ai.database.CacheDao
import com.example.ai.database.MIGRATION_1_2
import com.example.ai.database.MIGRATION_2_3
import com.example.ai.database.MIGRATION_3_4
import com.example.ai.database.MIGRATION_4_5
import com.example.ai.database.MIGRATION_5_6
import com.example.ai.database.MIGRATION_6_7
import com.example.ai.database.MIGRATION_7_8
import com.example.ai.database.UserDao
import com.example.ai.network.AIApi
import com.example.ai.network.HotelApi
import com.example.ai.network.NetworkConfig
import com.example.ai.network.ScenicApi
import com.example.ai.network.WeatherApi
import com.example.ai.repository.AIChatRepository
import com.example.ai.repository.FavoriteRepository
import com.example.ai.repository.HistoryRepository
import com.example.ai.repository.HomeRepository
import com.example.ai.repository.ItineraryRepository
import com.example.ai.repository.LoginRepository
import com.example.ai.repository.MapRepository
import com.example.ai.repository.PlannerRepository
import com.example.ai.repository.ProfileRepository
import com.example.ai.repository.RegisterRepository
import com.example.ai.repository.ScenicDetailRepository
import com.example.ai.repository.SettingsRepository
import com.example.ai.repository.SplashRepository
import com.example.ai.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideBaseInterceptor(): Interceptor = Interceptor { chain -> chain.proceed(chain.request()) }

    @Provides
    @Singleton
    fun provideOkHttpClient(baseInterceptor: Interceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(baseInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides fun provideAIApi(retrofit: Retrofit): AIApi = retrofit.create(AIApi::class.java)
    @Provides fun provideWeatherApi(retrofit: Retrofit): WeatherApi = retrofit.create(WeatherApi::class.java)
    @Provides fun provideHotelApi(retrofit: Retrofit): HotelApi = retrofit.create(HotelApi::class.java)
    @Provides fun provideScenicApi(retrofit: Retrofit): ScenicApi = retrofit.create(ScenicApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "ai_travelmate.db"
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8).build()

    @Provides
    fun provideCacheDao(database: AppDatabase): CacheDao = database.cacheDao()

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides fun provideSplashRepository(sessionRepository: com.example.ai.repository.SessionRepository, userDao: UserDao) = SplashRepository(sessionRepository, userDao)
    @Provides fun provideLoginRepository(userDao: UserDao, sessionRepository: com.example.ai.repository.SessionRepository) = LoginRepository(userDao, sessionRepository)
    @Provides fun provideRegisterRepository() = RegisterRepository()
    @Provides fun provideHomeRepository() = HomeRepository()
    @Provides fun providePlannerRepository() = PlannerRepository()
    @Provides fun provideItineraryRepository() = ItineraryRepository()
    @Provides fun provideScenicDetailRepository() = ScenicDetailRepository()
    @Provides fun provideWeatherRepository() = WeatherRepository()
    @Provides fun provideMapRepository() = MapRepository()
    @Provides fun provideFavoriteRepository(database: AppDatabase, sessionRepository: com.example.ai.repository.SessionRepository) = FavoriteRepository(database.favoriteDao(), sessionRepository)
    @Provides fun provideHistoryRepository(database: AppDatabase, sessionRepository: com.example.ai.repository.SessionRepository) = HistoryRepository(database.historyDao(), sessionRepository)
    @Provides fun provideProfileRepository() = ProfileRepository()
    @Provides fun provideSettingsRepository() = SettingsRepository()
    @Provides fun provideAIChatRepository() = AIChatRepository()
}
