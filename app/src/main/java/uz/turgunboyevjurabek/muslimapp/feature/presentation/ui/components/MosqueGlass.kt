package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.OpticalSizeValue
import dev.chrisbanes.haze.glass.hazeGlass

@Immutable
internal data class MosqueGlass(
    val input: HazeInput,
    val darkTheme: Boolean,
    val onMap: Boolean,
)

@Composable
internal fun MosqueGlassPill(
    glass: MosqueGlass,
    pressScale: State<Float>,
    morphProgress: State<Float>,
    highlightAtEnd: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val fill = if (glass.darkTheme) colors.surfaceContainerHigh else colors.surface
    val style = remember(fill, glass.darkTheme) {
        GlassStyle.regular.then {
            shape(CircleShape)
            backgroundColor(fill.copy(alpha = 0.70f))
            tint(fill.copy(alpha = if (glass.darkTheme) 0.48f else 0.35f))
            optics {
                copy(blurRadius = OpticalSizeValue.Fixed(10.dp), refractionDisplacement = 9.dp)
            }
            edgeSoftness(1.dp)
            specularIntensity(if (glass.darkTheme) 0.23f else 0.40f)
        }
    }
    val coating = fill.copy(
        alpha = when {
            glass.onMap && glass.darkTheme -> 0.73f
            glass.onMap -> 0.68f
            glass.darkTheme -> 0.42f
            else -> 0.16f
        },
    )
    val rim = remember(glass.darkTheme, colors.outline) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (glass.darkTheme) 0.36f else 0.97f),
                colors.outline.copy(alpha = if (glass.darkTheme) 0.48f else 0.50f),
            ),
        )
    }
    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier.matchParentSize()
                .graphicsLayer {
                    val compression = 1f - pressScale.value
                    val progress = morphProgress.value
                    val morph = 4f * progress * (1f - progress)
                    scaleX = 1f + compression * 0.58f
                    scaleY = 1f - compression * 2.2f - morph * 0.07f
                }
                .shadow(2.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .border(1.dp, rim, CircleShape)
                .drawWithCache {
                    val sheen = Brush.verticalGradient(
                        0f to Color.White.copy(alpha = if (glass.darkTheme) 0.11f else 0.55f),
                        0.22f to Color.White.copy(alpha = if (glass.darkTheme) 0.03f else 0.16f),
                        0.66f to Color.Transparent,
                        1f to Color.Black.copy(alpha = if (glass.darkTheme) 0.16f else 0.05f),
                    )
                    val halfHeight = size.height / 2f
                    val highlightX = when {
                        !highlightAtEnd -> size.width / 2f
                        layoutDirection == LayoutDirection.Rtl -> halfHeight
                        else -> size.width - halfHeight
                    }
                    val pressSheen = Brush.radialGradient(
                        0f to MosqueActionColor.copy(alpha = if (glass.darkTheme) 0.46f else 0.38f),
                        0.55f to MosqueActionColor.copy(alpha = 0.16f),
                        1f to Color.Transparent,
                        center = Offset(highlightX, size.height * 0.24f),
                        radius = size.height * 1.35f,
                    )
                    onDrawWithContent {
                        drawContent()
                        // Keeps text legible even if the native map is unavailable to the blur source.
                        drawRect(coating)
                        drawRect(sheen)
                        val press = ((1f - pressScale.value) / 0.06f).coerceIn(0f, 1f)
                        if (press > 0f) drawRect(pressSheen, alpha = press)
                    }
                }
                .hazeGlass(
                    input = glass.input,
                    style = style,
                    performanceMode = HazePerformanceMode.Performance,
                ),
        )
        Box(Modifier.matchParentSize().clip(CircleShape), content = content)
    }
}
