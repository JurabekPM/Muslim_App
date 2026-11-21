package uz.turgunboyevjurabek.muslimapp.feature.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleSource
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleState
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.SavedSchedule
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.ScheduleFailure
import java.io.IOException

/** Accessed from the main thread. Requests stay active when the user switches tabs. */
internal class ScheduleCache<K : Any, T : Any>(
    private val scope: CoroutineScope,
    private val periodOf: (K) -> String,
    private val online: () -> Boolean,
    private val restore: suspend (K) -> SavedSchedule<T>?,
    private val persist: suspend (K, SavedSchedule<T>) -> Unit,
    private val request: suspend (K) -> T,
) : PrayerScheduleSource<K, T> {
    private val entries = MutableStateFlow<Map<K, PrayerScheduleState<T>>>(emptyMap())
    private val order = LinkedHashSet<K>()
    override val states = entries.asStateFlow()

    override fun load(key: K, force: Boolean) {
        val previous = entries.value[key]
        order.remove(key)
        order.add(key)
        if (previous?.requesting == true) return
        val period = periodOf(key)
        val samePeriod = previous?.snapshot?.period == period
        if (!force && previous != null && (samePeriod || previous.attemptedOnline || !online())) return

        var retained = previous?.snapshot
        publish(key, PrayerScheduleState(snapshot = retained, requesting = true))
        scope.launch {
            var attempted = false
            try {
                if (retained == null) retained = restore(key)
                if (!online() || !force && retained?.period == period) {
                    val failure = if (retained == null) ScheduleFailure.Offline else null
                    publish(key, PrayerScheduleState(snapshot = retained, failure = failure))
                    return@launch
                }

                attempted = true
                publish(key, PrayerScheduleState(snapshot = retained, requesting = true))
                val saved = SavedSchedule(request(key), period, System.currentTimeMillis())
                val saveError = try {
                    persist(key, saved)
                    null
                } catch (_: IOException) {
                    ScheduleFailure.Save
                }
                publish(key, PrayerScheduleState(snapshot = saved, failure = saveError, attemptedOnline = true))
            } catch (cancelled: CancellationException) {
                if (previous == null) {
                    entries.value = entries.value - key
                    order.remove(key)
                } else {
                    publish(key, previous)
                }
                throw cancelled
            } catch (error: Exception) {
                val failure = when (error) {
                    is HttpException -> ScheduleFailure.Http(error.code())
                    is IOException -> ScheduleFailure.Network
                    else -> ScheduleFailure.Read
                }
                publish(key, PrayerScheduleState(snapshot = retained, failure = failure, attemptedOnline = attempted))
            } finally {
                while (order.size > 24) {
                    val oldest = order.firstOrNull { it != key && entries.value[it]?.requesting == false } ?: break
                    order.remove(oldest)
                    entries.value = entries.value - oldest
                }
            }
        }
    }

    private fun publish(key: K, state: PrayerScheduleState<T>) {
        entries.value = entries.value + (key to state)
    }
}
