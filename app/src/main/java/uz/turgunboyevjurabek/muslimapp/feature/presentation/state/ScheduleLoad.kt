package uz.turgunboyevjurabek.muslimapp.feature.presentation.state

import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.core.utils.Resource
import uz.turgunboyevjurabek.muslimapp.core.utils.UiMessage
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleState
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.SavedSchedule
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.ScheduleFailure

internal data class ScheduleLoad<T>(
    val result: Resource<T>,
    val requesting: Boolean,
    val refreshError: UiMessage?,
    val snapshot: SavedSchedule<T>?,
) {
    val refreshing: Boolean get() = requesting && snapshot != null
}

internal fun <T : Any> PrayerScheduleState<T>?.toScheduleLoad(): ScheduleLoad<T> {
    val snapshot = this?.snapshot
    val message = this?.failure?.toUiMessage()
    val result: Resource<T> = when {
        snapshot != null -> Resource.success(snapshot.data)
        message != null -> Resource.error(message)
        else -> Resource.loading(null)
    }
    return ScheduleLoad(
        result = result,
        requesting = this?.requesting == true,
        refreshError = message.takeIf { snapshot != null },
        snapshot = snapshot,
    )
}

private fun ScheduleFailure.toUiMessage() = when (this) {
    ScheduleFailure.Offline -> UiMessage(R.string.schedule_offline)
    ScheduleFailure.Network -> UiMessage(R.string.schedule_network_error)
    ScheduleFailure.Read -> UiMessage(R.string.schedule_read_error)
    ScheduleFailure.Save -> UiMessage(R.string.schedule_save_failed)
    is ScheduleFailure.Http -> UiMessage(R.string.schedule_http_error, code.toString())
}
