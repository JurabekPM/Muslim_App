package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TasbexHistorySheet(history: List<DhikrSession>, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val locale = LocalConfiguration.current.locales[0]
    val dateFormat = remember(locale) { DateFormat.getDateInstance(DateFormat.MEDIUM, locale) }
    val timeFormat = remember(locale) { DateFormat.getTimeInstance(DateFormat.SHORT, locale) }
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.surfaceContainer,
        contentColor = colors.onSurface,
        tonalElevation = 0.dp,
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(max = minOf(maxHeight * 0.84f, 620.dp))
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.dhikr_history),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                if (history.isEmpty()) {
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
                    LazyColumn(Modifier.weight(1f, fill = false)) {
                        itemsIndexed(history) { index, session ->
                            val date = Date(session.finishedAtMillis)
                            HistoryEntry(
                                date = dateFormat.format(date),
                                time = timeFormat.format(date),
                                count = numberFormat.format(session.count),
                                first = index == 0,
                                last = index == history.lastIndex,
                            )
                        }
                    }
                }
                TasbexDialogButton(
                    label = stringResource(R.string.close),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            state.hide()
                            if (!state.isVisible) onDismiss()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryEntry(
    date: String,
    time: String,
    count: String,
    first: Boolean,
    last: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    val markerColor = colors.tertiary
    val lineColor = colors.outlineVariant
    Row(
        modifier = Modifier.fillMaxWidth()
            .drawWithCache {
                val markerX = if (layoutDirection == LayoutDirection.Ltr) {
                    5.dp.toPx()
                } else {
                    size.width - 5.dp.toPx()
                }
                val markerY = 18.dp.toPx()
                val dash = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
                onDrawBehind {
                    drawLine(
                        color = lineColor,
                        start = Offset(markerX, if (first) markerY else 0f),
                        end = Offset(markerX, if (last) markerY else size.height),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dash,
                    )
                    drawCircle(markerColor, radius = 4.dp.toPx(), center = Offset(markerX, markerY))
                }
            }
            .padding(start = 24.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(date, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(time, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Text(
            count,
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
internal fun TasbexResetDialog(count: Int, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val locale = LocalConfiguration.current.locales[0]
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = colors.surfaceContainer,
        tonalElevation = 0.dp,
        title = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(40.dp).background(colors.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_tasbex_reset),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = colors.onSecondaryContainer,
                    )
                }
                Text(
                    stringResource(R.string.reset_count),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(colors.surfaceContainerHigh, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.tasbih_current_count),
                        modifier = Modifier.weight(1f),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        numberFormat.format(count),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.End,
                    )
                }
                Text(
                    stringResource(R.string.tasbih_reset_message),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        dismissButton = {
            val interaction = remember { MutableInteractionSource() }
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.pressScale(rememberPressScale(interaction)).heightIn(min = 48.dp),
                interactionSource = interaction,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, colors.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TasbexDialogButton(stringResource(R.string.tasbih_reset_short), onClick = onConfirm)
        },
    )
}

@Composable
private fun TasbexDialogButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier.pressScale(rememberPressScale(interaction)).heightIn(min = 48.dp),
        interactionSource = interaction,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
        ),
    ) {
        Text(label, textAlign = TextAlign.Center)
    }
}
