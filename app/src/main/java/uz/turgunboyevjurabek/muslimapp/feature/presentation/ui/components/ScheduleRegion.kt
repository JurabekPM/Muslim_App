package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

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
private val displayDate = DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("uz-Latn-UZ"))

internal fun scheduleDate(value: String?): String =
    value?.let { LocalDate.parse(it, apiDate).format(displayDate) }.orEmpty()

private val savedTime = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale.forLanguageTag("uz-Latn-UZ"))
    .withZone(prayerZone)

internal fun ScheduleLoad<*>.savedLabel(connection: InternetConnection, requestedPeriod: String): String? {
    val saved = snapshot ?: return null
    if (connection == InternetConnection.Online && saved.period == requestedPeriod && refreshError == null) return null
    return "Saqlangan jadval · ${savedTime.format(Instant.ofEpochMilli(saved.savedAt))}"
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
