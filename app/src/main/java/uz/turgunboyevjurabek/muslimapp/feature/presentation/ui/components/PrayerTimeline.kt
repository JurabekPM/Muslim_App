package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
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
            listOfNotNull(region, date?.let { scheduleDate(it) }).joinToString(" · "),
            color = color,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
internal fun PrayerTimeline(
    slots: List<Pair<String, String?>>,
    currentIndex: Int,
) {
    val colors = MaterialTheme.colorScheme
    val scheduleDescription = stringResource(R.string.prayer_schedule)
    val markers = remember { mutableStateMapOf<Int, Float>() }
    Column(
        Modifier.fillMaxWidth()
            .semantics { contentDescription = scheduleDescription }
            .drawBehind {
                val start = markers[0] ?: return@drawBehind
                val end = markers[slots.lastIndex] ?: return@drawBehind
                val inset = 12.dp.toPx()
                val x = if (layoutDirection == LayoutDirection.Ltr) inset else size.width - inset
                drawLine(colors.outlineVariant, Offset(x, start), Offset(x, end), strokeWidth = 1.5.dp.toPx())

                slots.indices.forEach { index ->
                    val center = Offset(x, markers[index] ?: return@forEach)
                    val radius = if (index == currentIndex) 6.5.dp.toPx() else 5.5.dp.toPx()
                    if (index == currentIndex) {
                        drawCircle(colors.tertiary, radius, center)
                    } else {
                        drawCircle(colors.background, radius, center)
                        drawCircle(colors.outline, radius, center, style = Stroke(1.5.dp.toPx()))
                    }
                }
            },
    ) {
        slots.forEachIndexed { index, (name, time) ->
            key(name) {
                val current = index == currentIndex
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
                                prayerName(name),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (current) FontWeight.Bold else FontWeight.SemiBold,
                            )
                            AnimatedValueText(
                                time ?: "—",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (current) FontWeight.Bold else FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}
