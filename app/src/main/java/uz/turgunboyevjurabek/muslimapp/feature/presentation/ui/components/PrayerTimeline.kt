package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.PrayerInterval
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.caption
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText

@Composable
internal fun PrayerLocationDate(region: String?, date: String?) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_qibla_location),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = color,
        )
        AnimatedValueText(
            listOfNotNull(region, date?.let(::scheduleDate)).joinToString(" · "),
            color = color,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
internal fun PrayerTimeline(
    slots: List<Pair<String, String?>>,
    currentIndex: Int,
    nextIndex: Int,
    interval: PrayerInterval?,
) {
    val colors = MaterialTheme.colorScheme
    val markers = remember { mutableStateMapOf<Int, Float>() }
    val fraction = remember(currentIndex, nextIndex) { Animatable(0f) }
    val glow = remember(currentIndex, nextIndex) { Animatable(0f) }
    LaunchedEffect(interval?.fraction, fraction) {
        if (interval != null) {
            coroutineScope {
                launch { fraction.animateTo(interval.fraction, tween(650)) }
                launch {
                    glow.snapTo(0.72f)
                    glow.animateTo(0f, tween(780))
                }
            }
        }
    }

    Column(
        Modifier.fillMaxWidth()
            .semantics { contentDescription = "Namoz jadvali" }
            .drawBehind {
                val start = markers[0] ?: return@drawBehind
                val end = markers[slots.lastIndex] ?: return@drawBehind
                val inset = 12.dp.toPx()
                val x = if (layoutDirection == LayoutDirection.Ltr) inset else size.width - inset
                drawLine(colors.outlineVariant, Offset(x, start), Offset(x, end), strokeWidth = 1.dp.toPx())

                val from = markers[currentIndex]
                val until = markers[nextIndex]
                if (interval != null && from != null && until != null && until > from && fraction.value > 0f) {
                    val tip = Offset(x, from + (until - from) * fraction.value)
                    drawLine(colors.tertiary, Offset(x, from), tip, 3.dp.toPx(), StrokeCap.Round)
                    val radius = 8.dp.toPx()
                    if (glow.value > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(colors.tertiary.copy(alpha = glow.value), colors.tertiary.copy(alpha = 0f)),
                                center = tip,
                                radius = radius,
                            ),
                            radius = radius,
                            center = tip,
                        )
                    }
                    drawCircle(colors.tertiary, 2.dp.toPx(), tip)
                }

                slots.indices.forEach { index ->
                    val center = Offset(x, markers[index] ?: return@forEach)
                    val radius = 4.dp.toPx()
                    if (index == currentIndex) {
                        drawCircle(colors.tertiary, radius, center)
                    } else {
                        drawCircle(colors.background, radius, center)
                        drawCircle(colors.outline, radius, center, style = Stroke(1.dp.toPx()))
                    }
                }
            },
    ) {
        slots.forEachIndexed { index, (name, time) ->
            key(name) {
                val current = index == currentIndex
                val nextName = slots.getOrNull(nextIndex)?.first
                Box(
                    Modifier.fillMaxWidth().onPlaced { coordinates ->
                        markers[index] = coordinates.positionInParent().y + coordinates.size.height / 2f
                    }.padding(start = 25.dp),
                ) {
                    Column(
                        Modifier.fillMaxWidth()
                            .background(
                                if (current) colors.secondaryContainer.copy(alpha = 0.24f) else Color.Transparent,
                                RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 9.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 43.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (current) FontWeight.SemiBold else FontWeight.Medium,
                            )
                            AnimatedValueText(
                                time ?: "—",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (current) FontWeight.SemiBold else FontWeight.Medium,
                            )
                        }
                        if (current && interval != null && nextName != null) {
                            AnimatedValueText(
                                interval.caption(nextName),
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = colors.tertiary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}
