package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla.QiblaState
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun QiblaCompassDial(state: State<QiblaState>, facingQibla: Boolean) {
    val colors = MaterialTheme.colorScheme
    val kaaba = painterResource(R.drawable.ic_qibla_kaaba)
    val alignment = animateFloatAsState(
        if (facingQibla) 1f else 0f,
        animationSpec = tween(280),
        label = "Qibla arc closing",
    )
    val glow = remember { Animatable(0f) }
    LaunchedEffect(facingQibla) {
        if (facingQibla) {
            glow.animateTo(1f, tween(190))
            glow.animateTo(0.68f, tween(520))
        } else {
            glow.animateTo(0f, tween(280))
        }
    }
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
    val labels = remember(textMeasurer, labelStyle) {
        listOf("Shimol", "Sharq", "Janub", "G‘arb").map { textMeasurer.measure(it, labelStyle) }
    }

    Box(
        Modifier.widthIn(max = 360.dp).fillMaxWidth().aspectRatio(1f)
            .semantics { contentDescription = "Qibla kompasi" }
            .drawWithCache {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension * 0.42f
                val trackRadius = radius * 0.94f
                val badgeRadius = 11.dp.toPx()
                val marker = center - Offset(0f, radius * 0.84f)
                val arcBounds = center - Offset(trackRadius, trackRadius)
                val arcSize = Size(trackRadius * 2, trackRadius * 2)
                val accent = colors.primary
                val face = lerp(colors.surface, colors.primaryContainer, 0.60f)
                val trackColor = lerp(colors.primaryContainer, accent, 0.16f)
                val dialFill = Brush.radialGradient(
                    colors = listOf(face, lerp(colors.surface, colors.primaryContainer, 0.30f)),
                    center = center,
                    radius = radius,
                )
                val shadowRadius = radius + 12.dp.toPx()
                val shadowCenter = center + Offset(0f, 4.dp.toPx())
                val shadow = Brush.radialGradient(
                    0.82f to Color.Black.copy(alpha = 0.20f),
                    1f to Color.Transparent,
                    center = shadowCenter,
                    radius = shadowRadius,
                )
                val glowRadius = radius + 13.dp.toPx()
                val glowEdge = radius / glowRadius
                val ringGlow = Brush.radialGradient(
                    (glowEdge - 0.08f) to Color.Transparent,
                    glowEdge to accent.copy(alpha = 0.43f),
                    1f to Color.Transparent,
                    center = center,
                    radius = glowRadius,
                )
                val badgeGlowRadius = badgeRadius + 13.dp.toPx()
                val badgeGlow = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.75f), accent.copy(alpha = 0f)),
                    center = marker,
                    radius = badgeGlowRadius,
                )
                val ticks = Path()
                val majorTicks = Path()
                for (tick in 0 until 32) {
                    val major = tick % 8 == 0
                    val path = if (major) majorTicks else ticks
                    val degrees = tick * 11.25f
                    val start = dialPoint(center, degrees, radius * if (major) 0.90f else 0.925f)
                    val end = dialPoint(center, degrees, radius * 0.97f)
                    path.moveTo(start.x, start.y)
                    path.lineTo(end.x, end.y)
                }
                val arrow = Path().apply {
                    moveTo(center.x, center.y - radius * 0.73f)
                    quadraticTo(
                        center.x + radius * 0.015f, center.y - radius * 0.73f,
                        center.x + radius * 0.027f, center.y - radius * 0.69f,
                    )
                    lineTo(center.x + radius * 0.090f, center.y - radius * 0.41f)
                    quadraticTo(center.x + radius * 0.10f, center.y - radius * 0.37f,
                        center.x + radius * 0.067f, center.y - radius * 0.35f)
                    quadraticTo(center.x + radius * 0.025f, center.y - radius * 0.25f,
                        center.x + radius * 0.041f, center.y)
                    lineTo(center.x - radius * 0.041f, center.y)
                    quadraticTo(center.x - radius * 0.025f, center.y - radius * 0.25f,
                        center.x - radius * 0.067f, center.y - radius * 0.35f)
                    quadraticTo(center.x - radius * 0.10f, center.y - radius * 0.37f,
                        center.x - radius * 0.090f, center.y - radius * 0.41f)
                    lineTo(center.x - radius * 0.027f, center.y - radius * 0.69f)
                    quadraticTo(
                        center.x - radius * 0.015f, center.y - radius * 0.73f,
                        center.x, center.y - radius * 0.73f,
                    )
                    close()
                }
                val arrowFill = Brush.linearGradient(
                    colors = listOf(accent, lerp(accent, face, 0.36f)),
                    start = center - Offset(radius * 0.06f, radius * 0.61f),
                    end = center + Offset(radius * 0.06f, 0f),
                )
                val kaabaSize = 16.dp.toPx()
                val kaabaTint = ColorFilter.tint(accent)
                val indexY = center.y - radius - 12.dp.toPx()
                val index = Path().apply {
                    moveTo(center.x - 4.5.dp.toPx(), indexY)
                    lineTo(center.x + 4.5.dp.toPx(), indexY)
                    lineTo(center.x, indexY + 7.dp.toPx())
                    close()
                }
                val trackStroke = Stroke(radius * 0.068f)
                val rimStroke = Stroke(1.dp.toPx())
                val arcStroke = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
                val majorTickStroke = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                val minorTickStroke = Stroke(1.dp.toPx(), cap = StrokeCap.Round)
                val badgeStroke = Stroke(1.5.dp.toPx())

                onDrawBehind {
                    // Sensor and animation values are read only during drawing.
                    val reading = state.value
                    val displayHeading = reading.trueHeading ?: 0f
                    val aligned = alignment.value
                    val light = glow.value
                    drawCircle(shadow, shadowRadius, shadowCenter)
                    drawCircle(dialFill, radius)
                    drawCircle(trackColor, trackRadius, style = trackStroke)
                    if (light > 0f) drawCircle(ringGlow, glowRadius, alpha = light)
                    drawCircle(accent.copy(alpha = 0.16f + light * 0.64f), radius, style = rimStroke)
                    rotate(-displayHeading, pivot = center) {
                        drawPath(ticks, colors.onSurfaceVariant.copy(alpha = 0.65f), style = minorTickStroke)
                        drawPath(majorTicks, accent, style = majorTickStroke)
                    }
                    val turn = reading.turn?.times(1f - aligned)
                    if (turn != null && abs(turn) > 0.2f) {
                        drawArc(accent, -90f + minOf(turn, 0f), abs(turn), false, arcBounds, arcSize, style = arcStroke)
                    }
                    labels.forEachIndexed { index, label ->
                        val point = dialPoint(center, index * 90f - displayHeading, radius * 0.65f)
                        drawText(
                            textLayoutResult = label,
                            color = colors.onSurface,
                            topLeft = Offset(point.x - label.size.width / 2f, point.y - label.size.height / 2f),
                        )
                    }
                    if (turn != null) {
                        rotate(turn, pivot = center) {
                            drawPath(arrow, arrowFill)
                            if (light > 0f) drawCircle(badgeGlow, badgeGlowRadius, marker, alpha = light)
                            drawCircle(face, badgeRadius, marker)
                            drawCircle(accent, badgeRadius, marker, style = badgeStroke)
                            rotate(-turn, pivot = marker) {
                                translate(marker.x - kaabaSize / 2f, marker.y - kaabaSize / 2f) {
                                    with(kaaba) { draw(Size(kaabaSize, kaabaSize), colorFilter = kaabaTint) }
                                }
                            }
                        }
                        drawCircle(accent, 8.dp.toPx())
                        drawCircle(face, 4.5.dp.toPx())
                    }
                    drawPath(index, accent)
                }
            },
    )
}

private fun dialPoint(center: Offset, degrees: Float, distance: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    return center + Offset(sin(radians).toFloat() * distance, -cos(radians).toFloat() * distance)
}
