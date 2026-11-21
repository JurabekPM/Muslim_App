package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.sunsetProgressColors
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.shape.MorphPolygonShape
import kotlin.math.abs

internal class TasbexPressLight {
    private val intensity = Animatable(0f)
    var position by mutableStateOf<Offset?>(null)
        private set
    val opacity get() = intensity.value

    suspend fun flashAt(position: Offset) {
        this.position = position
        intensity.animateTo(1f, tween(45))
        intensity.animateTo(0f, tween(255))
        this.position = null
    }
}

@Composable
internal fun rememberTasbexPressLight(interactionSource: InteractionSource): TasbexPressLight {
    val light = remember(interactionSource) { TasbexPressLight() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.filterIsInstance<PressInteraction.Press>().collectLatest {
            light.flashAt(it.pressPosition)
        }
    }
    return light
}

internal fun Modifier.tasbexTouchRimLight(
    shape: MorphPolygonShape,
    touchCenter: DpOffset,
    dark: Boolean,
    pressLight: TasbexPressLight,
): Modifier = drawWithCache {
    val touch = pressLight.position ?: return@drawWithCache onDrawBehind {}
    val diameter = size.minDimension
    if (diameter <= 0f) return@drawWithCache onDrawBehind {}

    val center = Offset(size.width / 2f, size.height / 2f)
    val delta = if (touch == Offset.Unspecified) Offset.Zero else
        touch - Offset(touchCenter.x.toPx(), touchCenter.y.toPx())
    val distance = delta.getDistance()
    val direction = if (distance < 2.dp.toPx()) Offset(0f, 1f) else delta / distance
    val measure = PathMeasure().apply { setPath(shape.outlinePath(size), forceClosed = true) }
    val length = measure.length
    if (length <= 0f) return@drawWithCache onDrawBehind {}

    val nearest = measure.nearestDistance(center + direction * diameter)
    val spread = minOf(40.dp.toPx(), length * 0.06f)
    val radius = minOf(17.dp.toPx(), diameter / 8f)
    val core = sunsetProgressColors[if (dark) 2 else 1]
    val warmth = sunsetProgressColors[if (dark) 1 else 0]
    // Overlapping, feathered gradients follow the contour and taper off at both ends of the arc.
    val halos = (-3..3).map { index ->
        val along = index / 3f
        val point = measure.getPosition((nearest + length + spread * along) % length) + direction * (radius * 0.15f)
        val weight = 1f - 0.85f * abs(along)
        val brush = Brush.radialGradient(
            0f to core.copy(alpha = weight * if (dark) 0.84f else 0.70f),
            0.46f to warmth.copy(alpha = weight * if (dark) 0.38f else 0.32f),
            1f to Color.Transparent,
            center = point,
            radius = radius,
        )
        point to brush
    }
    onDrawBehind {
        val opacity = pressLight.opacity
        if (opacity > 0f) {
            halos.forEach { (point, brush) -> drawCircle(brush, radius, point, alpha = opacity) }
        }
    }
}
