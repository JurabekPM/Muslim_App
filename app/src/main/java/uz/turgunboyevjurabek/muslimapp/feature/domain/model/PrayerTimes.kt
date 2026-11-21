package uz.turgunboyevjurabek.muslimapp.feature.domain.model

data class PrayerTimes(
    val bomdod: String?,
    val quyosh: String?,
    val peshin: String?,
    val asr: String?,
    val shom: String?,
    val xufton: String?,
)

data class HijriDate(val day: Int?, val month: String?)
