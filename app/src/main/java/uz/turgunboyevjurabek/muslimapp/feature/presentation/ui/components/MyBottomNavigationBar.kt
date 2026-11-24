package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.ui.res.stringResource
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.OpticalSizeValue
import dev.chrisbanes.haze.glass.hazeGlass
import kotlin.math.abs
import kotlin.math.roundToInt
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation.AppRoute
import uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation.BottomNavigationItem

private val dockItems = listOf(
    BottomNavigationItem(R.string.home, R.drawable.ic_nav_home, R.drawable.ic_nav_home_filled, AppRoute.HOME),
    BottomNavigationItem(R.string.tasbih, R.drawable.ic_nav_tasbex, R.drawable.ic_nav_tasbex_filled, AppRoute.TASBEX),
    BottomNavigationItem(R.string.weekly, R.drawable.ic_nav_week, R.drawable.ic_nav_week_filled, AppRoute.WEEKLY),
    BottomNavigationItem(R.string.monthly, R.drawable.ic_nav_month, R.drawable.ic_nav_month_filled, AppRoute.MONTHLY),
    BottomNavigationItem(R.string.menu, R.drawable.ic_nav_menu, R.drawable.ic_nav_menu_filled, AppRoute.MENU),
)
private val dockShape = RoundedCornerShape(30.dp)
private val dockFollowSpring = spring(dampingRatio = 1f, stiffness = 1800f, visibilityThreshold = 0.002f)
private val dockSettleSpring = spring(dampingRatio = 0.74f, stiffness = 430f, visibilityThreshold = 0.002f)

internal fun titleForRoute(route: String?): Int = when (route) {
    AppRoute.QIBLA -> R.string.qibla
    AppRoute.HOME, null -> R.string.prayer_times
    else -> dockItems.firstOrNull { it.route == route }?.title ?: R.string.prayer_times
}

