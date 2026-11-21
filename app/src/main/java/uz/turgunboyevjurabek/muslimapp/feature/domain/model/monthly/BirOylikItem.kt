package uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly

import uz.turgunboyevjurabek.muslimapp.feature.domain.model.HijriDate
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.PrayerTimes

data class BirOylikItem(
    val date: String? = null,
    val hijriDate: HijriDate? = null,
    val region: String? = null,
    val times: PrayerTimes? = null,
    val weekday: String? = null,
    val day: Int? = null,
    val month: Int? = null,
    val regionNumber: Int? = null,
)
