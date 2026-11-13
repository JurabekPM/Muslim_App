package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.messageText
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.scheduleWeekday
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.displayName
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.SelectRegionViewModel
import androidx.compose.animation.AnimatedContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.textTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.core.utils.Status
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.PrayerTimesError
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.PrayerScheduleRows
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.ScheduleRegion
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.scheduleDate
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPrayerTimes
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPrayerClock
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.ScheduleRefresh
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.SavedScheduleLabel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.savedLabel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.PrayerScheduleViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.ScheduleDay
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.DayOf7ShimmerList
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly.BirhaftalikItem
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.ScheduleCardColor

@Composable
fun Dayof7Screen(
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
    val load = rememberPrayerTimes(schedule.weekly, key, connection)
    val savedLabel = load.savedLabel(connection, now.toLocalDate().toString())
    val refresh: () -> Unit = { key?.let { schedule.weekly.load(it, force = true) } }

    ScheduleRefresh(load, refresh, topPadding, bottomPadding) {
        AnimatedContent(
            targetState = load.result to region,
            contentKey = { it.first.status },
            transitionSpec = { textTransition() },
            label = "Weekly prayer schedule",
        ) { (current, displayedRegion) ->
            when (current.status) {
                Status.LOADING -> DayOf7ShimmerList(
                    Modifier.padding(top = topPadding + 12.dp, start = 16.dp, end = 16.dp),
                )
                Status.ERROR -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = topPadding)) {
                    PrayerTimesError(messageText(current.message), refresh)
                }
                Status.SUCCESS -> {
                    if (displayedRegion == null) return@AnimatedContent
                    val days = current.data.orEmpty()
                    var expandedDate by rememberSaveable(displayedRegion.slug, days.firstOrNull()?.date) {
                        mutableStateOf(days.firstOrNull()?.date)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = topPadding + 12.dp,
                            bottom = 12.dp + bottomPadding,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            val range = listOfNotNull(days.firstOrNull()?.date, days.lastOrNull()?.date)
                                .map { scheduleDate(it) }.joinToString(" – ")
                            ScheduleRegion(displayedRegion.displayName(), range)
                            SavedScheduleLabel(savedLabel)
                            Spacer(Modifier.height(4.dp))
                        }
                        items(days, key = { it.date.orEmpty() }) { day ->
                            WeekDayCard(day, expanded = day.date == expandedDate) {
                                expandedDate = if (day.date == expandedDate) null else day.date
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekDayCard(day: BirhaftalikItem, expanded: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val arrow by animateFloatAsState(if (expanded) 180f else 0f, label = "Week day arrow")
    val colors = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(18.dp), color = colors.surfaceContainerLow) {
        Column {
            Surface(
                color = if (expanded) ScheduleCardColor else colors.surfaceContainerLow,
                contentColor = if (expanded) Color.White else colors.onSurface,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = if (expanded) "Vaqtlarni yopish" else "Vaqtlarni ochish",
                        onClick = onClick,
                    ).pressScale(rememberPressScale(interactionSource)).padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        AnimatedValueText(scheduleDate(day.date), style = MaterialTheme.typography.titleMedium)
                        AnimatedValueText(scheduleWeekday(day.date), style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_down),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp).rotate(arrow),
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                val times = day.times
                PrayerScheduleRows(
                    times?.tongSaharlik, times?.quyosh, times?.peshin,
                    times?.asr, times?.shomIftor, times?.hufton,
                    rowHeight = 36.dp,
                )
            }
        }
    }
}
