package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import uz.turgunboyevjurabek.muslimapp.R
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.Instant
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import uz.turgunboyevjurabek.muslimapp.feature.data.network.InternetConnection
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.ScheduleLoad
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.prayerZone
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.ScheduleCardColor
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.ScheduleCardContent

@Composable
internal fun ScheduleRegion(region: String, dateRange: String = "") {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ScheduleCardColor,
        contentColor = Color.White,
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            AnimatedValueText(region, style = MaterialTheme.typography.titleMedium)
            if (dateRange.isNotBlank()) {
                AnimatedValueText(dateRange, style = MaterialTheme.typography.bodySmall, color = ScheduleCardContent)
            }
        }
    }
}

private val apiDate = DateTimeFormatter.ofPattern("dd.MM.uuuu", Locale.ROOT)
@Composable
internal fun scheduleDate(value: String?): String {
    val date = value?.let { LocalDate.parse(it, apiDate) } ?: return ""
    return date.displayDate()
}

@Composable
private fun LocalDate.displayDate(): String {
    val monthNames = if (appLocale().language == "ru") R.array.date_month_names else R.array.calendar_month_names
    return stringResource(R.string.schedule_date, dayOfMonth, stringArrayResource(monthNames)[monthValue - 1])
}

@Composable
internal fun scheduleMonth(month: YearMonth): String =
    "${stringArrayResource(R.array.calendar_month_names)[month.monthValue - 1]} ${month.year}"

@Composable
internal fun scheduleWeekday(value: String?): String {
    val date = value?.let { LocalDate.parse(it, apiDate) } ?: return ""
    return stringArrayResource(R.array.weekday_names)[date.dayOfWeek.ordinal]
}

@Composable
internal fun ScheduleLoad<*>.savedLabel(connection: InternetConnection, requestedPeriod: String): String? {
    val saved = snapshot ?: return null
    if (connection == InternetConnection.Online && saved.period == requestedPeriod && refreshError == null) return null
    val savedTime = Instant.ofEpochMilli(saved.savedAt).atZone(prayerZone)
    val time = savedTime.format(DateTimeFormatter.ofPattern("HH:mm", appLocale()))
    return stringResource(R.string.saved_schedule, "${savedTime.toLocalDate().displayDate()}, $time")
}

@Composable
internal fun SavedScheduleLabel(label: String?) {
    AnimatedContent(
        targetState = label,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "Saved schedule time",
    ) { current ->
        if (current != null) {
            Text(
                current,
                modifier = Modifier.padding(start = 4.dp, top = 5.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
