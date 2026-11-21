package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.sunsetProgressLight
import kotlin.math.abs

// Centers and stroke dimensions follow the 24-unit viewport of ic_nav_tasbex.
private val beadCenters = listOf(
    Offset(18f, 14f), Offset(14.5f, 16.5f), Offset(10.2f, 16.4f), Offset(6.6f, 14.5f),
    Offset(4.8f, 11.2f), Offset(5.5f, 7.5f), Offset(8.8f, 4.6f), Offset(13f, 4.3f),
    Offset(16.9f, 6.3f), Offset(19f, 10f),
)

@Composable
internal fun TasbexPressIcon(interactionSource: InteractionSource, tint: Color, glow: Color) {
    val phase = remember(interactionSource) { Animatable(2f) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.filterIsInstance<PressInteraction>().collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    phase.snapTo(0f)
                    phase.animateTo(1f, tween(220, easing = LinearEasing))
                }
                is PressInteraction.Release -> {
                    if (phase.value < 1f) phase.animateTo(1f, tween(120, easing = LinearEasing))
                    phase.animateTo(2f, tween(160))
                }
                is PressInteraction.Cancel -> phase.snapTo(2f)
            }
        }
    }
    Icon(
        painterResource(R.drawable.ic_nav_tasbex),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(46.dp).drawWithCache {
            val unit = size.minDimension / 24f
            val centers = beadCenters.map { it * unit }
            val haloRadius = 2.8f * unit
            val ring = Stroke(width = 1.05f * unit)
            val halos = centers.map { center ->
                Brush.radialGradient(listOf(glow.copy(alpha = 0.70f), Color.Transparent), center, haloRadius)
            }
            onDrawWithContent {
                drawContent()
                val position = phase.value
                if (position >= 2f) return@onDrawWithContent
                val head = position.coerceAtMost(1f) * (centers.lastIndex + 2) - 2f
                val opacity = (2f - position).coerceAtMost(1f)
                centers.forEachIndexed { index, center ->
                    val light = (1f - abs(index - head) / 2.2f).coerceAtLeast(0f) * opacity
                    if (light > 0f) {
                        drawCircle(halos[index], haloRadius, center, alpha = light)
                        drawCircle(sunsetProgressLight, 1.15f * unit, center, alpha = light, style = ring)
                    }
                }
            }
        },
    )
}
