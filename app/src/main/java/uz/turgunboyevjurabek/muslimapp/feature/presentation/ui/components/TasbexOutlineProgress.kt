package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import android.graphics.ComposeShader
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.Shader
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
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
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
    pressLight: TasbexPressLight,
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
        val endAngle = if (filled == length) 1f else ((tipAngle - startAngle + 360f) % 360f / 360f).coerceAtLeast(0.001f)
        val segment = if (filled == length) contour else measure.wrappedSegment(start, filled)
        val sweep = if (filled > 0f) sunsetSweep(center, startAngle, endAngle) else null
        val sunset = sweep?.let { ShaderBrush(it) }
        val radius = shortSide / 2f - inset
        val fringe = minOf(21.dp.toPx(), radius * 0.32f)
        val glow = sweep?.let {
            val rimSweep = if (endAngle == 1f) it else taperedSunsetSweep(center, startAngle, endAngle)
            filledRimGlow(rimSweep, center, radius, fringe)
        }
        val glowStroke = Stroke(fringe * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val outline = shape.outlinePath(size)
        val tipGlow = Brush.radialGradient(
            listOf(sunsetProgressLight, Color.Transparent),
            center = tip,
            radius = strokeWidth / 2f,
        )
        val pressHead = sunsetProgressColors[if (dark) 2 else 1]
        val haloRadius = strokeWidth * 2.65f
        val pressHalo = Brush.radialGradient(
            0f to pressHead.copy(alpha = 0.75f),
            0.43f to pressHead.copy(alpha = 0.37f),
            1f to Color.Transparent,
            center = tip,
            radius = haloRadius,
        )
        val tail = minOf(filled, strokeWidth * 4f)
        val pressTail = measure.wrappedSegment(start + filled - tail, tail)
        val tailGlow = Brush.radialGradient(
            listOf(sunsetProgressLight, Color.Transparent),
            center = tip,
            radius = strokeWidth * 4f,
        )
        val tailStroke = Stroke(strokeWidth * 0.68f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val drift = 17.dp.toPx()
        onDrawBehind {
            val heldGlow = pressLight.rimOpacity
            if (glow != null && heldGlow > 0f) {
                drawPath(segment, glow, alpha = heldGlow * if (dark) 0.88f else 0.83f, style = glowStroke)
            }
            // Keep the track and tip inside the shape while the press glow extends past its edge.
            clipPath(outline) {
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
                val flash = pressLight.tipOpacity
                if (flash > 0f) {
                    drawCircle(pressHalo, radius = haloRadius, center = tip, alpha = flash)
                    if (tail > 0f) drawPath(pressTail, tailGlow, alpha = flash, style = tailStroke)
                    drawCircle(pressHead, radius = strokeWidth * 0.60f, center = tip, alpha = flash)
                    drawCircle(Color.White, radius = strokeWidth * 0.43f, center = tip, alpha = flash)
                }
            }
        }
    }
}

internal fun PathMeasure.nearestDistance(point: Offset): Float {
    val sampleStep = length / 128
    return (0 until 128).minBy { index ->
        val offset = getPosition(index * sampleStep) - point
        offset.x * offset.x + offset.y * offset.y
    } * sampleStep
}

private fun Offset.angleDegrees(): Float = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()

private fun sunsetSweep(center: Offset, angle: Float, end: Float): SweepGradient {
    // Return to the start color just before the sweep seam around the rounded start cap.
    val colors = sunsetProgressColors.map(Color::toArgb)
    val stops = floatArrayOf(0f, end / 3f, end * 2f / 3f, end, 1f)
    return rotatedSweep(center, angle, (colors + colors.first()).toIntArray(), stops)
}

private fun taperedSunsetSweep(center: Offset, angle: Float, end: Float): SweepGradient {
    val startColor = sunsetProgressColors.first()
    val endColor = sunsetProgressColors.last()
    val taper = minOf(0.024f, end / 4f)
    val colors = listOf(
        startColor.copy(alpha = 0f),
        startColor,
        sunsetProgressColors[1],
        sunsetProgressColors[2],
        endColor,
        endColor.copy(alpha = 0f),
        endColor.copy(alpha = 0f),
    )
    val stops = floatArrayOf(0f, taper, end / 3f, end * 2f / 3f, end - taper, end, 1f)
    return rotatedSweep(center, angle, colors.map(Color::toArgb).toIntArray(), stops)
}

private fun rotatedSweep(center: Offset, angle: Float, colors: IntArray, stops: FloatArray) =
    SweepGradient(center.x, center.y, colors, stops).apply {
        setLocalMatrix(Matrix().apply { setRotate(angle, center.x, center.y) })
    }

private fun filledRimGlow(sunset: SweepGradient, center: Offset, radius: Float, fringe: Float): ShaderBrush {
    val outer = radius + fringe
    val mask = RadialGradient(
        center.x, center.y, outer,
        intArrayOf(
            Color.Transparent.toArgb(),
            Color.Transparent.toArgb(),
            Color.White.copy(alpha = 0.08f).toArgb(),
            Color.White.copy(alpha = 0.80f).toArgb(),
            Color.White.copy(alpha = 0.27f).toArgb(),
            Color.Transparent.toArgb(),
        ),
        floatArrayOf(
            0f, (radius - fringe) / outer, (radius - fringe * 0.45f) / outer,
            radius / outer, (radius + fringe * 0.5f) / outer, 1f,
        ),
        Shader.TileMode.CLAMP,
    )
    return ShaderBrush(ComposeShader(sunset, mask, PorterDuff.Mode.DST_IN))
}
