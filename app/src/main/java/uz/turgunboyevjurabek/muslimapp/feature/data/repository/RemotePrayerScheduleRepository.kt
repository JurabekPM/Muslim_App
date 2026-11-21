package uz.turgunboyevjurabek.muslimapp.feature.data.repository

import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import javax.inject.Inject
import uz.turgunboyevjurabek.muslimapp.feature.data.network.ApiService
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerTimesDayDto
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerTimesDto
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly.BirOylikItem
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.PrayerTimes
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly.BirhaftalikItem

internal class RemotePrayerScheduleRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getDailySchedule(location: Regions): Bugungi {
        val response = apiService.getPrayerTimes(location.slug, "today")
        val date = LocalDate.parse(requireNotNull(response.meta?.date))
        val today = requireNotNull(response.today)
        val times = requireTimes(today.times)
        val current = today.current?.takeIf { !it.start.isNullOrBlank() }
        return Bugungi(
            date = date.format(apiDateFormat),
            region = location.nameRegion,
            times = times,
            weekday = date.uzbekWeekday(),
            featuredPrayer = current?.label ?: today.next?.label,
            featuredTime = current?.start ?: today.next?.time,
            featuredIsCurrent = current != null,
        )
    }

    suspend fun getWeeklySchedule(location: Regions): List<BirhaftalikItem> {
        val days = apiService.getPrayerTimes(location.slug, "week").periodTable.orEmpty().take(7)
        require(days.size == 7)
        return days.map { day ->
            val date = day.localDate()
            val times = requireTimes(day.times)
            BirhaftalikItem(
                date = date.format(apiDateFormat),
                region = location.nameRegion,
                times = times,
                weekday = date.uzbekWeekday(),
            )
        }
    }

    suspend fun getMonthlySchedule(location: Regions, month: YearMonth): List<BirOylikItem> {
        val days = apiService.getPrayerTimes(location.slug, month.toString()).periodTable.orEmpty()
        require(days.isNotEmpty())
        return days.map { day ->
            val date = day.localDate()
            val times = requireTimes(day.times)
            BirOylikItem(
                date = date.format(apiDateFormat),
                day = date.dayOfMonth,
                month = date.monthValue,
                region = location.nameRegion,
                times = times,
                weekday = date.uzbekWeekday(),
            )
        }
    }

    private fun PrayerTimesDayDto.localDate(): LocalDate =
        LocalDate.parse(requireNotNull(date), apiDateFormat)

    private fun LocalDate.uzbekWeekday(): String =
        dayOfWeek.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("uz-Latn-UZ"))

    private fun requireTimes(value: PrayerTimesDto?): PrayerTimes {
        val times = requireNotNull(value)
        require(listOf(times.bomdod, times.quyosh, times.peshin, times.asr, times.shom, times.xufton)
            .all { it != null && timeFormat.matches(it) })
        return PrayerTimes(
            bomdod = times.bomdod,
            quyosh = times.quyosh,
            peshin = times.peshin,
            asr = times.asr,
            shom = times.shom,
            xufton = times.xufton,
        )
    }

    private companion object {
        val apiDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT)
        val timeFormat = Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]")
    }
}
