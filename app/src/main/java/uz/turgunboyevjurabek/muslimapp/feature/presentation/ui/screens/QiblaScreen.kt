package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.ui.res.stringResource
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import androidx.annotation.DrawableRes
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.core.utils.vibrateDevice
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla.QiblaState
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.ShimmerPlaceholder
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(
    modifier: Modifier = Modifier,
    qiblaState: StateFlow<QiblaState>,
    compassAvailable: Boolean,
    topPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    val state = qiblaState.collectAsStateWithLifecycle()
    var showCalibration by rememberSaveable { mutableStateOf(false) }
    val bearing by remember(state) { derivedStateOf(structuralEqualityPolicy()) { state.value.qiblaDirection } }
    val facingQibla by remember(state, compassAvailable) {
        derivedStateOf(structuralEqualityPolicy()) { compassAvailable && state.value.aligned }
    }
    LaunchedEffect(facingQibla) {
        if (facingQibla) vibrateDevice(context, 200)
    }
    val status by remember(state, compassAvailable) {
        derivedStateOf(structuralEqualityPolicy()) { qiblaStatus(state.value, compassAvailable) }
    }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = topPadding + 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            QiblaCompassDial(state, facingQibla)
            val bearingText = bearing?.let { "${it.roundToInt().mod(360)}°" } ?: "—"
            val bearingDescription = stringResource(R.string.qibla_direction, bearingText)
            AnimatedValueText(
                text = bearingText,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = bearingDescription
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        QiblaStatusCard(status, facingQibla) { showCalibration = true }
    }
    if (showCalibration) {
        CompassCalibrationDialog(state) { showCalibration = false }
    }
}

internal data class QiblaStatus(
    @DrawableRes val icon: Int,
    val title: Int,
    val showCalibration: Boolean = false,
    val loading: Boolean = false,
)

internal fun qiblaStatus(state: QiblaState, compassAvailable: Boolean): QiblaStatus {
    val turn = state.turn
    val degrees = turn?.let { abs(it).roundToInt() }
    return when {
        !compassAvailable -> QiblaStatus(R.drawable.ic_qibla_warning, R.string.no_compass)
        state.locationUnavailable -> QiblaStatus(R.drawable.ic_qibla_warning, R.string.location_unavailable)
        state.qiblaDirection == null -> QiblaStatus(R.drawable.ic_qibla_location, R.string.locating, loading = true)
        state.aligned -> QiblaStatus(R.drawable.ic_qibla_check, R.string.qibla_found)
        state.approximatelyAligned && !state.compass.canConfirm ->
            QiblaStatus(R.drawable.ic_qibla_target, R.string.qibla_approx, showCalibration = true)
        degrees != null && degrees <= 3 && state.rawTurn != null && state.compass.canConfirm ->
            QiblaStatus(R.drawable.ic_qibla_target, R.string.hold_still)
        degrees != null && degrees <= 3 && state.rawTurn != null ->
            QiblaStatus(R.drawable.ic_qibla_target, R.string.near_qibla, showCalibration = true)
        !state.compass.reliable || turn == null || degrees == null ->
            QiblaStatus(R.drawable.ic_qibla_warning, R.string.low_accuracy, showCalibration = true)
        turn < 0f -> QiblaStatus(R.drawable.ic_qibla_turn_left, R.string.turn_left)
        else -> QiblaStatus(R.drawable.ic_qibla_turn_right, R.string.turn_right)
    }
}

@Composable
private fun QiblaStatusCard(status: QiblaStatus, facingQibla: Boolean, onCalibrate: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(
        if (facingQibla) colors.primaryContainer else colors.surfaceContainerLow, label = "Qibla status background",
    )
    val foreground by animateColorAsState(
        if (facingQibla) colors.onPrimaryContainer else colors.onSurface, label = "Qibla status text",
    )
    Surface(
        modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = background,
        contentColor = foreground,
    ) {
        AnimatedContent(
            targetState = status,
            transitionSpec = { fadeIn(tween(140, delayMillis = 90)) togetherWith fadeOut(tween(90)) },
            label = "Qibla status",
        ) { current ->
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val confirmed = current.icon == R.drawable.ic_qibla_check
                Box(
                    modifier = Modifier.size(30.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = if (confirmed) Modifier.size(28.dp).background(colors.primary, CircleShape) else Modifier,
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(current.icon),
                            contentDescription = null,
                            tint = if (confirmed) colors.onPrimary else colors.primary,
                            modifier = Modifier.size(if (confirmed) 18.dp else 23.dp),
                        )
                    }
                }
                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = colors.outline.copy(alpha = 0.42f),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(stringResource(current.title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (current.loading) {
                        ShimmerPlaceholder(Modifier.width(88.dp).height(4.dp), cornerRadius = 2.dp)
                    }
                    if (current.showCalibration) {
                        val interaction = remember { MutableInteractionSource() }
                        TextButton(
                            onClick = onCalibrate,
                            interactionSource = interaction,
                            modifier = Modifier.pressScale(rememberPressScale(interaction)),
                            contentPadding = PaddingValues(horizontal = 0.dp),
                        ) {
                            Text(stringResource(R.string.calibrate))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompassCalibrationDialog(state: State<QiblaState>, onDismiss: () -> Unit) {
    val ready by remember(state) {
        derivedStateOf(structuralEqualityPolicy()) { state.value.compass.canConfirm }
    }
    val interaction = remember { MutableInteractionSource() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.calibrate_compass)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.calibrate_metal))
                Text(stringResource(R.string.calibrate_motion))
                AnimatedValueText(
                    if (ready) stringResource(R.string.accuracy_ready) else stringResource(R.string.accuracy_still_low),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                interactionSource = interaction,
                modifier = Modifier.pressScale(rememberPressScale(interaction)),
            ) {
                Text(stringResource(R.string.close))
            }
        },
    )
}
