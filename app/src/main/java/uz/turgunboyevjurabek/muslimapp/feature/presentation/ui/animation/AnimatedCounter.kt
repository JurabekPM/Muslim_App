package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.floor

@Composable
internal fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    resetId: Long? = null,
) {
    val countText = count.toString()
    var roll by remember { mutableStateOf(CounterRoll.settled(count)) }
    val progress = remember { Animatable(1f) }

    var lastResetId by remember { mutableStateOf(resetId) }
    LaunchedEffect(count, resetId) {
        val isReset = count == 0 && resetId != lastResetId
        lastResetId = resetId
        if (count == roll.target) return@LaunchedEffect
        val next = roll.retarget(count, progress.value)
        progress.snapTo(0f)
        roll = next
        val duration = if (isReset) 1050 else 200
        progress.animateTo(1f, tween(duration, easing = FastOutSlowInEasing))
        roll = CounterRoll.settled(count)
    }

    val measurer = rememberTextMeasurer()
    val digits = remember(measurer, style) {
        List(10) { digit -> measurer.measure(digit.toString(), style, softWrap = false) }
    }
    val digitWidth = digits.maxOf { it.size.width }.toFloat()
    val digitHeight = digits.maxOf { it.size.height }.toFloat()
    val wheels = roll.wheels
    val width = digitWidth * wheels.size
    val color = LocalContentColor.current
    val density = LocalDensity.current

    Canvas(
        modifier = modifier
            .semantics { text = AnnotatedString(countText) }
            .fillMaxWidth()
            .height(with(density) { digitHeight.toDp() } + 16.dp)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithCache {
                val fade = Brush.verticalGradient(
                    0f to Color.Transparent,
                    0.18f to Color.Black,
                    0.82f to Color.Black,
                    1f to Color.Transparent,
                    endY = size.height,
                )
                onDrawWithContent {
                    drawContent()
                    drawRect(fade, blendMode = BlendMode.DstIn)
                }
            },
    ) {
        val fraction = progress.value
        val leadingAlpha = ((1f - fraction) * 3f).coerceIn(0f, 1f)
        val hiddenWidth = (wheels.size - countText.length).coerceAtLeast(0) * digitWidth * (1f - leadingAlpha)
        // The outgoing digits shift left as the surviving digits move to the center.
        val horizontalSpace = (size.width - 24.dp.toPx()).coerceAtLeast(0f)
        val fit = minOf(1f, horizontalSpace / (width + hiddenWidth), size.height / digitHeight)
        val left = (size.width - width - hiddenWidth) / 2f
        val top = (size.height - digitHeight) / 2f

        scale(fit, pivot = center) {
            wheels.forEachIndexed { place, wheel ->
                val position = wheel.positionAt(fraction)
                val lower = floor(position).toInt()
                val offset = position - lower
                val alpha = if (place >= countText.length) {
                    leadingAlpha
                } else {
                    1f
                }
                val column = wheels.lastIndex - place
                val columnLeft = left + column * digitWidth
                val digit = digits[lower.mod(10)]
                drawText(
                    textLayoutResult = digit,
                    color = color,
                    topLeft = Offset(
                        columnLeft + (digitWidth - digit.size.width) / 2f,
                        top - offset * digitHeight,
                    ),
                    alpha = alpha,
                )
                if (offset > 0f) {
                    val next = digits[(lower + 1).mod(10)]
                    drawText(
                        textLayoutResult = next,
                        color = color,
                        topLeft = Offset(
                            columnLeft + (digitWidth - next.size.width) / 2f,
                            top + (1f - offset) * digitHeight,
                        ),
                        alpha = alpha,
                    )
                }
            }
        }
    }
}

internal data class DigitWheel(val start: Float, val end: Float) {
    fun positionAt(progress: Float): Float = start + (end - start) * progress
}

internal class CounterRoll private constructor(
    val target: Int,
    val wheels: List<DigitWheel>,
) {
    fun retarget(count: Int, progress: Float): CounterRoll {
        var oldDigits = target
        var newDigits = count
        val wheelCount = maxOf(wheels.size, count.toString().length)
        val nextWheels = List(wheelCount) { place ->
            val previousWheel = wheels.getOrNull(place) ?: DigitWheel(0f, 0f)
            val oldDigit = (oldDigits % 10).toFloat()
            val current = oldDigit + (previousWheel.positionAt(progress) - previousWheel.end) % 10f
            val change = newDigits - oldDigits
            // Keep the destination digit and direction, with at most one turn per update.
            val steps = when {
                change < 0 -> -((abs(change) - 1) % 10 + 1)
                change > 0 -> (change - 1) % 10 + 1
                else -> 0
            }
            oldDigits /= 10
            newDigits /= 10
            DigitWheel(current, oldDigit + steps)
        }
        return CounterRoll(count, nextWheels)
    }

    companion object {
        fun settled(count: Int): CounterRoll {
            val wheels = count.toString().reversed().map { char ->
                val digit = char.digitToInt().toFloat()
                DigitWheel(digit, digit)
            }
            return CounterRoll(count, wheels)
        }
    }
}
