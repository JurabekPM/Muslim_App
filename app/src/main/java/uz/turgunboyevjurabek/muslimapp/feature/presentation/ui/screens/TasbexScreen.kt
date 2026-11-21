package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import uz.turgunboyevjurabek.muslimapp.core.utils.coloredShadow
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.CounterViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.DhikrSession
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedCounter
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.ShimmerPlaceholder
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.shape.MorphPolygonShape
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

private enum class TasbexDialog { History, Reset }

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
    val view = LocalView.current
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 620.dp
        val counterSize = minOf(350.dp, maxWidth - 24.dp, maxHeight - 215.dp)
            .coerceAtLeast(48.dp)

        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = if (compact) 16.dp else 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedCounter(
                count = counter.count,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFeatureSettings = "tnum",
                ),
            )
            CounterMessage(hasCount = counter.count > 0)
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
                    onClick = viewModel::decrementCounter,
                )
                TasbexAction(
                    icon = R.drawable.ic_tasbex_reset,
                    label = stringResource(R.string.tasbih_reset_short),
                    modifier = Modifier.weight(1f),
                    enabled = counter.count > 0,
                    onClick = { dialog = TasbexDialog.Reset },
                )
            }
            ShapeCount(
                count = counter.count,
                shapeSize = counterSize,
                topPadding = if (compact) 16.dp else 34.dp,
                bottomPadding = if (compact) 12.dp else 30.dp,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    viewModel.incrementCounter()
                },
            )
        }
    }

    when (dialog) {
        TasbexDialog.History -> TasbexHistoryDialog(counter.history) { dialog = null }
        TasbexDialog.Reset -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(stringResource(R.string.reset_count)) },
            text = { Text(stringResource(R.string.tasbih_reset_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCounter()
                    dialog = null
                }) {
                    Text(stringResource(R.string.tasbih_reset_short))
                }
            },
            dismissButton = {
                TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.cancel)) }
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
private fun TasbexHistoryDialog(history: List<DhikrSession>, onDismiss: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val dateFormat = remember(locale) { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale) }
    val numberFormat = remember(locale) { NumberFormat.getIntegerInstance(locale) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dhikr_history)) },
        text = {
            if (history.isEmpty()) {
                Text(stringResource(R.string.no_dhikr_history))
            } else {
                LazyColumn(Modifier.heightIn(max = 240.dp)) {
                    items(history) { session ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(dateFormat.format(Date(session.finishedAtMillis)), Modifier.weight(1f))
                            Text(numberFormat.format(session.count), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
    )
}

@Composable
private fun CounterMessage(hasCount: Boolean) {
    var showText by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(8000L)
        showText = false
    }
    AnimatedVisibility(
        visible = showText,
        enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(3000)),
        exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(3000)),
    ) {
        AnimatedValueText(
            text = stringResource(if (hasCount) R.string.continue_dhikr else R.string.new_dhikr),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            contentAlignment = Alignment.Center,
        )
    }
}

@Composable
private fun ShapeCount(
    count: Int,
    shapeSize: Dp,
    topPadding: Dp,
    bottomPadding: Dp,
    onClick: () -> Unit,
) {
    val shadowAlpha = if (count < 10) 0.01f else (count / 10).coerceAtMost(10) * 0.05f
    val morph = remember {
        Morph(RoundedPolygon(12, rounding = CornerRounding(0.2f)), RoundedPolygon.circle(15))
    }
    val interactionSource = remember { MutableInteractionSource() }
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
    val label = stringResource(R.string.dhikr)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = label }
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
                .coloredShadow(
                    colors.primary,
                    borderRadius = 150.dp,
                    alpha = shadowAlpha,
                    shadowRadius = 50.dp,
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                )
                .pressScale(rememberPressScale(interactionSource))
                .clip(MorphPolygonShape(morph, progress.value))
                .background(colors.surfaceColorAtElevation(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_nav_tasbex),
                contentDescription = null,
                modifier = Modifier.size(46.dp),
                tint = colors.primary,
            )
        }
    }
}
