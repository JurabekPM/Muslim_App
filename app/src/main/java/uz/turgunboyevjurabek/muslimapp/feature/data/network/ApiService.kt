package uz.turgunboyevjurabek.muslimapp.feature.data.network

import retrofit2.http.GET
import retrofit2.http.Query

internal interface ApiService {
    @GET("index.php?format=json&lang=lotin")
    suspend fun getPrayerTimes(
        @Query("region") region: String,
        @Query("period") period: String,
    ): PrayerTimesResponseDto
}
