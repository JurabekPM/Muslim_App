package uz.turgunboyevjurabek.muslimapp.feature.data.repository

import uz.turgunboyevjurabek.muslimapp.feature.data.network.ApiService
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerTimes
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerTimesDay
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly.Birhaftalik
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly.BirhaftalikItem
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly.Times as WeeklyTimes
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly.BirOylik
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly.BirOylikItem
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly.Times as MonthlyTimes
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Times as DailyTimes
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleRepository
import javax.inject.Inject

internal class RemotePrayerScheduleRepository @Inject constructor(
    private val apiService: ApiService,
) : PrayerScheduleRepository {
    override suspend fun getDailySchedule(location: Regions): Bugungi {
        val response = apiService.getPrayerTimes(location.slug, "today")
        val date = LocalDate.parse(requireNotNull(response.meta?.date))
        val today = requireNotNull(response.today)
        val times = requireTimes(today.times)
        val current = today.current?.takeIf { !it.start.isNullOrBlank() }
        return Bugungi(
            date = date.format(apiDateFormat),
            region = location.nameRegion,
            times = DailyTimes(times.asr, times.xufton, times.peshin, times.quyosh, times.shom, times.bomdod),
            weekday = date.uzbekWeekday(),
            featuredPrayer = current?.label ?: today.next?.label,
            featuredTime = current?.start ?: today.next?.time,
            featuredIsCurrent = current != null,
        )
    }

    override suspend fun getWeeklySchedule(location: Regions): Birhaftalik {
        val days = apiService.getPrayerTimes(location.slug, "week").periodTable.orEmpty().take(7)
        require(days.size == 7)
        return days.mapTo(Birhaftalik()) { day ->
            val date = day.localDate()
            val times = requireTimes(day.times)
            BirhaftalikItem(
                date = date.format(apiDateFormat),
                region = location.nameRegion,
                times = WeeklyTimes(times.asr, times.xufton, times.peshin, times.quyosh, times.shom, times.bomdod),
                weekday = date.uzbekWeekday(),
            )
        }
    }

    override suspend fun getMonthlySchedule(location: Regions, month: YearMonth): BirOylik {
        val days = apiService.getPrayerTimes(location.slug, month.toString()).periodTable.orEmpty()
        require(days.isNotEmpty())
        return days.mapTo(BirOylik()) { day ->
            val date = day.localDate()
            val times = requireTimes(day.times)
            BirOylikItem(
                date = date.format(apiDateFormat),
                day = date.dayOfMonth,
                month = date.monthValue,
                region = location.nameRegion,
                times = MonthlyTimes(times.asr, times.xufton, times.peshin, times.quyosh, times.shom, times.bomdod),
                weekday = date.uzbekWeekday(),
            )
        }
    }

    private fun PrayerTimesDay.localDate(): LocalDate =
        LocalDate.parse(requireNotNull(date), apiDateFormat)

    private fun LocalDate.uzbekWeekday(): String =
        dayOfWeek.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("uz-Latn-UZ"))

    private fun requireTimes(value: PrayerTimes?): PrayerTimes {
        val times = requireNotNull(value)
        require(listOf(times.bomdod, times.quyosh, times.peshin, times.asr, times.shom, times.xufton)
            .all { it != null && timeFormat.matches(it) })
        return times
    }

    private companion object {
        val apiDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT)
        val timeFormat = Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]")
    }
}
