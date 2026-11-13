package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter

@Composable
internal fun rememberPressScale(interactionSource: InteractionSource): State<Float> {
    val scale = remember(interactionSource) { Animatable(1f) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.filter { it is PressInteraction }.collectLatest { press ->
            when (press) {
                is PressInteraction.Press -> scale.animateTo(0.94f, tween(85))
                is PressInteraction.Release -> {
                    // A quick tap can release before the first animation frame.
                    if (scale.value > 0.98f) scale.animateTo(0.94f, tween(65))
                    scale.animateTo(1f, tween(160))
                }
                is PressInteraction.Cancel -> scale.animateTo(1f, tween(160))
                else -> Unit
            }
        }
    }
    return scale.asState()
}

internal fun Modifier.pressScale(scale: State<Float>): Modifier = graphicsLayer {
    scaleX = scale.value
    scaleY = scale.value
}

@Composable
internal fun PressIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        modifier = Modifier.pressScale(rememberPressScale(interactionSource)),
        interactionSource = interactionSource,
        content = content,
    )
}
