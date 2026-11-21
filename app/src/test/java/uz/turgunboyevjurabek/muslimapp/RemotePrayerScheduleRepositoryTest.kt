package uz.turgunboyevjurabek.muslimapp

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.data.network.ApiService
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerTimesResponseDto
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.RemotePrayerScheduleRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.PrayerTimes
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import java.time.YearMonth

class RemotePrayerScheduleRepositoryTest {
    private val region = Regions("Qo‘qon shahri", "qoqon")
    private val expected = PrayerTimes("05:36", "06:58", "11:57", "15:15", "17:00", "18:18")
    private val times = """{"bomdod":"05:36","quyosh":"06:58","peshin":"11:57","asr":"15:15","shom":"17:00","xufton":"18:18"}"""

    @Test
    fun mapsApiJsonForEachRequestedPeriod() = runBlocking {
        val requested = mutableListOf<String>()
        val api = object : ApiService {
            override suspend fun getPrayerTimes(region: String, period: String): PrayerTimesResponseDto {
                requested.add(period)
                assertEquals("qoqon", region)
                val day = """{"date":"12.11.2026","times":$times}"""
                val json = when (period) {
                    "today" -> """
                        {"meta":{"date":"2026-11-12"},
                         "today":{"times":$times,"current":{"label":"Asr","start":"15:15"}}}
                    """.trimIndent()
                    "week" -> """{"period_table":[${List(7) { day }.joinToString()}]}"""
                    else -> """{"period_table":[$day]}"""
                }
                return Gson().fromJson(json, PrayerTimesResponseDto::class.java)
            }
        }
        val remote = RemotePrayerScheduleRepository(api)
        val daily = remote.getDailySchedule(region)
        assertEquals("12.11.2026", daily.date)
        assertEquals(expected, daily.times)
        assertEquals("Asr", daily.featuredPrayer)
        assertEquals("15:15", daily.featuredTime)
        assertEquals(true, daily.featuredIsCurrent)

        val weekly = remote.getWeeklySchedule(region)
        assertEquals(7, weekly.size)
        assertEquals(expected, weekly.last().times)
        val monthly = remote.getMonthlySchedule(region, YearMonth.of(2026, 11)).single()
        assertEquals(12, monthly.day)
        assertEquals(11, monthly.month)
        assertEquals(expected, monthly.times)
        assertEquals(listOf("today", "week", "2026-11"), requested)
    }
}
