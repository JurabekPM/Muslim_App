package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import android.os.Build
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.circle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.CounterViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedCounter
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.CounterTapCadence
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberTasbexPressLight
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.TasbexPressIcon
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.ShimmerPlaceholder
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.tasbexOutlineProgress
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.tasbexTouchRimLight
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.shape.MorphPolygonShape

private enum class TasbexDialog { History, Reset, Goal }

@Composable
fun TasbexScreen(viewModel: CounterViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val counter = state
    if (counter == null) {
        Box(Modifier.fillMaxSize().padding(top = 50.dp), contentAlignment = Alignment.TopCenter) {
            ShimmerPlaceholder(
                Modifier.size(width = 88.dp, height = 56.dp),
                description = stringResource(R.string.loading_tasbih),
            )
        }
        return
    }

    var dialog by rememberSaveable { mutableStateOf<TasbexDialog?>(null) }
    val resetId = counter.history.firstOrNull()?.finishedAtMillis
    val view = LocalView.current
    val tapCadence = remember { CounterTapCadence() }
    var rollDurationMillis by remember { mutableIntStateOf(CounterTapCadence.SlowDurationMillis) }
    fun recordTap() {
        rollDurationMillis = tapCadence.recordTap(SystemClock.uptimeMillis())
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 620.dp

        Column(
            modifier = Modifier.fillMaxSize().padding(top = if (compact) 0.dp else 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TasbexGoalPicker(counter.target) { dialog = TasbexDialog.Goal }
            AnimatedCounter(
                count = counter.count,
                resetId = resetId,
                rollDurationMillis = rollDurationMillis,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                ),
            )
            CounterMessage(hasCount = counter.count > 0, resetId = resetId)

            Spacer(Modifier.height(if (compact) 10.dp else 14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                TasbexAction(
                    icon = R.drawable.ic_tasbex_history,
                    label = stringResource(R.string.tasbih_history_short),
                    modifier = Modifier.weight(1f),
                    onClick = { dialog = TasbexDialog.History },
                )
                TasbexAction(
                    icon = R.drawable.ic_tasbex_minus,
                    label = "−1",
                    description = stringResource(R.string.tasbih_decrement),
                    modifier = Modifier.weight(1f),
                    enabled = counter.count > 0,
                    onClick = {
                        recordTap()
                        viewModel.decrementCounter()
                    },
                )
                TasbexAction(
                    icon = R.drawable.ic_tasbex_reset,
                    label = stringResource(R.string.tasbih_reset_short),
                    modifier = Modifier.weight(1f),
                    enabled = counter.count > 0,
                    onClick = { dialog = TasbexDialog.Reset },
                )
            }
            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                val topPadding = minOf(if (compact) 12.dp else 20.dp, maxHeight / 4)
                val bottomPadding = minOf(if (compact) 10.dp else 20.dp, maxHeight / 4)
                val shapeSize = minOf(350.dp, maxWidth - 24.dp, maxHeight - topPadding - bottomPadding)
                    .coerceAtLeast(0.dp)
                ShapeCount(
                    shapeSize = shapeSize,
                    touchCenter = DpOffset(maxWidth / 2, topPadding + shapeSize / 2),
                    topPadding = topPadding,
                    bottomPadding = bottomPadding,
                    fraction = counter.target?.let { counter.count.coerceAtMost(it).toFloat() / it },
                    onClick = {
                        recordTap()
                        viewModel.incrementCounter { reachedTarget ->
                            if (view.isAttachedToWindow) {
                                val feedback = when {
                                    !reachedTarget -> HapticFeedbackConstants.KEYBOARD_TAP
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CONFIRM
                                    else -> HapticFeedbackConstants.LONG_PRESS
                                }
                                view.performHapticFeedback(feedback)
                            }
                        }
                    },
                )
            }
        }
    }

    when (dialog) {
        TasbexDialog.History -> TasbexHistorySheet(counter.history) { dialog = null }
        TasbexDialog.Goal -> TasbexGoalSheet(counter.target, onDismiss = { dialog = null }, onSelect = viewModel::setTarget)
        TasbexDialog.Reset -> TasbexResetDialog(
            count = counter.count,
            onDismiss = { dialog = null },
            onConfirm = {
                tapCadence.reset()
                viewModel.clearCounter()
                dialog = null
            },
        )
        null -> Unit
    }
}

