package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import android.graphics.Matrix
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.sunsetProgressColors
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.shape.MorphPolygonShape
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

private val halfTurn = PI.toFloat()
private val fullTurn = 2f * halfTurn

private fun wrapAngle(angle: Float): Float = (angle + halfTurn + fullTurn) % fullTurn - halfTurn

internal class TasbexPressLight {
    private val rim = Animatable(0f)
    private val tip = Animatable(0f)
    private var targetAngle by mutableFloatStateOf(halfTurn / 2f)
    var angle by mutableFloatStateOf(targetAngle)
        private set
    val rimOpacity get() = rim.value
    val tipOpacity get() = tip.value

    fun aimAt(delta: Offset, deadZone: Float, start: Boolean = false) {
        if (delta.getDistanceSquared() > deadZone * deadZone) {
            targetAngle = atan2(delta.y, delta.x)
        } else if (start) {
            targetAngle = halfTurn / 2f
        }
        if (start) angle = targetAngle
    }

    suspend fun followDirection() {
        snapshotFlow { targetAngle }.collect {
            var previousFrame = 0L
            while (abs(wrapAngle(targetAngle - angle)) > 0.002f) {
                withFrameNanos { frame ->
                    val elapsed = if (previousFrame == 0L) 16_000_000L else frame - previousFrame
                    val response = 1f - exp(-elapsed / 28_000_000f)
                    angle = wrapAngle(angle + wrapAngle(targetAngle - angle) * response)
                    previousFrame = frame
                }
            }
            angle = targetAngle
        }
    }

    suspend fun begin(): Unit = coroutineScope {
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
        launch { light.followDirection() }
        interactionSource.interactions.filterIsInstance<PressInteraction>().collectLatest { press ->
            when (press) {
                is PressInteraction.Press -> light.begin()
                is PressInteraction.Release -> light.finish(released = true)
                is PressInteraction.Cancel -> light.finish(released = false)
            }
        }
    }
    return light
}

internal fun Modifier.tasbexCountGestures(
    touchCenter: DpOffset,
    interactionSource: MutableInteractionSource,
    pressLight: TasbexPressLight,
    onTap: State<() -> Unit>,
): Modifier = pointerInput(touchCenter, interactionSource, pressLight) {
    val center = Offset(touchCenter.x.toPx(), touchCenter.y.toPx())
    val deadZone = 12.dp.toPx()
    val slop = viewConfiguration.touchSlop
    awaitEachGesture {
        // Own touch gestures here. Clickable still supplies keyboard and accessibility actions.
        val down = awaitFirstDown(pass = PointerEventPass.Initial)
        down.consume()
        pressLight.aimAt(down.position - center, deadZone, start = true)
        val press = PressInteraction.Press(down.position)
        interactionSource.tryEmit(press)
        var dragged = false
        var tapped = false
        try {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (change.isConsumed) break
                val position = change.position
                dragged = dragged || (position - down.position).getDistanceSquared() > slop * slop
                pressLight.aimAt(position - center, deadZone)
                change.consume()
                if (!change.pressed) {
                    tapped = !dragged && position.x in 0f..size.width.toFloat() && position.y in 0f..size.height.toFloat()
                    break
                }
            }
        } finally {
            interactionSource.tryEmit(if (tapped) PressInteraction.Release(press) else PressInteraction.Cancel(press))
        }
        if (tapped) onTap.value()
    }
}

internal fun Modifier.tasbexTouchRimLight(
    shape: MorphPolygonShape,
    dark: Boolean,
    pressLight: TasbexPressLight,
): Modifier = drawWithCache {
    val diameter = size.minDimension
    if (diameter <= 0f) return@drawWithCache onDrawBehind {}

    val center = Offset(size.width / 2f, size.height / 2f)
    val contour = shape.outlinePath(size)
    val spread = minOf(64.dp.toPx(), diameter / 4f)
    val haloRadius = minOf((if (dark) 38.dp else 41.dp).toPx(), diameter / 6.5f)
    val warm = if (dark) sunsetProgressColors[1] else Color(0xFFD75A15)
    val hot = if (dark) sunsetProgressColors.last() else Color(0xFFE58024)
    val haloShader = RadialGradient(
        0f, 0f, haloRadius,
        intArrayOf(warm.copy(alpha = if (dark) 0.66f else 0.79f).toArgb(),
            warm.copy(alpha = if (dark) 0.25f else 0.34f).toArgb(), Color.Transparent.toArgb()),
        floatArrayOf(0f, 0.52f, 1f),
        Shader.TileMode.CLAMP,
    )
    val edgeShader = RadialGradient(
        0f, 0f, spread,
        intArrayOf(hot.toArgb(), warm.copy(alpha = 0.82f).toArgb(), Color.Transparent.toArgb()),
        floatArrayOf(0f, 0.47f, 1f),
        Shader.TileMode.CLAMP,
    )
    val halo = ShaderBrush(haloShader)
    val edge = ShaderBrush(edgeShader)
    val translation = Matrix()
    val outerStroke = Stroke(width = haloRadius * 0.52f, cap = StrokeCap.Round)
    val softStroke = Stroke(width = haloRadius * 0.33f, cap = StrokeCap.Round)
    val edgeStroke = Stroke(width = minOf(6.dp.toPx(), haloRadius * 0.40f), cap = StrokeCap.Round)
    onDrawBehind {
        val opacity = pressLight.rimOpacity
        if (opacity > 0f) {
            val angle = pressLight.angle
            val point = center + Offset(cos(angle), sin(angle)) * (diameter / 2f)
            translation.setTranslate(point.x, point.y)
            haloShader.setLocalMatrix(translation)
            edgeShader.setLocalMatrix(translation)
            drawCircle(halo, haloRadius, point, alpha = opacity)
            drawPath(contour, edge, alpha = opacity * 0.16f, style = outerStroke)
            drawPath(contour, edge, alpha = opacity * 0.28f, style = softStroke)
            drawPath(contour, edge, alpha = opacity, style = edgeStroke)
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
