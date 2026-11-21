package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TasbexHistoryDatePicker(
    dates: List<LocalDate>,
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val selectableDates = remember(dates) {
        val savedDays = dates.map { it.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli() }.toSet()
        val savedYears = dates.map { it.year }.toSet()
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis in savedDays
            override fun isSelectableYear(year: Int) = year in savedYears
        }
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        yearRange = dates.minOf { it.year }..dates.maxOf { it.year },
        selectableDates = selectableDates,
    )
    val selectedDate = state.selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate()
    }
    val cancelInteraction = remember { MutableInteractionSource() }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(containerColor = colors.surfaceContainer),
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.pressScale(rememberPressScale(cancelInteraction)),
                interactionSource = cancelInteraction,
            ) {
                Text(stringResource(R.string.cancel), color = colors.onSurface)
            }
        },
        confirmButton = {
            TasbexDialogButton(stringResource(R.string.tasbih_history_open_date)) {
                selectedDate?.let(onSelect)
            }
        },
    ) {
        DatePicker(
            state = state,
            showModeToggle = false,
            title = {
                Text(
                    stringResource(R.string.tasbih_history_select_date),
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = colors.surfaceContainer,
                selectedDayContainerColor = colors.secondaryContainer,
                selectedDayContentColor = colors.onSecondaryContainer,
            ),
        )
    }
}
