package uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily

import uz.turgunboyevjurabek.muslimapp.feature.domain.model.HijriDate
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.PrayerTimes

data class Bugungi(
    val date: String? = null,
    val hijriDate: HijriDate? = null,
    val region: String? = null,
    val times: PrayerTimes? = null,
    val weekday: String? = null,
    val featuredPrayer: String? = null,
    val featuredTime: String? = null,
    val featuredIsCurrent: Boolean = false,
)
