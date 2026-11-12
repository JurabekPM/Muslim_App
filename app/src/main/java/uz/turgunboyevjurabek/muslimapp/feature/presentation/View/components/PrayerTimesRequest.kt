package uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import uz.turgunboyevjurabek.muslimapp.feature.data.network.InternetConnection
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.UIutils.AnimatedValueText
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.ScheduleCache
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.ScheduleLoad
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.prayerZone
import java.time.ZonedDateTime

@Composable
internal fun rememberPrayerClock() = produceState(initialValue = ZonedDateTime.now(prayerZone)) {
    while (true) {
        value = ZonedDateTime.now(prayerZone)
        delay(60_000L - value.second * 1_000L - value.nano / 1_000_000L)
    }
}

@Composable
internal fun <K : Any, T : Any> rememberPrayerTimes(
    cache: ScheduleCache<K, T>,
    key: K?,
    connection: InternetConnection,
): ScheduleLoad<T> {
    LaunchedEffect(cache, key, connection) { key?.let { cache.load(it) } }
    return cache[key]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleRefresh(
    state: ScheduleLoad<*>,
    onRefresh: () -> Unit,
    topPadding: Dp,
    bottomPadding: Dp,
    content: @Composable () -> Unit,
) {
    val pullState = rememberPullToRefreshState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.refreshError) {
        state.refreshError?.let { snackbar.showSnackbar(it) }
    }
    Box(Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
            state = pullState,
            indicator = {
                if (!state.refreshing) {
                    PullToRefreshDefaults.Indicator(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = topPadding),
                        state = pullState,
                        isRefreshing = false,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            },
        ) {
            content()
        }
        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = bottomPadding + 8.dp),
        )
    }
}

@Composable
internal fun PrayerTimesError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedValueText(text = message, textAlign = TextAlign.Center)
        val interactionSource = remember { MutableInteractionSource() }
        Button(
            onClick = onRetry,
            interactionSource = interactionSource,
            modifier = Modifier.pressScale(rememberPressScale(interactionSource)),
        ) {
            Text("Qayta urinish")
        }
    }
}
