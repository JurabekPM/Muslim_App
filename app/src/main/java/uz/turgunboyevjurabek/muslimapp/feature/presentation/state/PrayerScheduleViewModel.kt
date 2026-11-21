package uz.turgunboyevjurabek.muslimapp.feature.presentation.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uz.turgunboyevjurabek.muslimapp.feature.data.network.InternetConnection
import uz.turgunboyevjurabek.muslimapp.feature.data.network.InternetMonitor
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.PrayerScheduleSource
import uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation.AppRoute
import java.time.ZoneId
import javax.inject.Inject

internal val prayerZone: ZoneId = ZoneId.of("Asia/Tashkent")

@HiltViewModel
class PrayerScheduleViewModel @Inject constructor(
    repository: PrayerScheduleRepository,
    internet: InternetMonitor,
) : ViewModel() {
    val connection = internet.observe().stateIn(viewModelScope, SharingStarted.Eagerly, InternetConnection.Connecting)

    private val schedules = repository.schedules(viewModelScope) { connection.value == InternetConnection.Online }
    internal val daily = schedules.daily
    internal val weekly = schedules.weekly
    internal val monthly = schedules.monthly

    private val dailyRefreshing = daily.refreshing()
    private val weeklyRefreshing = weekly.refreshing()
    private val monthlyRefreshing = monthly.refreshing()

    internal fun refreshing(route: String?) = when (route) {
        AppRoute.HOME, null -> dailyRefreshing
        AppRoute.WEEKLY -> weeklyRefreshing
        AppRoute.MONTHLY -> monthlyRefreshing
        else -> null
    }

    private fun PrayerScheduleSource<*, *>.refreshing() = states
        .map { entries -> entries.values.any { it.refreshing } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}
