package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.rememberSunsetProgressPhase
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.sunsetProgressColors
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.sunsetProgressLight
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.shape.MorphPolygonShape
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.random.Random

@Composable
internal fun Modifier.tasbexOutlineProgress(
    shape: MorphPolygonShape,
    fraction: Float,
    dark: Boolean,
    glowColor: Color,
): Modifier {
    val visibleFraction = animateFloatAsState(fraction, tween(280), label = "Tasbix progress")
    val motion = rememberSunsetProgressPhase(2_500)
    val sparklePhases = remember { Random(23).run { List(26) { nextFloat() } } }
    val trackColor = if (dark) Color(0xFF46505C) else Color(0xFFC7CECF)

    return drawWithCache {
        val shortSide = size.minDimension
        if (shortSide <= 0f) return@drawWithCache onDrawBehind {}

        val strokeWidth = minOf(9.dp.toPx(), shortSide / 8f)
        // The outer half of the stroke reaches the button outline. The parent shape clips it.
        val inset = strokeWidth / 2f
        val stroke = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val center = Offset(size.width / 2f, size.height / 2f)
        val contour = shape.outlinePath(size, inset)
        val measure = PathMeasure().apply { setPath(contour, forceClosed = true) }
        val length = measure.length
        if (length <= 0f) return@drawWithCache onDrawBehind {}

        val start = measure.nearestDistance(Offset(center.x, inset))
        val filled = length * visibleFraction.value
        val tip = measure.getPosition((start + filled) % length)
        val startAngle = (measure.getPosition(start) - center).angleDegrees()
        val tipAngle = (tip - center).angleDegrees()
        val endAngle = if (filled == length) 1f else ((tipAngle - startAngle + 360f) % 360f / 360f)
        val segment = if (filled == length) contour else Path().apply {
            measure.getSegment(start, minOf(start + filled, length), this, startWithMoveTo = true)
            if (start + filled > length) {
                measure.getSegment(0f, start + filled - length, this, startWithMoveTo = false)
            }
        }
        val sunset = if (filled > 0f) sunsetSweep(center, startAngle, endAngle.coerceAtLeast(0.001f)) else null
        val tipGlow = Brush.radialGradient(
            listOf(sunsetProgressLight, Color.Transparent),
            center = tip,
            radius = strokeWidth / 2f,
        )
        val drift = 17.dp.toPx()
        onDrawBehind {
            val phase = motion.value
            drawPath(contour, trackColor, style = stroke)
            if (sunset != null) drawPath(segment, sunset, style = stroke)
            sparklePhases.forEachIndexed { index, offset ->
                val age = (phase + offset) % 1f
                val distance = (index * length / sparklePhases.size + offset * drift + age * drift) % length
                val insideFill = distance < filled - strokeWidth / 2f
                drawCircle(
                    color = if (insideFill) sunsetProgressLight else glowColor,
                    radius = strokeWidth * (0.07f + offset * 0.08f),
                    center = measure.getPosition((start + distance) % length),
                    alpha = sin(PI * age).toFloat() * if (insideFill) 0.85f else 0.32f,
                )
            }
            if (filled >= strokeWidth) {
                val pulse = 0.8f + 0.2f * sin(2 * PI * phase).toFloat()
                drawCircle(tipGlow, radius = strokeWidth / 2f, center = tip, alpha = pulse)
            }
        }
    }
}

private fun PathMeasure.nearestDistance(point: Offset): Float {
    val sampleStep = length / 128
    return (0 until 128).minBy { index ->
        val offset = getPosition(index * sampleStep) - point
        offset.x * offset.x + offset.y * offset.y
    } * sampleStep
}

private fun Offset.angleDegrees(): Float = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()

private fun sunsetSweep(center: Offset, angle: Float, end: Float): ShaderBrush {
    // Return to the start color just before the sweep seam around the rounded start cap.
    val colors = sunsetProgressColors.map(Color::toArgb)
    val stops = floatArrayOf(0f, end / 3f, end * 2f / 3f, end, 1f)
    val shader = SweepGradient(center.x, center.y, (colors + colors.first()).toIntArray(), stops)
    shader.setLocalMatrix(Matrix().apply { setRotate(angle, center.x, center.y) })
    return ShaderBrush(shader)
}
