package uz.turgunboyevjurabek.muslimapp.feature.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import uz.turgunboyevjurabek.muslimapp.feature.data.network.ApiService
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.RemotePrayerScheduleRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleRepository
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class NetworkModule {
    @Binds
    abstract fun bindPrayerScheduleRepository(implementation: RemotePrayerScheduleRepository): PrayerScheduleRepository

    companion object {
        private const val BASE_URL = "https://namoz-vaqti.uz/"

        @Provides
        @Singleton
        fun provideApiService(): ApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
