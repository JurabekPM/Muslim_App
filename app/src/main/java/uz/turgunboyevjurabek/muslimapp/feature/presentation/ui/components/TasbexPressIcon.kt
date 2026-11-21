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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
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
    val phase = remember(interactionSource) { Animatable(1f) }
    LaunchedEffect(interactionSource) {
        var sweep: Job? = null
        interactionSource.interactions.filterIsInstance<PressInteraction.Press>().collect {
            if (sweep?.isActive != true) {
                sweep = launch {
                    phase.snapTo(0f)
                    phase.animateTo(1f, tween(650, easing = LinearEasing))
                }
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
            val haloRadius = 3.1f * unit
            val ring = Stroke(width = 1.2f * unit)
            val halos = centers.map { center ->
                Brush.radialGradient(
                    0f to sunsetProgressLight.copy(alpha = 0.80f),
                    0.50f to glow.copy(alpha = 0.52f),
                    1f to Color.Transparent,
                    center = center,
                    radius = haloRadius,
                )
            }
            onDrawWithContent {
                drawContent()
                val position = phase.value
                if (position >= 1f) return@onDrawWithContent
                val spread = 2.5f
                val head = position * (centers.lastIndex + spread * 2f) - spread
                centers.forEachIndexed { index, center ->
                    val proximity = (1f - abs(index - head) / spread).coerceIn(0f, 1f)
                    val light = proximity * proximity * (3f - 2f * proximity)
                    if (light > 0f) {
                        drawCircle(halos[index], haloRadius, center, alpha = light)
                        drawCircle(sunsetProgressLight, 1.2f * unit, center, alpha = light, style = ring)
                        drawCircle(Color.White, 0.57f * unit, center, alpha = light * 0.82f)
                    }
                }
            }
        },
    )
}
