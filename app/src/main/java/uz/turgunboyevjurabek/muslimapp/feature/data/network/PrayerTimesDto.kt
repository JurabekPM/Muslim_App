package uz.turgunboyevjurabek.muslimapp.feature.data.network

import com.google.gson.annotations.SerializedName

internal data class PrayerTimesResponseDto(
    @SerializedName("meta") val meta: PrayerTimesMetadataDto?,
    @SerializedName("today") val today: TodayPrayerTimesDto?,
    @SerializedName("period_table") val periodTable: List<PrayerTimesDayDto>?,
)

internal data class PrayerTimesMetadataDto(@SerializedName("date") val date: String?)

internal data class TodayPrayerTimesDto(
    @SerializedName("times") val times: PrayerTimesDto?,
    @SerializedName("current") val current: PrayerSlotDto?,
    @SerializedName("next") val next: PrayerSlotDto?,
)

internal data class PrayerSlotDto(
    @SerializedName("label") val label: String?,
    @SerializedName("start") val start: String?,
    @SerializedName("time") val time: String?,
)

internal data class PrayerTimesDayDto(
    @SerializedName("date") val date: String?,
    @SerializedName("times") val times: PrayerTimesDto?,
)

internal data class PrayerTimesDto(
    @SerializedName("bomdod") val bomdod: String?,
    @SerializedName("quyosh") val quyosh: String?,
    @SerializedName("peshin") val peshin: String?,
    @SerializedName("asr") val asr: String?,
    @SerializedName("shom") val shom: String?,
    @SerializedName("xufton") val xufton: String?,
)
