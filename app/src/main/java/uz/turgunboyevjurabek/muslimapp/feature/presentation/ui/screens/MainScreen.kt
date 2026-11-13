package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.messageText
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.displayName
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.SelectRegionViewModel
import androidx.compose.animation.AnimatedContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.textTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.core.utils.Status
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.PrayerTimesError
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.FeaturedPrayerCard
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.PrayerTimeline
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.PrayerLocationDate
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPrayerTimes
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPrayerClock
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.ScheduleRefresh
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.SavedScheduleLabel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.savedLabel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.PrayerScheduleViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.ScheduleDay
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.prayerInterval
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.MainCardShimmer
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.TimeListCardShimmer

@Composable
fun MainScreen(
    selectRegionViewModel: SelectRegionViewModel,
    schedule: PrayerScheduleViewModel,
    bottomPadding: Dp,
    topPadding: Dp,
) {
    val selection by selectRegionViewModel.selection.collectAsStateWithLifecycle()
    val region = selection?.region
    val now by rememberPrayerClock()
    val connection by schedule.connection.collectAsStateWithLifecycle()
    val key = region?.let { ScheduleDay(it, now.toLocalDate()) }
    val load = rememberPrayerTimes(schedule.daily, key, connection)
    val requestedPeriod = now.toLocalDate().toString()
    val savedLabel = load.savedLabel(connection, requestedPeriod)
    val refresh: () -> Unit = { key?.let { schedule.daily.load(it, force = true) } }

    ScheduleRefresh(load, refresh, topPadding, bottomPadding) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = topPadding + 12.dp, bottom = 12.dp + bottomPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AnimatedContent(
                targetState = load.result to load.snapshot?.period,
                contentKey = { it.first.status },
                transitionSpec = { textTransition() },
                label = "Daily prayer schedule",
            ) { (current, period) ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    when (current.status) {
                        Status.LOADING -> {
                            MainCardShimmer()
                            TimeListCardShimmer()
                        }
                        Status.ERROR -> PrayerTimesError(messageText(current.message), refresh)
                        Status.SUCCESS -> current.data?.let { today ->
                            val currentDay = period == requestedPeriod
                            val slots = remember(today.times) { today.prayerSlots() }
                            val clock = now.toLocalTime().toString().take(5)
                            val currentIndex = if (currentDay) {
                                slots.indexOfLast { (_, time) -> time != null && time <= clock }
                            } else -1
                            val nextIndex = if (currentDay) {
                                slots.indexOfFirst { (_, time) -> time != null && time > clock }
                            } else -1
                            val featured = slots.getOrNull(if (currentIndex >= 0) currentIndex else nextIndex)
                                ?.let { (name, time) -> today.copy(featuredPrayer = name, featuredTime = time) }
                                ?: today
                            val interval = prayerInterval(
                                slots.getOrNull(currentIndex)?.second,
                                slots.getOrNull(nextIndex)?.second,
                                now.toLocalTime(),
                            )
                            Column {
                                FeaturedPrayerCard(featured, interval, slots.getOrNull(nextIndex)?.first)
                                Spacer(Modifier.height(12.dp))
                                PrayerLocationDate(region?.displayName() ?: today.region, today.date)
                                SavedScheduleLabel(savedLabel)
                            }
                            PrayerTimeline(slots, currentIndex)
                        }
                    }
                }
            }
        }
    }
}

private fun Bugungi.prayerSlots(): List<Pair<String, String?>> = listOf(
    "Bomdod" to times?.tongSaharlik,
    "Quyosh" to times?.quyosh,
    "Peshin" to times?.peshin,
    "Asr" to times?.asr,
    "Shom" to times?.shomIftor,
    "Xufton" to times?.hufton,
)
