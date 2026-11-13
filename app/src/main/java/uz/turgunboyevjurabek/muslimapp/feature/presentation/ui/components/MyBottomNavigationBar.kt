package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntOffset
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
    BottomNavigationItem("Asosiy", R.drawable.ic_nav_home, R.drawable.ic_nav_home_filled, AppRoute.HOME),
    BottomNavigationItem("Tasbex", R.drawable.ic_nav_tasbex, R.drawable.ic_nav_tasbex_filled, AppRoute.TASBEX),
    BottomNavigationItem("7 Kunlik", R.drawable.ic_nav_week, R.drawable.ic_nav_week_filled, AppRoute.WEEKLY),
    BottomNavigationItem("30 Kunlik", R.drawable.ic_nav_month, R.drawable.ic_nav_month_filled, AppRoute.MONTHLY),
)
private val dockShape = RoundedCornerShape(30.dp)

internal fun screenNameForRoute(route: String?): String =
    if (route == AppRoute.QIBLA) "Qibla" else dockItems.firstOrNull { it.route == route }?.title ?: dockItems.first().title

@Composable
fun myBottomNavigationBar(
    navController: NavController,
    backdrop: HazeState,
    modifier: Modifier = Modifier,
) {
    val items = dockItems
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: AppRoute.HOME
    val colors = MaterialTheme.colorScheme
    val interactionSources = remember { List(items.size) { MutableInteractionSource() } }
    val pressScales = interactionSources.map { rememberPressScale(it) }
    val pressedTabs = interactionSources.map { it.collectIsPressedAsState().value }
    val selectedIndex = items.indexOfFirst { it.route == route }
    val pressedIndex = pressedTabs.indexOfFirst { it }
    val previewIndex = if (pressedIndex >= 0) pressedIndex else selectedIndex
    var dragPosition by remember { mutableStateOf<Float?>(null) }
    val dragging = dragPosition != null
    val position = animateFloatAsState(
        targetValue = dragPosition ?: previewIndex.coerceAtLeast(0).toFloat(),
        animationSpec = if (dragging) {
            spring(dampingRatio = 1f, stiffness = 1800f, visibilityThreshold = 0.002f)
        } else {
            spring(dampingRatio = 0.86f, stiffness = 420f, visibilityThreshold = 0.002f)
        },
        label = "Tab position",
    )
    val dragScale = animateFloatAsState(
        targetValue = if (dragging) 0.94f else 1f,
        animationSpec = tween(if (dragging) 85 else 160),
        label = "Dragged indicator scale",
    )
    val dragStretch = animateFloatAsState(
        targetValue = if (dragging) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 470f),
        label = "Liquid indicator stretch",
    )
    val lensVisible by remember(dragStretch) { derivedStateOf { dragStretch.value > 0.01f } }
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
    val indicatorFill = colors.secondaryContainer
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
                Color.White.copy(alpha = if (darkDock) 0.28f else 0.63f),
                colors.outline.copy(alpha = 0.32f),
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
            specularIntensity(0.28f)
            ambientResponse(0.16f)
        }
    }
    val lensFill = remember(indicatorFill, darkDock) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (darkDock) 0.16f else 0.34f),
                indicatorFill.copy(alpha = 0.04f),
                indicatorFill.copy(alpha = 0.24f),
            ),
        )
    }
    val lensRim = remember(indicatorFill, darkDock, colors.primary) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (darkDock) 0.48f else 0.88f),
                indicatorFill.copy(alpha = 0.35f),
                colors.primary.copy(alpha = if (darkDock) 0.65f else 0.45f),
            ),
        )
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
                    .hazeGlass(input = dockInput, style = dockGlass)
                    .border(0.75.dp, dockRim, dockShape),
            )
            BoxWithConstraints(modifier = Modifier.padding(4.dp)) {
                val spacing = 3.dp
                val tabWidth = (maxWidth - spacing * (items.size - 1)) / items.size
                val tabStride = with(LocalDensity.current) { (tabWidth + spacing).toPx() }
                val dragDirection = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
                Box(Modifier.matchParentSize()) {
                    Box(
                        Modifier.offset { IntOffset((tabStride * position.value).roundToInt(), 0) }
                            .width(tabWidth)
                            .fillMaxHeight()
                            .graphicsLayer {
                                val scale = minOf(dragScale.value, indicatorScale?.value ?: 1f)
                                val currentPosition = position.value
                                val pull = abs((dragPosition ?: currentPosition) - currentPosition).coerceAtMost(0.45f)
                                val stretch = dragStretch.value.coerceAtLeast(0f)
                                val fromStart = (currentPosition / items.lastIndex).coerceIn(0f, 1f)
                                transformOrigin = TransformOrigin(
                                    if (dragDirection > 0f) fromStart else 1f - fromStart,
                                    0.5f,
                                )
                                scaleX = scale * (1f + stretch * (0.20f + pull * 0.20f))
                                scaleY = scale * (1f + stretch * 0.28f)
                            },
                    ) {
                        Box(
                            Modifier.matchParentSize()
                                .graphicsLayer {
                                    alpha = indicatorOpacity.value * (1f - dragStretch.value.coerceIn(0f, 1f) * 0.66f)
                                }
                                .background(indicatorFill, dockShape),
                        )
                        if (dragging || lensVisible) {
                            Box(
                                Modifier.matchParentSize()
                                    .graphicsLayer {
                                        alpha = indicatorOpacity.value * dragStretch.value.coerceIn(0f, 1f)
                                    }
                                    .background(lensFill, dockShape)
                                    .border(1.dp, lensRim, dockShape),
                            )
                        }
                    }
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
                                onDragStart = { dragPosition = tabAt(it.x) },
                                onDragCancel = { dragPosition = null },
                                onDragEnd = {
                                    dragPosition?.roundToInt()?.let(::openTab)
                                    dragPosition = null
                                },
                            ) { change, _ ->
                                dragPosition = tabAt(change.position.x)
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
    val emphasis = if (previewVisible) (1f - abs(index - position.value)).coerceIn(0f, 1f) else 0f
    val selectedTint = lerp(
        colors.onSecondaryContainer,
        colors.onSurface,
        dragStretch.value.coerceIn(0f, 1f),
    )
    val tint = lerp(colors.onSurface, selectedTint, emphasis)
    Column(
        modifier = modifier.heightIn(min = 52.dp).clip(dockShape)
            .selectable(selected, interaction, null, role = Role.Tab, onClick = onClick)
            .pressScale(pressScale)
            .padding(horizontal = 2.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Crossfade(
            targetState = emphasis > 0.5f,
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
            item.title,
            color = tint,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight((500 + 100 * emphasis).roundToInt()),
        )
    }
}

private fun NavController.dismissQibla() {
    while (currentDestination?.route == AppRoute.QIBLA) {
        if (!popBackStack()) break
    }
}
