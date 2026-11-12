package uz.turgunboyevjurabek.muslimapp.feature.presentation.View.Screens

import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.DataStorePreferencesViewModel.SelectRegionViewModel
import androidx.compose.animation.AnimatedContent
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.UIutils.textTransition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.core.utils.Status
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.PrayerTimesError
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.PrayerScheduleRows
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.ScheduleRegion
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.scheduleDate
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.rememberPrayerTimes
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.rememberPrayerClock
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.ScheduleRefresh
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.SavedScheduleLabel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.savedLabel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.PrayerScheduleViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.ScheduleMonth
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.shimmers.ShimmerPlaceholder

import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.UIutils.AnimatedValueText
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.Calendar.CalendarWidget
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.Calendar.CalendarViewModel
import uz.turgunboyevjurabek.muslimapp.feature.data.Calendar.CalendarDataSource
import java.time.YearMonth

@Composable
fun DayOf30Screen(
    selectRegionViewModel: SelectRegionViewModel,
    schedule: PrayerScheduleViewModel,
    bottomPadding: Dp,
    topPadding: Dp,
) {
    val calendarViewModel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val calendar by calendarViewModel.uiState.collectAsState()
    val selection by selectRegionViewModel.selection.collectAsState()
    val region = selection?.region
    val requestedMonth = calendar.yearMonth
    val connection by schedule.connection.collectAsState()
    val key = region?.let { ScheduleMonth(it, requestedMonth) }
    val load = rememberPrayerTimes(schedule.monthly, key, connection)
    val savedLabel = load.savedLabel(connection, requestedMonth.toString())
    val month = load.snapshot?.period?.let { runCatching { YearMonth.parse(it) }.getOrNull() } ?: requestedMonth
    val calendarDates = if (month == requestedMonth) calendar.dates else remember(month) { CalendarDataSource().getDates(month) }
    val refresh: () -> Unit = { key?.let { schedule.monthly.load(it, force = true) } }
    val now by rememberPrayerClock()
    val today = now.toLocalDate()
    var selectedDay by rememberSaveable(month.toString()) {
        mutableIntStateOf(if (YearMonth.from(today) == month) today.dayOfMonth else 1)
    }

    val ovalShape = RoundedCornerShape(36.dp)
    ScheduleRefresh(load, refresh, topPadding, bottomPadding) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, top = topPadding + 14.dp, bottom = 14.dp + bottomPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (region != null) {
                Column {
                    ScheduleRegion(region.nameRegion)
                    SavedScheduleLabel(savedLabel)
                }
            } else {
                ShimmerPlaceholder(Modifier.fillMaxWidth().height(44.dp), cornerRadius = 16.dp)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = ovalShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                CalendarWidget(
                    days = arrayOf("Du", "Se", "Ch", "Pa", "Ju", "Sh", "Ya"),
                    yearMonth = month,
                    dates = calendarDates.map { it.copy(isSelected = it.dayOfMonth.toIntOrNull() == selectedDay) },
                    onPreviousMonthButtonClicked = calendarViewModel::toPreviousMonth,
                    onNextMonthButtonClicked = calendarViewModel::toNextMonth,
                    onDateClickListener = { date -> date.dayOfMonth.toIntOrNull()?.let { selectedDay = it } },
                )
            }
            AnimatedContent(
                targetState = load.result to selectedDay,
                contentKey = { it.first.status },
                transitionSpec = { textTransition() },
                label = "Monthly prayer schedule",
            ) { (current, day) ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (current.status) {
                        Status.LOADING -> {
                            ShimmerPlaceholder(Modifier.width(116.dp).height(25.dp), cornerRadius = 8.dp)
                            ShimmerPlaceholder(
                                Modifier.fillMaxWidth().height(230.dp),
                                cornerRadius = 36.dp,
                                description = "Namoz vaqtlari yuklanmoqda",
                            )
                        }
                        Status.ERROR -> PrayerTimesError(current.message.orEmpty(), refresh)
                        Status.SUCCESS -> {
                            val entry = current.data?.firstOrNull { it.day == day }
                            if (entry == null) {
                                PrayerTimesError("Tanlangan kun uchun namoz vaqtlari topilmadi.", refresh)
                            } else {
                                AnimatedValueText(
                                    scheduleDate(entry.date),
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.titleLarge,
                                )
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = ovalShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                ) {
                                    Column(Modifier.padding(horizontal = 10.dp, vertical = 10.dp)) {
                                        val times = entry.times
                                        PrayerScheduleRows(
                                            times?.tongSaharlik, times?.quyosh, times?.peshin,
                                            times?.asr, times?.shomIftor, times?.hufton,
                                            rowHeight = 35.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
