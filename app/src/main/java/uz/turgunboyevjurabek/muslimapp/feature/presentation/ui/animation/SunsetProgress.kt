package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color

internal val sunsetProgressColors = listOf(
    Color(0xFFEA6113), Color(0xFFF88F22), Color(0xFFFBB931), Color(0xFFFFE3B3),
)
internal val sunsetProgressLight = Color(0xFFFFF9E8)

@Composable
internal fun rememberSunsetProgressPhase(durationMillis: Int): State<Float> =
    rememberInfiniteTransition(label = "Sunset sparkles").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing)),
        label = "Sparkle phase",
    )
