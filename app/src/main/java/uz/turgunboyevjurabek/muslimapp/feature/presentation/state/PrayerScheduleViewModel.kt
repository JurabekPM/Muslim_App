package uz.turgunboyevjurabek.muslimapp.feature.presentation.state

import uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation.AppRoute
import uz.turgunboyevjurabek.muslimapp.core.utils.UiMessage
import uz.turgunboyevjurabek.muslimapp.R
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import uz.turgunboyevjurabek.muslimapp.core.utils.Resource
import uz.turgunboyevjurabek.muslimapp.feature.data.network.InternetConnection
import uz.turgunboyevjurabek.muslimapp.feature.data.network.InternetMonitor
import uz.turgunboyevjurabek.muslimapp.feature.data.local.PrayerScheduleStore
import uz.turgunboyevjurabek.muslimapp.feature.data.local.SavedSchedule
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly.Birhaftalik
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly.BirOylik
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleRepository
import java.io.IOException
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

internal val prayerZone: ZoneId = ZoneId.of("Asia/Tashkent")
internal data class ScheduleDay(val region: Regions, val date: LocalDate)
internal data class ScheduleMonth(val region: Regions, val month: YearMonth)

@HiltViewModel
class PrayerScheduleViewModel @Inject constructor(
    repository: PrayerScheduleRepository,
    private val store: PrayerScheduleStore,
    internet: InternetMonitor,
) : ViewModel() {
    val connection = internet.observe().stateIn(viewModelScope, SharingStarted.Eagerly, InternetConnection.Connecting)

    internal val daily = cache(
        "daily", Bugungi::class.java, ScheduleDay::region, { it.date.toString() },
    ) { repository.getDailySchedule(it.region) }
    internal val weekly = cache(
        "weekly", Birhaftalik::class.java, ScheduleDay::region, { it.date.toString() },
    ) { repository.getWeeklySchedule(it.region) }
    internal val monthly = cache(
        "monthly", BirOylik::class.java, ScheduleMonth::region, { it.month.toString() },
    ) { repository.getMonthlySchedule(it.region, it.month) }

    fun isRefreshing(route: String?): Boolean = when (route) {
        AppRoute.HOME, null -> daily.isRefreshing
        AppRoute.WEEKLY -> weekly.isRefreshing
        AppRoute.MONTHLY -> monthly.isRefreshing
        else -> false
    }

    private fun <K : Any, T : Any> cache(
        name: String,
        type: Class<T>,
        region: (K) -> Regions,
        period: (K) -> String,
        request: suspend (K) -> T,
    ) = ScheduleCache(
        scope = viewModelScope,
        periodOf = period,
        online = { connection.value == InternetConnection.Online },
        restore = { key -> store.read("$name:${region(key).slug}", period(key), type) },
        persist = { key, entry -> store.write("$name:${region(key).slug}", entry) },
        request = request,
    )
}

internal data class ScheduleLoad<T>(
    val result: Resource<T> = Resource.loading(null),
    val requesting: Boolean = false,
    val refreshError: UiMessage? = null,
    val snapshot: SavedSchedule<T>? = null,
    val attemptedOnline: Boolean = false,
) {
    val refreshing: Boolean get() = requesting && result.data != null
}

/** Accessed from the main thread. Requests continue in the activity ViewModel when tabs change. */
internal class ScheduleCache<K : Any, T : Any>(
    private val scope: CoroutineScope,
    private val periodOf: (K) -> String = { "" },
    private val online: () -> Boolean = { true },
    private val restore: suspend (K) -> SavedSchedule<T>? = { null },
    private val persist: suspend (K, SavedSchedule<T>) -> Unit = { _, _ -> },
    private val request: suspend (K) -> T,
) {
    private val entries = mutableStateMapOf<K, ScheduleLoad<T>>()
    private val order = LinkedHashSet<K>()
    private val initial = ScheduleLoad<T>()
    val isRefreshing: Boolean get() = entries.values.any { it.refreshing }

    operator fun get(key: K?): ScheduleLoad<T> = if (key == null) initial else entries[key] ?: initial

    fun load(key: K, force: Boolean = false) {
        val previous = entries[key]
        order.remove(key)
        order.add(key)
        if (previous?.requesting == true) return
        val period = periodOf(key)
        val samePeriod = previous?.snapshot?.period == period
        if (!force && previous != null && (samePeriod || previous.attemptedOnline || !online())) return

        var retained = previous?.snapshot
        entries[key] = retained.asLoad(requesting = true)
        scope.launch {
            var attempted = false
            try {
                if (retained == null) retained = restore(key)
                if (!online() || !force && retained?.period == period) {
                    entries[key] = retained?.asLoad() ?: ScheduleLoad(
                        Resource.error(UiMessage(R.string.schedule_offline)),
                    )
                    return@launch
                }

                attempted = true
                entries[key] = retained.asLoad(requesting = true)
                val saved = SavedSchedule(request(key), period, System.currentTimeMillis())
                val saveError = try {
                    persist(key, saved)
                    null
                } catch (_: IOException) {
                    UiMessage(R.string.schedule_save_failed)
                }
                entries[key] = saved.asLoad(attemptedOnline = true).copy(refreshError = saveError)
            } catch (cancelled: CancellationException) {
                if (previous == null) {
                    entries.remove(key)
                    order.remove(key)
                } else {
                    entries[key] = previous
                }
                throw cancelled
            } catch (error: Exception) {
                val message = when (error) {
                    is HttpException -> UiMessage(R.string.schedule_http_error, error.code().toString())
                    is IOException -> UiMessage(R.string.schedule_network_error)
                    else -> UiMessage(R.string.schedule_read_error)
                }
                entries[key] = retained?.asLoad(attemptedOnline = attempted)?.copy(refreshError = message)
                    ?: ScheduleLoad(Resource.error(message), attemptedOnline = attempted)
            } finally {
                while (order.size > 24) {
                    val oldest = order.firstOrNull { it != key && entries[it]?.requesting == false } ?: break
                    order.remove(oldest)
                    entries.remove(oldest)
                }
            }
        }
    }

    private fun SavedSchedule<T>?.asLoad(requesting: Boolean = false, attemptedOnline: Boolean = false) =
        ScheduleLoad(
            result = if (this == null) Resource.loading(null) else Resource.success(data),
            requesting = requesting,
            snapshot = this,
            attemptedOnline = attemptedOnline,
        )
}
