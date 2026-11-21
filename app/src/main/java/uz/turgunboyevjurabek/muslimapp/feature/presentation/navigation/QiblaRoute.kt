package uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import uz.turgunboyevjurabek.muslimapp.feature.data.location.QiblaSensors
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla.QiblaViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.QiblaScreen

@Composable
internal fun QiblaRoute(
    lifecycle: Lifecycle,
    viewModel: QiblaViewModel,
    sensors: QiblaSensors,
    permissionsManager: PermissionsManager,
    displayRotation: () -> Int,
    modifier: Modifier,
    topPadding: Dp,
) {
    DisposableEffect(lifecycle, sensors, permissionsManager, viewModel) {
        var active = false
        fun stopSensors() {
            if (!active) return
            active = false
            sensors.stop()
            viewModel.updateCurrentDirection()
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    active = true
                    viewModel.beginLocationLookup()
                    val available = sensors.start(
                        displayRotation, viewModel::updateCurrentDirection, viewModel::updateQiblaDirection,
                    )
                    viewModel.setCompassAvailable(available)
                    sensors.requestLocation()
                }
                Lifecycle.Event.ON_PAUSE -> stopSensors()
                else -> Unit
            }
        }
        val requestIfActive: () -> Unit = { if (active) sensors.requestLocation() }
        permissionsManager.onPermissionGranted = requestIfActive
        lifecycle.addObserver(observer)
        permissionsManager.checkAndRequestLocationPermission()
        onDispose {
            lifecycle.removeObserver(observer)
            stopSensors()
            if (permissionsManager.onPermissionGranted === requestIfActive) {
                permissionsManager.onPermissionGranted = null
            }
        }
    }
    QiblaScreen(modifier = modifier, qiblaState = viewModel.qiblaState, topPadding = topPadding)
}
