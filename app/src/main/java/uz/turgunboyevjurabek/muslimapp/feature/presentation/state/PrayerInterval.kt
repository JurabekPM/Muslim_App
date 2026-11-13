package uz.turgunboyevjurabek.muslimapp.feature.presentation.state

import java.time.LocalTime
import java.time.format.DateTimeParseException

internal data class PrayerInterval(val fraction: Float, val minutesLeft: Long)

internal fun prayerInterval(start: String?, end: String?, now: LocalTime): PrayerInterval? {
    val from = parsePrayerTime(start) ?: return null
    val until = parsePrayerTime(end) ?: return null
    if (until <= from || now < from || now >= until) return null

    val total = until.toSecondOfDay() - from.toSecondOfDay()
    val elapsed = now.toSecondOfDay() - from.toSecondOfDay()
    return PrayerInterval(elapsed.toFloat() / total, (total - elapsed + 59L) / 60)
}

private fun parsePrayerTime(time: String?): LocalTime? = try {
    time?.let(LocalTime::parse)
} catch (_: DateTimeParseException) {
    null
}
