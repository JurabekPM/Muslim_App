package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.textTransition

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
internal fun PrayerTimeline(slots: List<Pair<String, String?>>, currentIndex: Int, nextIndex: Int) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().semantics { contentDescription = "Namoz jadvali" }) {
        slots.forEachIndexed { index, (name, time) ->
            val current = index == currentIndex
            val next = index == nextIndex
            val dot by animateColorAsState(
                if (current) colors.tertiary else colors.outline,
                label = "Prayer timeline marker",
            )
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 43.dp)
                    .drawBehind {
                        val inset = 12.dp.toPx()
                        val x = if (layoutDirection == LayoutDirection.Ltr) inset else size.width - inset
                        val middle = size.height / 2f
                        drawLine(
                            color = colors.outlineVariant,
                            start = Offset(x, if (index == 0) middle else 0f),
                            end = Offset(x, if (index == slots.lastIndex) middle else size.height),
                            strokeWidth = 1.dp.toPx(),
                        )
                        drawCircle(dot, radius = 4.dp.toPx(), center = Offset(x, middle))
                    }
                    .padding(start = 30.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (current) FontWeight.Bold else FontWeight.Medium,
                    )
                    AnimatedContent(
                        targetState = when {
                            current -> "Hozir"
                            next -> "Keyingi"
                            else -> null
                        },
                        transitionSpec = { textTransition() },
                        label = "Prayer timeline status",
                    ) { status ->
                        if (status != null) {
                            val active = status == "Hozir"
                            Text(
                                status,
                                modifier = Modifier
                                    .background(
                                        if (active) colors.secondaryContainer else colors.surfaceContainerHigh,
                                        CircleShape,
                                    )
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                color = if (active) colors.onSecondaryContainer else colors.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                            )
                        }
                    }
                }
                AnimatedValueText(
                    time ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
