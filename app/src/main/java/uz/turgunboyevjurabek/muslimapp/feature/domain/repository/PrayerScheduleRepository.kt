package uz.turgunboyevjurabek.muslimapp.feature.domain.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly.BirOylik
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly.Birhaftalik
import java.time.LocalDate
import java.time.YearMonth

interface PrayerScheduleRepository {
    /** Keeps schedule requests and their memory cache scoped to the caller's lifetime. */
    fun schedules(scope: CoroutineScope, isOnline: () -> Boolean): PrayerSchedules
}

data class PrayerSchedules(
    val daily: PrayerScheduleSource<ScheduleDay, Bugungi>,
    val weekly: PrayerScheduleSource<ScheduleDay, Birhaftalik>,
    val monthly: PrayerScheduleSource<ScheduleMonth, BirOylik>,
)

data class ScheduleDay(val region: Regions, val date: LocalDate)
data class ScheduleMonth(val region: Regions, val month: YearMonth)

interface PrayerScheduleSource<K, T> {
    val states: StateFlow<Map<K, PrayerScheduleState<T>>>
    fun load(key: K, force: Boolean = false)
}

data class SavedSchedule<T>(val data: T, val period: String, val savedAt: Long)

data class PrayerScheduleState<T>(
    val snapshot: SavedSchedule<T>? = null,
    val requesting: Boolean = false,
    val failure: ScheduleFailure? = null,
    internal val attemptedOnline: Boolean = false,
) {
    val refreshing: Boolean get() = requesting && snapshot != null
}

sealed interface ScheduleFailure {
    data object Offline : ScheduleFailure
    data object Network : ScheduleFailure
    data object Read : ScheduleFailure
    data object Save : ScheduleFailure
    data class Http(val code: Int) : ScheduleFailure
}
