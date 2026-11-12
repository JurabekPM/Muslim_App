package uz.turgunboyevjurabek.muslimapp.core.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

fun Modifier.shimmerEffectAsCard(): Modifier = composed {
    val colors = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "Shimmer")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1150, easing = LinearEasing)),
        label = "Shimmer sweep",
    )
    drawWithCache {
        val shades = listOf(colors.surfaceContainerHigh, colors.surfaceContainerHighest, colors.surfaceContainerHigh)
        val band = (size.width * 0.58f).coerceAtLeast(1f)
        onDrawBehind {
            val x = (size.width + band) * progress.value - band
            drawRect(Brush.linearGradient(shades, Offset(x, 0f), Offset(x + band, size.height / 2f)))
        }
    }
}
