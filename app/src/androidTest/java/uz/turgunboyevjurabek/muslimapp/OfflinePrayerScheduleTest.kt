package uz.turgunboyevjurabek.muslimapp

import android.net.ConnectivityManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import uz.turgunboyevjurabek.muslimapp.feature.data.local.PrayerScheduleStore
import uz.turgunboyevjurabek.muslimapp.feature.data.network.ApiService
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerTimesDayDto
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerTimesDto
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerTimesMetadataDto
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerTimesResponseDto
import uz.turgunboyevjurabek.muslimapp.feature.data.network.TodayPrayerTimesDto
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.CachedPrayerScheduleRepository
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.DataStoreRegionSelectionRepository
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.RemotePrayerScheduleRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionSelection
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleSource
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.ScheduleDay
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.ScheduleMonth
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.prayerZone
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class OfflinePrayerScheduleTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val today by lazy {
        val epochSecond = checkNotNull(InstrumentationRegistry.getArguments().getString("offlineScheduleEpoch")).toLong()
        Instant.ofEpochSecond(epochSecond).atZone(prayerZone).toLocalDate()
    }
    private val month get() = YearMonth.from(today)
    private val region = Regions.resolve("toshkent-shahri")
    private val dailyTimes = sampleTimes(31)
    private val weeklyTimes = sampleTimes(41)
    private val lastWeeklyTimes = sampleTimes(42)
    private val monthlyTimes = sampleTimes(51)

    @Before
    fun requireDisposableTestRun() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("offlineScheduleTest") == "true")
    }

    @Test
    fun seedSchedules() = runBlocking {
        AppLanguage.Uzbek.save(context)
        DataStoreRegionSelectionRepository(context).save(RegionSelection(region, fromGps = false))
        val repository = CachedPrayerScheduleRepository(
            RemotePrayerScheduleRepository(fixtureApi()),
            PrayerScheduleStore(context),
        )
        withContext(Dispatchers.Main) {
            val schedules = repository.schedules(this) { true }
            val day = ScheduleDay(region, today)
            schedules.daily.loadAndVerifySaved(day)
            schedules.weekly.loadAndVerifySaved(day)
            schedules.monthly.loadAndVerifySaved(ScheduleMonth(region, month))
        }
    }

    @Test
    fun showsCachedSchedulesAfterOfflineRestart() {
        val network = context.getSystemService(ConnectivityManager::class.java)
        assertNotNull("ConnectivityManager is required", network)
        assertNull("The app must start with no active network", network?.activeNetwork)
        val localized = AppLanguage.load(context).localize(context)
        val savedScheduleLabel = localized.getString(R.string.saved_schedule, "").trimEnd()

        ActivityScenario.launch(MainActivity::class.java).use {
            assertVisible(hasText(savedScheduleLabel, substring = true))
            val inDailySchedule = hasAnyAncestor(hasContentDescription(localized.getString(R.string.prayer_schedule)))
            for (time in dailyTimes) assertVisible(hasText(time) and inDailySchedule)

            tapTab(localized.getString(R.string.weekly))
            assertVisible(hasText(savedScheduleLabel, substring = true))
            for (time in weeklyTimes) assertVisible(hasText(time))
            compose.onNode(hasScrollToIndexAction()).performScrollToIndex(7)
            val lastDay = today.plusDays(6)
            val monthName = localized.resources.getStringArray(R.array.calendar_month_names)[lastDay.monthValue - 1]
            val lastDayLabel = localized.getString(R.string.schedule_date, lastDay.dayOfMonth, monthName)
            compose.onNode(hasText(lastDayLabel) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)
            for (time in lastWeeklyTimes) assertVisible(hasText(time))

            tapTab(localized.getString(R.string.monthly))
            assertVisible(hasText(savedScheduleLabel, substring = true))
            for (time in monthlyTimes) assertVisible(hasText(time))
            assertNull("The device must stay offline throughout verification", network?.activeNetwork)
        }
    }

    private fun assertVisible(matcher: SemanticsMatcher) {
        compose.waitUntil(10_000) { compose.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(matcher).performScrollTo().assertIsDisplayed()
    }

    private fun tapTab(label: String) {
        compose.onNode(hasText(label) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)
    }

    private suspend fun <K, T> PrayerScheduleSource<K, T>.loadAndVerifySaved(key: K) {
        load(key)
        val loaded = withTimeout(10_000) {
            states.first { it[key]?.requesting == false }.getValue(key)
        }
        assertNull("Fixture failed to persist for $key", loaded.failure)
        assertNotNull("Missing fixture for $key", loaded.snapshot)
    }

    private fun fixtureApi() = object : ApiService {
        override suspend fun getPrayerTimes(region: String, period: String): PrayerTimesResponseDto {
            assertEquals(this@OfflinePrayerScheduleTest.region.slug, region)
            return when (period) {
                "today" -> PrayerTimesResponseDto(
                    meta = PrayerTimesMetadataDto(today.toString()),
                    today = TodayPrayerTimesDto(dailyTimes.toDto(), current = null, next = null),
                    periodTable = null,
                )
                "week" -> table(List(7) { index ->
                    val times = if (index == 6) lastWeeklyTimes else weeklyTimes
                    entry(today.plusDays(index.toLong()), times)
                })
                month.toString() -> table(List(month.lengthOfMonth()) { index ->
                    entry(month.atDay(index + 1), monthlyTimes)
                })
                else -> error("Unexpected fixture period $period")
            }
        }
    }

    private fun table(days: List<PrayerTimesDayDto>) =
        PrayerTimesResponseDto(meta = null, today = null, periodTable = days)

    private fun entry(date: LocalDate, times: List<String>) =
        PrayerTimesDayDto(date.format(DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT)), times.toDto())

    private fun List<String>.toDto() = PrayerTimesDto(this[0], this[1], this[2], this[3], this[4], this[5])

    private fun sampleTimes(minute: Int) = listOf(5, 6, 12, 15, 18, 19).map { hour ->
        LocalTime.of(hour, minute).toString()
    }
}
