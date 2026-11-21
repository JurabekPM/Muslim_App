package uz.turgunboyevjurabek.muslimapp.feature.data.local

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.HijriDate
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.PrayerTimes
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly.BirOylikItem
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly.BirhaftalikItem

internal class ScheduleCacheFormat<T : Any> private constructor(
    val decode: (Gson, JsonElement) -> T?,
    val encode: (Gson, T) -> JsonElement,
) {
    companion object {
        val Daily = format(
            { day: CachedPrayerDay ->
                Bugungi(
                    date = day.date, hijriDate = day.hijriDate?.toDomain(), region = day.region,
                    times = day.times?.toDomain(), weekday = day.weekday,
                    featuredPrayer = day.featuredPrayer, featuredTime = day.featuredTime,
                    featuredIsCurrent = day.featuredIsCurrent ?: false,
                )
            },
            { day: Bugungi ->
                CachedPrayerDay(
                    date = day.date, hijriDate = day.hijriDate?.toCache(), region = day.region,
                    times = day.times?.toCache(), weekday = day.weekday,
                    featuredPrayer = day.featuredPrayer, featuredTime = day.featuredTime,
                    featuredIsCurrent = day.featuredIsCurrent,
                )
            },
        )

        val Weekly = format(
            { days: List<CachedPrayerDay> ->
                days.map { day ->
                    BirhaftalikItem(
                        date = day.date, hijriDate = day.hijriDate?.toDomain(), region = day.region,
                        times = day.times?.toDomain(), weekday = day.weekday,
                    )
                }
            },
            { days: List<BirhaftalikItem> ->
                days.map { day ->
                    CachedPrayerDay(
                        date = day.date, hijriDate = day.hijriDate?.toCache(), region = day.region,
                        times = day.times?.toCache(), weekday = day.weekday,
                    )
                }
            },
        )

        val Monthly = format(
            { days: List<CachedPrayerDay> ->
                days.map { day ->
                    BirOylikItem(
                        date = day.date, hijriDate = day.hijriDate?.toDomain(), region = day.region,
                        times = day.times?.toDomain(), weekday = day.weekday,
                        day = day.day, month = day.month, regionNumber = day.regionNumber,
                    )
                }
            },
            { days: List<BirOylikItem> ->
                days.map { day ->
                    CachedPrayerDay(
                        date = day.date, hijriDate = day.hijriDate?.toCache(), region = day.region,
                        times = day.times?.toCache(), weekday = day.weekday,
                        day = day.day, month = day.month, regionNumber = day.regionNumber,
                    )
                }
            },
        )

        private inline fun <reified D : Any, T : Any> format(
            crossinline toDomain: (D) -> T,
            crossinline toCache: (T) -> D,
        ): ScheduleCacheFormat<T> {
            val type = object : TypeToken<D>() {}.type
            return ScheduleCacheFormat(
                decode = { gson, json -> gson.fromJson<D>(json, type)?.let { toDomain(it) } },
                encode = { gson, value -> gson.toJsonTree(toCache(value), type) },
            )
        }
    }
}

// Field names match the payloads already in prayer_schedule_cache_v1.
private data class CachedPrayerDay(
    @SerializedName("date") val date: String?,
    @SerializedName("hijri_date") val hijriDate: CachedHijriDate?,
    @SerializedName("region") val region: String?,
    @SerializedName("times") val times: CachedPrayerTimes?,
    @SerializedName("weekday") val weekday: String?,
    @SerializedName("day") val day: Int? = null,
    @SerializedName("month") val month: Int? = null,
    @SerializedName("regionNumber") val regionNumber: Int? = null,
    @SerializedName("featuredPrayer") val featuredPrayer: String? = null,
    @SerializedName("featuredTime") val featuredTime: String? = null,
    @SerializedName("featuredIsCurrent") val featuredIsCurrent: Boolean? = null,
)

private data class CachedPrayerTimes(
    @SerializedName("tong_saharlik") val bomdod: String?,
    @SerializedName("quyosh") val quyosh: String?,
    @SerializedName("peshin") val peshin: String?,
    @SerializedName("asr") val asr: String?,
    @SerializedName("shom_iftor") val shom: String?,
    @SerializedName("hufton") val xufton: String?,
) {
    fun toDomain() = PrayerTimes(bomdod, quyosh, peshin, asr, shom, xufton)
}

private fun PrayerTimes.toCache() = CachedPrayerTimes(bomdod, quyosh, peshin, asr, shom, xufton)

private data class CachedHijriDate(
    @SerializedName("day") val day: Int?,
    @SerializedName("month") val month: String?,
) {
    fun toDomain() = HijriDate(day, month)
}

private fun HijriDate.toCache() = CachedHijriDate(day, month)
