@file:OptIn(ExperimentalMaterial3Api::class)

package uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.OpticalSizeValue
import dev.chrisbanes.haze.glass.hazeGlass
import kotlinx.coroutines.delay
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.data.network.InternetConnection
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.UIutils.screenFadeInMillis
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.UIutils.screenFadeOutMillis

private val glassFadeOverlap = 22.dp
private val glassFadeAboveEdge = 24.dp

@Composable
fun MyTopBar(
    modifier: Modifier,
    navController: NavController,
    screenName: String,
    backdrop: HazeState,
    connection: InternetConnection,
    refreshing: Boolean,
    onSheetClick: () -> Unit,
) {
    val onQiblaScreen = screenName == "Qibla"
    val colors = MaterialTheme.colorScheme
    val glassColor = colors.surface
    val darkGlass = glassColor.luminance() < 0.35f
    val input = remember(backdrop) { HazeInput.Sources(backdrop) }
    val glass = remember(glassColor, darkGlass) {
        GlassStyle.regular.then {
            shape(RoundedCornerShape(0.dp))
            backgroundColor(glassColor.copy(alpha = 0.76f))
            tint(glassColor.copy(alpha = if (darkGlass) 0.69f else 0.63f))
            optics { copy(blurRadius = OpticalSizeValue.Fixed(16.dp), refractionDisplacement = 0.dp) }
            specularIntensity(0f)
            ambientResponse(0f)
            edgeShadow(Color.Transparent)
        }
    }

    Box(modifier.fillMaxWidth().zIndex(1f)) {
        Box(
            Modifier.matchParentSize()
                .layout { measurable, constraints ->
                    // Extend the glass over the content without adding to the AppBar height.
                    val glassHeight = constraints.maxHeight + glassFadeOverlap.roundToPx()
                    val layer = measurable.measure(constraints.copy(minHeight = glassHeight, maxHeight = glassHeight))
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        layer.place(0, 0)
                    }
                }
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithCache {
                    val fadeStart = (size.height - (glassFadeAboveEdge + glassFadeOverlap).toPx())
                        .coerceAtLeast(0f)
                    val fadeStops = Array(13) { index ->
                        val progress = index / 12f
                        val eased = progress * progress * (3f - 2f * progress)
                        progress to Color.Black.copy(alpha = 1f - eased)
                    }
                    val fade = Brush.verticalGradient(
                        *fadeStops,
                        startY = fadeStart,
                        endY = size.height,
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(fade, blendMode = BlendMode.DstIn)
                    }
                }
                .hazeGlass(input = input, style = glass, performanceMode = HazePerformanceMode.Performance),
        )

        CenterAlignedTopAppBar(
            expandedHeight = 58.dp,
            windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = colors.primary,
            ),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppBarTitle(if (screenName == "Asosiy") "Namoz vaqtlari" else screenName)
                    ConnectionSubtitle(connection, refreshing)
                }
            },
            navigationIcon = {
                if (onQiblaScreen) {
                    AppBarIconButton(R.drawable.ic_qibla_back, "Orqaga") { navController.popBackStack() }
                } else {
                    AppBarIconButton(R.drawable.ic_qibla_location, "Hududni tanlash", onSheetClick)
                }
            },
            actions = {
                if (!onQiblaScreen) {
                    AppBarIconButton(R.drawable.ic_qibla_kaaba, "Qibla") {
                        if (navController.currentDestination?.route != "QiblaScreen") {
                            navController.navigate("QiblaScreen")
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun AppBarTitle(title: String) {
    var displayedTitle by remember { mutableStateOf(title) }
    val opacity = remember { Animatable(1f) }
    LaunchedEffect(title) {
        if (displayedTitle != title) {
            opacity.animateTo(0f, tween(screenFadeOutMillis))
            displayedTitle = title
        }
        opacity.animateTo(1f, tween(screenFadeInMillis))
    }
    Text(
        text = displayedTitle,
        modifier = Modifier.graphicsLayer { alpha = opacity.value },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private enum class ConnectionNotice(val title: String, val busy: Boolean = false) {
    Connecting("Ulanmoqda…", busy = true),
    Offline("Internet yo‘q"),
    Restored("Internet ulandi"),
    Refreshing("Yangilanmoqda…", busy = true),
}

@Composable
private fun ConnectionSubtitle(connection: InternetConnection, refreshing: Boolean) {
    val colors = MaterialTheme.colorScheme
    var interrupted by remember { mutableStateOf(false) }
    var connecting by remember { mutableStateOf(false) }
    var restored by remember { mutableStateOf(false) }
    LaunchedEffect(connection) {
        connecting = false
        restored = false
        when (connection) {
            InternetConnection.Offline -> interrupted = true
            InternetConnection.Connecting -> {
                delay(260)
                interrupted = true
                connecting = true
            }
            InternetConnection.Online -> if (interrupted) {
                interrupted = false
                restored = true
                delay(1600)
                restored = false
            }
        }
    }
    val notice = when {
        connection == InternetConnection.Offline -> ConnectionNotice.Offline
        connection == InternetConnection.Connecting && (connecting || interrupted) -> ConnectionNotice.Connecting
        connection == InternetConnection.Online && refreshing -> ConnectionNotice.Refreshing
        connection == InternetConnection.Online && restored -> ConnectionNotice.Restored
        else -> null
    }
    AnimatedContent(
        targetState = notice,
        contentAlignment = Alignment.Center,
        transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(135)) },
        label = "Internet status",
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    ) { current ->
        if (current != null) {
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (current.busy) {
                    CircularProgressIndicator(Modifier.size(10.dp), color = colors.tertiary, strokeWidth = 1.5.dp)
                } else {
                    Box(Modifier.size(5.dp).background(colors.tertiary, CircleShape))
                }
                Text(
                    current.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AppBarIconButton(@DrawableRes icon: Int, label: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier.padding(horizontal = 4.dp)
            .pressScale(rememberPressScale(interaction))
            .minimumInteractiveComponentSize()
            .size(42.dp),
        shape = CircleShape,
        color = colors.surfaceContainerHigh.copy(alpha = 0.84f),
        contentColor = colors.tertiary,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
