package uz.turgunboyevjurabek.muslimapp

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.JsonParser
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import uz.turgunboyevjurabek.muslimapp.feature.data.local.PrayerScheduleStore
import uz.turgunboyevjurabek.muslimapp.feature.data.local.ScheduleCacheFormat
import uz.turgunboyevjurabek.muslimapp.feature.data.network.ApiService
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerTimesResponseDto
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.CachedPrayerScheduleRepository
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.RemotePrayerScheduleRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.PrayerTimes
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleSource
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.SavedSchedule
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.ScheduleDay
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.ScheduleFailure
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.ScheduleMonth

class PrayerScheduleStoreTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun reopeningRecoversAllSchedulesAndUsesLatestWhenRequestedPeriodIsMissing() = runBlocking {
        val file = File(folder.root, "prayer.preferences_pb")
        val first = Job()
        try {
            // Start with literal v1 JSON written independently of the new serializer.
            val times = """{"tong_saharlik":"05:36","quyosh":"06:58","peshin":"11:57","asr":"15:15","shom_iftor":"17:00","hufton":"18:18"}"""
            val store = openPreferences(file, first)
            store.edit { preferences ->
                preferences[stringPreferencesKey("daily:qoqon")] = """
                    [
                      {"period":"2026-11-11","savedAt":1,"data":{"date":"11.11.2026"}},
                      {"period":"2026-11-12","savedAt":2,"data":{"date":"12.11.2026","times":$times,
                       "featuredPrayer":"Asr","featuredTime":"15:15","featuredIsCurrent":true}}
                    ]
                """.trimIndent()
                preferences[stringPreferencesKey("weekly:qoqon")] = """
                    [{"period":"2026-11-12","savedAt":2,"data":[{"date":"12.11.2026","times":$times}]}]
                """.trimIndent()
                preferences[stringPreferencesKey("monthly:qoqon")] = """
                    [{"period":"2026-11","savedAt":2,"data":[{"date":"12.11.2026","day":12,"month":11,
                      "regionNumber":3,"hijri_date":{"day":2,"month":"Jumada"},"times":$times}]}]
                """.trimIndent()
            }
        } finally {
            first.cancelAndJoin()
        }

        val second = Job()
        try {
            val preferences = openPreferences(file, second)
            val store = PrayerScheduleStore(preferences)
            val oldDay = store.read("daily:qoqon", "2026-11-11", ScheduleCacheFormat.Daily)
            val fallback = store.read("daily:qoqon", "2026-11-13", ScheduleCacheFormat.Daily)
            assertEquals("11.11.2026", oldDay?.data?.date)
            assertEquals("2026-11-12", fallback?.period)
            assertEquals("12.11.2026", fallback?.data?.date)
            assertEquals(2L, fallback?.savedAt)
            assertEquals("12.11.2026", store.read("weekly:qoqon", "2026-11-12", ScheduleCacheFormat.Weekly)?.data?.single()?.date)
            assertEquals(12, store.read("monthly:qoqon", "2026-12", ScheduleCacheFormat.Monthly)?.data?.single()?.day)
            assertNull(store.read("daily:andijon", "2026-11-12", ScheduleCacheFormat.Daily))

            var online = false
            var requests = 0
            val api = object : ApiService {
                override suspend fun getPrayerTimes(region: String, period: String): PrayerTimesResponseDto {
                    requests++
                    assertEquals("qoqon", region)
                    assertEquals("today", period)
                    throw HttpException(Response.error<PrayerTimesResponseDto>(502, "".toResponseBody()))
                }
            }
            val repository = CachedPrayerScheduleRepository(RemotePrayerScheduleRepository(api), store)
            val schedules = repository.schedules(this) { online }
            val region = Regions("Qo‘qon shahri", "qoqon")
            val day = ScheduleDay(region, LocalDate.of(2026, 11, 12))
            val weekFallback = day.copy(date = day.date.plusDays(1))
            val monthFallback = ScheduleMonth(region, YearMonth.of(2026, 12))

            val expectedTimes = PrayerTimes("05:36", "06:58", "11:57", "15:15", "17:00", "18:18")
            val daily = schedules.daily.loadAndWait(day).snapshot?.data
            val weekly = schedules.weekly.loadAndWait(weekFallback).snapshot
            val monthly = schedules.monthly.loadAndWait(monthFallback).snapshot
            assertEquals("12.11.2026", daily?.date)
            assertEquals(expectedTimes, daily?.times)
            assertEquals("Asr", daily?.featuredPrayer)
            assertEquals("15:15", daily?.featuredTime)
            assertEquals(true, daily?.featuredIsCurrent)
            assertEquals(false, oldDay?.data?.featuredIsCurrent)
            assertEquals(day.date.toString(), weekly?.period)
            assertEquals(expectedTimes, weekly?.data?.single()?.times)
            assertEquals(12, monthly?.data?.single()?.day)
            assertEquals(expectedTimes, monthly?.data?.single()?.times)
            assertEquals(3, monthly?.data?.single()?.regionNumber)
            assertEquals("Jumada", monthly?.data?.single()?.hijriDate?.month)
            assertEquals(0, requests)

            online = true
            schedules.daily.load(day)
            assertEquals(false, schedules.daily.states.value[day]?.requesting)
            assertEquals(0, requests)
            val failedRefresh = schedules.daily.loadAndWait(day, force = true)
            assertEquals(1, requests)
            assertEquals("12.11.2026", failedRefresh.snapshot?.data?.date)
            assertEquals(ScheduleFailure.Http(502), failedRefresh.failure)

            val fresh = SavedSchedule(Bugungi(date = "13.11.2026", times = expectedTimes), "2026-11-13", 3L)
            store.write("daily:qoqon", fresh, ScheduleCacheFormat.Daily)
            assertEquals(fresh, store.read("daily:qoqon", fresh.period, ScheduleCacheFormat.Daily))

            val currentWeek = checkNotNull(weekly).copy(period = "2026-11-13", savedAt = 3L)
            store.write("weekly:qoqon", currentWeek, ScheduleCacheFormat.Weekly)
            assertEquals(currentWeek, store.read("weekly:qoqon", currentWeek.period, ScheduleCacheFormat.Weekly))
            val currentMonth = checkNotNull(monthly).copy(period = "2026-12", savedAt = 3L)
            store.write("monthly:qoqon", currentMonth, ScheduleCacheFormat.Monthly)
            assertEquals(currentMonth, store.read("monthly:qoqon", currentMonth.period, ScheduleCacheFormat.Monthly))

            val raw = checkNotNull(preferences.data.first()[stringPreferencesKey("daily:qoqon")])
            val saved = JsonParser.parseString(raw).asJsonArray[0].asJsonObject.getAsJsonObject("data")
            val times = saved.getAsJsonObject("times")
            assertEquals("05:36", times.get("tong_saharlik").asString)
            assertEquals("17:00", times.get("shom_iftor").asString)
            assertEquals("18:18", times.get("hufton").asString)
        } finally {
            second.cancelAndJoin()
        }
    }

    private suspend fun <K, T> PrayerScheduleSource<K, T>.loadAndWait(key: K, force: Boolean = false) =
        withTimeout(5_000L) {
            load(key, force)
            checkNotNull(states.first { it[key]?.requesting == false }[key])
        }

    private fun openPreferences(file: File, job: Job) =
        PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
}
