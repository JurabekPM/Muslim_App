package uz.turgunboyevjurabek.muslimapp.feature.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("index.php?format=json&lang=lotin")
    suspend fun getPrayerTimes(
        @Query("region") region: String,
        @Query("period") period: String,
    ): PrayerTimesResponse
}

data class PrayerTimesResponse(
    val meta: PrayerTimesMetadata?,
    val today: TodayPrayerTimes?,
    @SerializedName("period_table") val periodTable: List<PrayerTimesDay>?,
)

data class PrayerTimesMetadata(val date: String?)

data class TodayPrayerTimes(
    val times: PrayerTimes?,
    val current: PrayerSlot?,
    val next: PrayerSlot?,
)

data class PrayerSlot(val label: String?, val start: String?, val time: String?)

data class PrayerTimesDay(val date: String?, val times: PrayerTimes?)

data class PrayerTimes(
    val bomdod: String?,
    val quyosh: String?,
    val peshin: String?,
    val asr: String?,
    val shom: String?,
    val xufton: String?,
)
