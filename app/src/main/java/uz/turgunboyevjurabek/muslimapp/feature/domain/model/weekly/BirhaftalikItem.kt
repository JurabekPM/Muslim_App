package uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly

import uz.turgunboyevjurabek.muslimapp.feature.domain.model.HijriDate
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.PrayerTimes

data class BirhaftalikItem(
    val date: String? = null,
    val hijriDate: HijriDate? = null,
    val region: String? = null,
    val times: PrayerTimes? = null,
    val weekday: String? = null,
)
