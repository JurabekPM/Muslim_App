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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.sunsetProgressColors
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.shape.MorphPolygonShape

internal class TasbexPressLight {
    private val rim = Animatable(0f)
    private val tip = Animatable(0f)
    var position by mutableStateOf<Offset?>(null)
        private set
    val rimOpacity get() = rim.value
    val tipOpacity get() = tip.value

    suspend fun pressAt(pressedPosition: Offset): Unit = coroutineScope {
        position = pressedPosition
        launch { rim.animateTo(1f, tween(105)) }
        launch {
            tip.animateTo(1f, tween(75))
            delay(90)
            tip.animateTo(0.40f, tween(310))
        }
    }

    suspend fun finish(released: Boolean): Unit = coroutineScope {
        launch {
            // Short taps still show the rim even if they end before its first frame.
            if (released && rim.value < 0.64f) rim.animateTo(0.72f, tween(65))
            rim.animateTo(0f, tween(300))
            position = null
        }
        launch {
            // On release the bright head follows the progress as the count increments.
            if (released) tip.animateTo(1f, tween(105))
            tip.animateTo(0f, tween(350))
        }
    }
}

@Composable
internal fun rememberTasbexPressLight(interactionSource: InteractionSource): TasbexPressLight {
    val light = remember(interactionSource) { TasbexPressLight() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.filterIsInstance<PressInteraction>().collectLatest { press ->
            when (press) {
                is PressInteraction.Press -> light.pressAt(press.pressPosition)
                is PressInteraction.Release -> light.finish(released = true)
                is PressInteraction.Cancel -> light.finish(released = false)
            }
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
    val spread = minOf(64.dp.toPx(), length * 0.08f)
    val point = measure.getPosition(nearest)
    val arc = measure.wrappedSegment(nearest - spread, spread * 2f)
    val haloRadius = minOf(38.dp.toPx(), diameter / 6.5f)
    val warm = sunsetProgressColors[if (dark) 1 else 0]
    val hot = sunsetProgressColors[if (dark) 3 else 1]
    val halo = Brush.radialGradient(
        0f to warm.copy(alpha = if (dark) 0.66f else 0.55f),
        0.52f to warm.copy(alpha = if (dark) 0.25f else 0.20f),
        1f to Color.Transparent,
        center = point,
        radius = haloRadius,
    )
    val edge = Brush.radialGradient(
        0f to hot,
        0.47f to warm.copy(alpha = 0.78f),
        1f to Color.Transparent,
        center = point,
        radius = spread,
    )
    val outerStroke = Stroke(width = haloRadius * 0.52f, cap = StrokeCap.Round)
    val edgeStroke = Stroke(width = minOf(6.dp.toPx(), haloRadius * 0.40f), cap = StrokeCap.Round)
    onDrawBehind {
        val opacity = pressLight.rimOpacity
        if (opacity > 0f) {
            drawCircle(halo, haloRadius, point, alpha = opacity)
            drawPath(arc, edge, alpha = opacity * 0.28f, style = outerStroke)
            drawPath(arc, edge, alpha = opacity, style = edgeStroke)
        }
    }
}

internal fun PathMeasure.wrappedSegment(start: Float, distance: Float): Path {
    val from = (start + length) % length
    val to = from + distance
    return Path().apply {
        getSegment(from, minOf(to, length), this, startWithMoveTo = true)
        if (to > length) getSegment(0f, to - length, this, startWithMoveTo = false)
    }
}
