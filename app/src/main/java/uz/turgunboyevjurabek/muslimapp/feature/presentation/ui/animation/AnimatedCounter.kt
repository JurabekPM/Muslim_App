package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
        val next = roll.retarget(count, progress.value, isReset)
        progress.snapTo(0f)
        roll = next
        val spec = if (isReset) {
            tween<Float>(1550, easing = LinearEasing)
        } else {
            tween<Float>(200, easing = FastOutSlowInEasing)
        }
        progress.animateTo(1f, spec)
        roll = CounterRoll.settled(count)
    }

    val measurer = rememberTextMeasurer()
    val digits = remember(measurer, style) {
        List(10) { digit -> measurer.measure(digit.toString(), style, softWrap = false) }
    }
    val digitWidth = digits.maxOf { it.size.width }.toFloat()
    val digitHeight = digits.maxOf { it.size.height }.toFloat()
    val wheels = roll.wheels
    val resetExit = roll.resetExit
    val activeRoll = roll
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
        val wheelProgress = activeRoll.wheelProgressAt(fraction)
        val exitProgress = resetExit?.progressAt(fraction)
        val leadingAlpha = if (exitProgress == null) {
            ((1f - fraction) * 3f).coerceIn(0f, 1f)
        } else {
            1f - exitProgress
        }
        val departingWidth =
            (wheels.size - countText.length).coerceAtLeast(0) * digitWidth * (1f - leadingAlpha)
        val horizontalSpace = (size.width - 24.dp.toPx()).coerceAtLeast(0f)
        val requiredWidth = if (resetExit == null) width + departingWidth else width
        val fit = minOf(1f, horizontalSpace / requiredWidth, size.height / digitHeight)
        val left = (size.width - width) / 2f
        val top = (size.height - digitHeight) / 2f

        scale(fit, pivot = center) {
            fun drawDigit(value: Int, x: Float, y: Float, alpha: Float) {
                val digit = digits[value.mod(10)]
                drawText(
                    textLayoutResult = digit,
                    color = color,
                    topLeft = Offset(x + (digitWidth - digit.size.width) / 2f, y),
                    alpha = alpha,
                )
            }

            wheels.forEachIndexed { place, wheel ->
                val isLeaving = place >= countText.length
                val columnLeft = left + (wheels.lastIndex - place) * digitWidth
                val leavingOnReset = exitProgress != null && isLeaving
                val x = if (leavingOnReset) columnLeft else columnLeft - departingWidth / 2f
                val y = if (leavingOnReset) top + digitHeight * 0.68f * (exitProgress ?: 0f) else top
                val position = wheel.positionAt(wheelProgress)
                val lower = floor(position).toInt()
                val offset = position - lower
                val alpha = if (isLeaving) leadingAlpha else 1f
                if (alpha <= 0f) return@forEachIndexed
                fun drawWheelDigit(digitIndex: Int, glyphY: Float) {
                    val drop = if (leavingOnReset) wheel.terminalZeroDrop(digitIndex, position) else 0f
                    val visibility = 1f - drop
                    if (visibility > 0f) {
                        drawDigit(
                            digitIndex,
                            x,
                            glyphY + digitHeight * 0.55f * drop,
                            alpha * visibility * visibility,
                        )
                    }
                }
                drawWheelDigit(lower, y - offset * digitHeight)
                if (offset > 0f) {
                    drawWheelDigit(lower + 1, y + (1f - offset) * digitHeight)
                }
            }
        }
    }
}

internal data class DigitWheel(val start: Float, val end: Float) {
    fun positionAt(progress: Float): Float = start + (end - start) * progress

    fun terminalZeroDrop(digitIndex: Int, position: Float): Float {
        if (digitIndex.toFloat() != end || digitIndex.mod(10) != 0) return 0f
        // Release the final zero while it is still rolling into the digit slot.
        return (1f - abs(position - end)).coerceIn(0f, 1f)
    }
}

private val resetRollEasing = CubicBezierEasing(0.35f, 0.2f, 0.65f, 1f)

internal class ResetDigitExit(private val carriedProgress: Float? = null) {
    fun wheelProgressAt(progress: Float): Float =
        if (carriedProgress == null) {
            resetRollEasing.transform((progress / 0.86f).coerceIn(0f, 1f))
        } else {
            progress
        }

    fun progressAt(progress: Float): Float {
        val start = carriedProgress
        return if (start == null) {
            // Let the wheels roll before the leading places start to leave.
            FastOutSlowInEasing.transform(((progress - 0.58f) / 0.42f).coerceIn(0f, 1f))
        } else {
            start + (1f - start) * progress
        }
    }
}

internal class CounterRoll private constructor(
    val target: Int,
    val wheels: List<DigitWheel>,
    val resetExit: ResetDigitExit? = null,
) {
    fun wheelProgressAt(progress: Float): Float = resetExit?.wheelProgressAt(progress) ?: progress

    fun retarget(count: Int, progress: Float, isReset: Boolean = false): CounterRoll {
        val wheelProgress = wheelProgressAt(progress)
        var oldDigits = target
        var newDigits = count
        val wheelCount = maxOf(wheels.size, count.toString().length)
        val nextWheels = List(wheelCount) { place ->
            val previousWheel = wheels.getOrNull(place) ?: DigitWheel(0f, 0f)
            val oldDigit = (oldDigits % 10).toFloat()
            val current = oldDigit + (previousWheel.positionAt(wheelProgress) - previousWheel.end) % 10f
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
        val exit = if (isReset) {
            ResetDigitExit()
        } else {
            resetExit?.let { ResetDigitExit(it.progressAt(progress)) }
        }
        return CounterRoll(count, nextWheels, exit)
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