@Composable
fun MyBottomNavigationBar(
    navController: NavController,
    backdrop: HazeState,
    modifier: Modifier = Modifier,
) {
    val items = dockItems
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: AppRoute.HOME
    val colors = MaterialTheme.colorScheme
    val interactionSources = remember { List(items.size) { MutableInteractionSource() } }
    val pressScales = interactionSources.map { rememberPressScale(it, bounce = true) }
    val pressedTabs = interactionSources.map { it.collectIsPressedAsState().value }
    val selectedIndex = items.indexOfFirst { it.route == route }
    val pressedIndex = pressedTabs.indexOfFirst { it }
    val previewIndex = if (pressedIndex >= 0) pressedIndex else selectedIndex
    val dragPosition = remember { mutableStateOf<Float?>(null) }
    val dragging by remember { derivedStateOf { dragPosition.value != null } }
    val position = animateDockPosition(dragPosition, previewIndex)
    val dragStretch = animateFloatAsState(
        targetValue = if (dragging) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 470f),
        label = "Liquid indicator stretch",
    )
    fun openTab(index: Int) {
        val destination = items[index].route
        // Qibla is opened above a tab and must be dismissed before saving that tab.
        navController.dismissQibla()
        if (navController.currentDestination?.route == destination) return

        navController.navigate(destination) {
            popUpTo(AppRoute.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }

        // Older versions may already have saved Qibla above a tab.
        navController.dismissQibla()
        if (navController.currentDestination?.route != destination) {
            navController.navigate(destination) {
                popUpTo(AppRoute.HOME)
                launchSingleTop = true
            }
        }
    }
    val indicatorOpacity = animateFloatAsState(
        targetValue = if (previewIndex >= 0 || dragging) 1f else 0f,
        animationSpec = tween(150),
        label = "Tab highlight opacity",
    )
    val indicatorScale = pressScales.getOrNull(previewIndex)
    val dockInput = remember(backdrop) { HazeInput.Sources(backdrop) }
    val dockFill = colors.surfaceContainerHigh
    val darkDock = dockFill.luminance() < 0.35f
    val dockRim = remember(darkDock, colors.outline) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (darkDock) 0.17f else 0.63f),
                colors.outline.copy(alpha = if (darkDock) 0.22f else 0.32f),
            ),
        )
    }
    val dockGlass = remember(dockFill, darkDock) {
        GlassStyle.regular.then {
            shape(dockShape)
            backgroundColor(dockFill.copy(alpha = 0.65f))
            tint(dockFill.copy(alpha = if (darkDock) 0.44f else 0.32f))
            optics {
                copy(
                    blurRadius = OpticalSizeValue.Fixed(4.dp),
                    refractionDisplacement = 10.dp,
                )
            }
            edgeSoftness(1.dp)
            specularIntensity(if (darkDock) 0.16f else 0.28f)
            ambientResponse(if (darkDock) 0.08f else 0.16f)
        }
    }
    Box(
        modifier = modifier.fillMaxWidth().navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(),
        ) {
            Box(
                Modifier.matchParentSize()
                    .shadow(3.dp, dockShape, clip = false)
                    .clip(dockShape)
                    .border(0.75.dp, dockRim, dockShape)
                    .drawWithContent {
                        drawContent()
                        if (darkDock) drawRect(colors.surfaceContainerLow.copy(alpha = 0.82f))
                    }
                    .hazeGlass(input = dockInput, style = dockGlass),
            )
            BoxWithConstraints(modifier = Modifier.padding(4.dp)) {
                val spacing = 3.dp
                val tabWidth = (maxWidth - spacing * (items.size - 1)) / items.size
                val tabStride = with(LocalDensity.current) { (tabWidth + spacing).toPx() }
                val dragDirection = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
                Box(Modifier.matchParentSize()) {
                    DockIndicator(
                        modifier = Modifier.width(tabWidth).fillMaxHeight().graphicsLayer {
                            val farEdge = items.lastIndex.toFloat()
                            val current = position.value.coerceIn(0f, farEdge)
                            val pull = abs((dragPosition.value ?: current) - current).coerceAtMost(0.45f)
                            val stretch = dragStretch.value.coerceIn(0f, 1f)
                            val compression = 1f - (indicatorScale?.value ?: 1f)
                            val edgeRebound = abs(position.value - current).coerceAtMost(0.18f)
                            val fromStart = current / farEdge
                            transformOrigin = TransformOrigin(
                                if (dragDirection > 0f) fromStart else 1f - fromStart, 0.5f,
                            )
                            translationX = tabStride * current * dragDirection
                            scaleX = 1f + compression * 0.65f + stretch * (0.18f + pull * 0.21f)
                            scaleY = 1f - compression * 2.05f + stretch * 0.16f + edgeRebound * 0.42f
                        },
                        opacity = indicatorOpacity,
                        dragStretch = dragStretch,
                        pressScale = indicatorScale,
                        darkDock = darkDock,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().selectableGroup()
                        .pointerInput(tabStride, dragDirection, items.size) {
                            fun tabAt(x: Float): Float {
                                val distanceFromStart = if (dragDirection > 0f) x else size.width - x
                                val firstTabCenter = tabStride * 0.5f - spacing.toPx() * 0.5f
                                return ((distanceFromStart - firstTabCenter) / tabStride)
                                    .coerceIn(0f, items.lastIndex.toFloat())
                            }

                            detectHorizontalDragGestures(
                                onDragStart = { dragPosition.value = tabAt(it.x) },
                                onDragCancel = { dragPosition.value = null },
                                onDragEnd = {
                                    dragPosition.value?.roundToInt()?.let(::openTab)
                                    dragPosition.value = null
                                },
                            ) { change, _ ->
                                dragPosition.value = tabAt(change.position.x)
                            }
                        },
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    items.forEachIndexed { index, item ->
                        DockTab(
                            modifier = Modifier.weight(1f),
                            item = item,
                            index = index,
                            selected = index == selectedIndex,
                            previewVisible = previewIndex >= 0 || dragging,
                            position = position,
                            dragStretch = dragStretch,
                            interaction = interactionSources[index],
                            pressScale = pressScales[index],
                            onClick = { openTab(index) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun animateDockPosition(dragPosition: State<Float?>, previewIndex: Int): State<Float> {
    val fingerPosition = dragPosition.value
    return animateFloatAsState(
        targetValue = fingerPosition ?: previewIndex.coerceAtLeast(0).toFloat(),
        animationSpec = if (fingerPosition != null) dockFollowSpring else dockSettleSpring,
        label = "Tab position",
    )
}

@Composable
private fun DockIndicator(
    modifier: Modifier,
    opacity: State<Float>,
    dragStretch: State<Float>,
    pressScale: State<Float>?,
    darkDock: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    val fill = colors.secondaryContainer
    Box(
        modifier.clip(dockShape).drawWithCache {
            val lens = Brush.verticalGradient(
                0f to Color.White.copy(alpha = if (darkDock) 0.26f else 0.48f),
                0.52f to fill.copy(alpha = 0.04f),
                1f to fill.copy(alpha = 0.22f),
            )
            val rim = Brush.linearGradient(
                0f to Color.White.copy(alpha = if (darkDock) 0.55f else 0.94f),
                0.48f to fill.copy(alpha = 0.38f),
                1f to colors.primary.copy(alpha = if (darkDock) 0.67f else 0.52f),
            )
            val sheen = Brush.verticalGradient(
                0f to Color.White.copy(alpha = 0.78f),
                0.55f to Color.Transparent,
            )
            val outline = dockShape.createOutline(size, layoutDirection, this)
            val stroke = Stroke(width = 2.dp.toPx())
            onDrawBehind {
                val alpha = opacity.value
                if (alpha <= 0f) return@onDrawBehind

                val stretch = dragStretch.value.coerceIn(0f, 1f)
                val press = ((1f - (pressScale?.value ?: 1f)) / 0.06f).coerceIn(0f, 1f)
                val reflect = maxOf(press, stretch * 0.72f) * alpha
                drawRect(fill, alpha = alpha * (1f - stretch * 0.66f))
                if (stretch > 0f) drawRect(lens, alpha = alpha * stretch)
                if (reflect > 0f) {
                    drawRect(sheen, alpha = reflect)
                    drawOutline(outline, rim, alpha = reflect, style = stroke)
                }
            }
        },
    )
}

@Composable
private fun DockTab(
    modifier: Modifier,
    item: BottomNavigationItem,
    index: Int,
    selected: Boolean,
    previewVisible: Boolean,
    position: State<Float>,
    dragStretch: State<Float>,
    interaction: MutableInteractionSource,
    pressScale: State<Float>,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val emphasis by remember(position, index, previewVisible) {
        derivedStateOf { if (previewVisible) (1f - abs(index - position.value)).coerceIn(0f, 1f) else 0f }
    }
    val emphasized = emphasis > 0.5f
    val tint = if (emphasis > 0f) {
        val selectedTint = lerp(colors.onSecondaryContainer, colors.onSurface, dragStretch.value.coerceIn(0f, 1f))
        lerp(colors.onSurface, selectedTint, emphasis)
    } else colors.onSurface
    Column(
        modifier = modifier.heightIn(min = 52.dp).clip(dockShape)
            .selectable(selected, interaction, null, role = Role.Tab, onClick = onClick)
            .pressScale(pressScale)
            .padding(horizontal = 2.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Crossfade(
            targetState = emphasized,
            modifier = Modifier.size(22.dp),
            animationSpec = tween(160),
            label = "Tab icon fill",
        ) { filled ->
            Icon(
                painter = painterResource(if (filled) item.filledIcon else item.outlineIcon),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                tint = tint,
            )
        }
        Text(
            stringResource(item.title),
            color = tint,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

private fun NavController.dismissQibla() {
    while (currentDestination?.route == AppRoute.QIBLA) {
        if (!popBackStack()) break
    }
}