@Composable
private fun TasbexAction(
    icon: Int,
    label: String,
    modifier: Modifier,
    description: String = label,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = MaterialTheme.colorScheme
    val alpha = if (enabled) 1f else 0.38f
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel = description,
                onClick = onClick,
            )
            .pressScale(rememberPressScale(interactionSource))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier.size(42.dp).background(colors.surfaceColorAtElevation(8.dp), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = colors.primary.copy(alpha = alpha),
            )
        }
        Text(
            text = label,
            modifier = Modifier.semantics { if (description != label) contentDescription = description },
            color = colors.onSurfaceVariant.copy(alpha = alpha),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CounterMessage(hasCount: Boolean, resetId: Long?) {
    var visible by remember(resetId) { mutableStateOf(true) }
    LaunchedEffect(resetId) {
        delay(5_000)
        visible = false
    }
    val opacity = animateFloatAsState(if (visible) 1f else 0f, tween(280), label = "Tasbix hint")
    AnimatedValueText(
        text = stringResource(if (hasCount) R.string.continue_dhikr else R.string.new_dhikr),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .then(if (visible) Modifier else Modifier.clearAndSetSemantics {})
            .graphicsLayer { alpha = opacity.value },
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        contentAlignment = Alignment.Center,
    )
}

@Composable
private fun ShapeCount(
    shapeSize: Dp,
    touchCenter: DpOffset,
    topPadding: Dp,
    bottomPadding: Dp,
    fraction: Float?,
    onClick: () -> Unit,
) {
    val morph = remember {
        Morph(RoundedPolygon(12, rounding = CornerRounding(0.2f)), RoundedPolygon.circle(15))
    }
    val interactionSource = remember { MutableInteractionSource() }
    val pressLight = rememberTasbexPressLight(interactionSource)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.filterIsInstance<PressInteraction>().collectLatest { press ->
            when (press) {
                is PressInteraction.Press -> progress.animateTo(1f, tween(95))
                is PressInteraction.Release -> {
                    // Even a tap that ends before the first frame reaches the circle.
                    if (progress.value < 1f) progress.animateTo(1f, tween(65))
                    progress.animateTo(0f, tween(180))
                }
                is PressInteraction.Cancel -> progress.animateTo(0f, tween(160))
            }
        }
    }
    val colors = MaterialTheme.colorScheme
    val dark = colors.background.luminance() < 0.3f
    val shape = MorphPolygonShape(morph, progress.value)
    val pressScale = rememberPressScale(interactionSource)
    val pressure by remember(pressScale) {
        derivedStateOf { ((1f - pressScale.value) / 0.06f).coerceIn(0f, 1f) }
    }
    val shadowColor = if (dark) Color.Black else colors.onSurface
    val ambientOpacity = if (dark) 0.46f else 0.15f
    val contactOpacity = if (dark) 0.44f else 0.15f
    val gold = if (dark) colors.tertiary else Color(0xFFB98525)
    val label = stringResource(R.string.dhikr)
    val hasOutline = fraction != null
    val progressOutline = if (fraction == null) {
        Modifier
    } else {
        Modifier.tasbexOutlineProgress(shape, fraction, dark, gold, pressLight)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = label
                if (fraction != null) progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(top = topPadding, bottom = bottomPadding),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(shapeSize)
                .pressScale(pressScale)
                .dropShadow(shape) {
                    color = shadowColor
                    radius = (if (hasOutline) 18.dp - 6.dp * pressure else 18.dp).toPx()
                    offset = Offset(0f, (9.dp - 6.dp * pressure).toPx())
                    alpha = ambientOpacity * (1f - 0.38f * pressure)
                }
                .dropShadow(shape) {
                    color = shadowColor
                    radius = (if (hasOutline) 6.dp - 2.dp * pressure else 6.dp).toPx()
                    offset = Offset(0f, (4.dp - 3.dp * pressure).toPx())
                    alpha = contactOpacity
                }
                .tasbexTouchRimLight(shape, touchCenter, dark, pressLight)
                .clip(shape)
                .background(colors.surfaceColorAtElevation(10.dp))
                .then(progressOutline),
            contentAlignment = Alignment.Center,
        ) {
            TasbexPressIcon(interactionSource, tint = colors.primary, glow = gold)
        }
    }
}
