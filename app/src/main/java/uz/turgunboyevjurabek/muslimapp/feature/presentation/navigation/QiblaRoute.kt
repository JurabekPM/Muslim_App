package uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation

import android.hardware.GeomagneticField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import uz.turgunboyevjurabek.muslimapp.core.utils.calculateQiblaDirection
import uz.turgunboyevjurabek.muslimapp.feature.data.location.CompassSensorManager
import uz.turgunboyevjurabek.muslimapp.feature.data.location.MyLocationManager
import uz.turgunboyevjurabek.muslimapp.feature.data.location.PermissionsManager
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.QiblaScreen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla.QiblaViewModel

@Composable
internal fun QiblaRoute(
    lifecycle: Lifecycle,
    viewModel: QiblaViewModel,
    locationManager: MyLocationManager,
    compassSensorManager: CompassSensorManager,
    permissionsManager: PermissionsManager,
    displayRotation: () -> Int,
    modifier: Modifier,
    topPadding: Dp,
) {
    var compassAvailable by remember { mutableStateOf(true) }
    DisposableEffect(lifecycle) {
        locationManager.onLocationReceived = { location ->
            if (location == null) {
                viewModel.updateQiblaDirection(null)
            } else {
                val bearing = calculateQiblaDirection(location.latitude, location.longitude)
                val field = GeomagneticField(
                    location.latitude.toFloat(),
                    location.longitude.toFloat(),
                    location.altitude.toFloat(),
                    System.currentTimeMillis(),
                )
                compassSensorManager.setMagneticReference(
                    field.fieldStrength / 1000f,
                    field.inclination,
                )
                viewModel.updateQiblaDirection(bearing.toFloat(), field.declination)
            }
        }
        compassSensorManager.onDirectionChanged = viewModel::updateCurrentDirection
        permissionsManager.onPermissionGranted = locationManager::getCurrentLocation
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.beginLocationLookup()
                    compassAvailable = compassSensorManager.registerListeners(displayRotation)
                    locationManager.getCurrentLocation()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    compassSensorManager.unregisterListeners()
                    locationManager.cancelRequest()
                    viewModel.updateCurrentDirection()
                }
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        permissionsManager.checkAndRequestLocationPermission()
        onDispose {
            lifecycle.removeObserver(observer)
            compassSensorManager.unregisterListeners()
            compassSensorManager.onDirectionChanged = null
            locationManager.cancelRequest()
            locationManager.onLocationReceived = null
            permissionsManager.onPermissionGranted = null
            viewModel.updateCurrentDirection()
        }
    }
    QiblaScreen(
        modifier = modifier,
        qiblaState = viewModel.qiblaState,
        compassAvailable = compassAvailable,
        topPadding = topPadding,
    )
}
