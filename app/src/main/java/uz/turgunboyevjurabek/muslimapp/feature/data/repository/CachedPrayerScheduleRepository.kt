package uz.turgunboyevjurabek.muslimapp.feature.data.repository

import kotlinx.coroutines.CoroutineScope
import uz.turgunboyevjurabek.muslimapp.feature.data.local.PrayerScheduleStore
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly.BirOylik
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly.Birhaftalik
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerSchedules
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.ScheduleDay
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.ScheduleMonth
import javax.inject.Inject

internal class CachedPrayerScheduleRepository @Inject constructor(
    private val remote: RemotePrayerScheduleRepository,
    private val store: PrayerScheduleStore,
) : PrayerScheduleRepository {
    override fun schedules(scope: CoroutineScope, isOnline: () -> Boolean) = PrayerSchedules(
        daily = cache(
            scope, isOnline, "daily", Bugungi::class.java, ScheduleDay::region, { it.date.toString() },
        ) { remote.getDailySchedule(it.region) },
        weekly = cache(
            scope, isOnline, "weekly", Birhaftalik::class.java, ScheduleDay::region, { it.date.toString() },
        ) { remote.getWeeklySchedule(it.region) },
        monthly = cache(
            scope, isOnline, "monthly", BirOylik::class.java, ScheduleMonth::region, { it.month.toString() },
        ) { remote.getMonthlySchedule(it.region, it.month) },
    )

    private fun <K : Any, T : Any> cache(
        scope: CoroutineScope,
        isOnline: () -> Boolean,
        bucket: String,
        type: Class<T>,
        regionOf: (K) -> Regions,
        periodOf: (K) -> String,
        request: suspend (K) -> T,
    ) = ScheduleCache(
        scope = scope,
        periodOf = periodOf,
        online = isOnline,
        restore = { key -> store.read("$bucket:${regionOf(key).slug}", periodOf(key), type) },
        persist = { key, entry -> store.write("$bucket:${regionOf(key).slug}", entry) },
        request = request,
    )
}
