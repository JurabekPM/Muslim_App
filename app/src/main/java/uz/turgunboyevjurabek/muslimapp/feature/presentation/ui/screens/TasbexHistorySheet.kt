package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.DhikrSession
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale
import java.text.DateFormat
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TasbexHistorySheet(history: List<DhikrSession>, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val sheet = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    val timeFormat = remember(locale, zone) { DateFormat.getTimeInstance(DateFormat.SHORT, locale) }
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    val monthNames = stringArrayResource(R.array.calendar_month_names)
    val dateMonthNames = stringArrayResource(R.array.date_month_names)
    val months = remember(history, zone) { history.groupIntoMonths(zone).entries.toList() }
    val newestMonth = months.firstOrNull()?.key?.toString()
    var expandedMonth by rememberSaveable(newestMonth) { mutableStateOf(newestMonth) }
    var focusedDay by rememberSaveable { mutableStateOf<String?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheet,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.surfaceContainer,
        contentColor = colors.onSurface,
        tonalElevation = 0.dp,
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(max = minOf(maxHeight * 0.84f, 620.dp))
                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                HistoryHeader(hasHistory = months.isNotEmpty(), onPickDate = { showDatePicker = true })
                if (months.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_dhikr_history),
                        modifier = Modifier.fillMaxWidth()
                            .background(colors.surfaceContainerHigh, RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        state = listState,
                    ) {
                        months.forEachIndexed { monthIndex, (month, days) ->
                            val monthKey = month.toString()
                            val expanded = expandedMonth == monthKey
                            item(key = "month-$monthKey", contentType = "month") {
                                val monthName = monthNames[month.monthValue - 1]
                                    .replaceFirstChar { it.titlecase(locale) }
                                HistoryMonthRow(
                                    title = "$monthName ${month.year}",
                                    count = remember(days) { days.values.sumOf { it.size } },
                                    expanded = expanded,
                                    modifier = Modifier.animateItem().padding(bottom = 6.dp),
                                    onClick = {
                                        expandedMonth = if (expanded) null else monthKey
                                        focusedDay = null
                                        if (!expanded && monthIndex < listState.firstVisibleItemIndex) {
                                            listState.requestScrollToItem(monthIndex)
                                        }
                                    },
                                )
                            }
                            if (expanded) days.forEach { (date, sessions) ->
                                item(key = "day-$date", contentType = "day") {
                                    val focused = focusedDay == date.toString()
                                    Text(
                                        text = stringResource(
                                            R.string.schedule_date,
                                            date.dayOfMonth,
                                            dateMonthNames[date.monthValue - 1],
                                        ),
                                        modifier = Modifier.animateItem().semantics { heading() }
                                            .padding(start = 12.dp, top = 6.dp, bottom = 2.dp),
                                        color = if (focused) colors.tertiary else colors.onSurface,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                itemsIndexed(
                                    items = sessions,
                                    key = { index, _ -> "session-$date-$index" },
                                    contentType = { _, _ -> "session" },
                                ) { index, session ->
                                    HistoryEntry(
                                        time = timeFormat.format(Date(session.finishedAtMillis)),
                                        count = numberFormat.format(session.count),
                                        first = index == 0,
                                        last = index == sessions.lastIndex,
                                        modifier = Modifier.animateItem(),
                                    )
                                }
                            }
                        }
                    }
                }
                TasbexDialogButton(
                    label = stringResource(R.string.close),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            sheet.hide()
                            if (!sheet.isVisible) onDismiss()
                        }
                    },
                )
            }
        }
        if (showDatePicker && months.isNotEmpty()) {
            val dates = remember(months) { months.flatMap { it.value.keys } }
            val initialDate = dates.firstOrNull { it.toString() == focusedDay }
                ?: months.firstOrNull { it.key.toString() == expandedMonth }?.value?.keys?.first()
                ?: dates.first()
            TasbexHistoryDatePicker(dates, initialDate, onDismiss = { showDatePicker = false }) { date ->
                val month = YearMonth.from(date)
                val monthIndex = months.indexOfFirst { it.key == month }
                val precedingDayRows = months[monthIndex].value.entries.takeWhile { it.key != date }
                    .sumOf { 1 + it.value.size }
                expandedMonth = month.toString()
                focusedDay = date.toString()
                showDatePicker = false
                listState.requestScrollToItem(monthIndex + 1 + precedingDayRows)
            }
        }
    }
}

@Composable
private fun HistoryHeader(hasHistory: Boolean, onPickDate: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.dhikr_history),
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (hasHistory) {
            val interaction = remember { MutableInteractionSource() }
            OutlinedButton(
                onClick = onPickDate,
                modifier = Modifier.weight(1f, fill = false).heightIn(min = 48.dp)
                    .pressScale(rememberPressScale(interaction)),
                interactionSource = interaction,
                border = BorderStroke(1.dp, colors.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.ic_calendar), null, Modifier.size(18.dp))
                    Text(stringResource(R.string.tasbih_history_select_date), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun HistoryMonthRow(
    title: String,
    count: Int,
    expanded: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val color by animateColorAsState(
        if (expanded) lerp(colors.surfaceContainerHigh, colors.tertiaryContainer, 0.4f)
        else colors.surfaceContainerHigh,
        label = "History month background",
    )
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "History month arrow")
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp)
            .pressScale(rememberPressScale(interaction))
            .clip(RoundedCornerShape(14.dp)).background(color)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .semantics { selected = expanded }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        Text(
            pluralStringResource(R.plurals.tasbih_history_entries, count, count),
            color = colors.tertiary,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
        )
        Icon(painterResource(R.drawable.ic_chevron_down), null, Modifier.size(20.dp).rotate(rotation))
    }
}

@Composable
private fun HistoryEntry(time: String, count: String, first: Boolean, last: Boolean, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val marker = colors.tertiary
    val line = colors.outlineVariant
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp)
            .drawBehind {
                val x = if (layoutDirection == LayoutDirection.Ltr) 4.dp.toPx() else size.width - 4.dp.toPx()
                drawLine(
                    color = line,
                    start = Offset(x, if (first) center.y else 0f),
                    end = Offset(x, if (last) center.y else size.height),
                    strokeWidth = 1.dp.toPx(),
                )
                drawCircle(marker, radius = 3.5.dp.toPx(), center = Offset(x, center.y))
            }
            .padding(start = 20.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(time, Modifier.weight(1f), color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(
            count,
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
    }
}

private fun List<DhikrSession>.groupIntoMonths(zone: ZoneId): Map<YearMonth, Map<LocalDate, List<DhikrSession>>> =
    sortedByDescending { it.finishedAtMillis }
        .groupBy { Instant.ofEpochMilli(it.finishedAtMillis).atZone(zone).toLocalDate() }
        .entries.groupBy { YearMonth.from(it.key) }
        .mapValues { (_, days) -> days.associate { it.key to it.value } }
