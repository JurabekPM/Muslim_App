package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.calendar

import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.calendar.CalendarUiState

@Composable
fun Content(dates: List<CalendarUiState.Date>, onDateClickListener: (CalendarUiState.Date) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column {
        dates.chunked(7).forEach { week ->
            Row {
                repeat(7) { index ->
                    val date = week.getOrNull(index)
                    Box(Modifier.weight(1f).heightIn(min = 29.dp), contentAlignment = Alignment.Center) {
                        if (date != null && date.dayOfMonth.isNotEmpty()) {
                            val interactionSource = remember { MutableInteractionSource() }
                            Box(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 29.dp).selectable(
                                    selected = date.isSelected,
                                    interactionSource = interactionSource,
                                    indication = null,
                                    role = Role.Button,
                                    onClick = { onDateClickListener(date) },
                                ).pressScale(rememberPressScale(interactionSource)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier.sizeIn(minWidth = 27.dp, minHeight = 27.dp).background(
                                        color = when {
                                            date.isSelected -> colors.primary
                                            date.isToday -> colors.primaryContainer.copy(alpha = 0.45f)
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape,
                                    ).then(
                                        if (date.isToday && !date.isSelected) {
                                            Modifier.border(1.dp, colors.primary.copy(alpha = 0.55f), CircleShape)
                                        } else Modifier
                                    ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    AnimatedValueText(
                                        date.dayOfMonth,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (date.isSelected) colors.onPrimary else colors.onSurface,
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
