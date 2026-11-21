package uz.turgunboyevjurabek.muslimapp.feature.data.location

import android.hardware.GeomagneticField
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.CompassReading
import uz.turgunboyevjurabek.muslimapp.core.utils.calculateQiblaDirection
import javax.inject.Inject

class QiblaSensors @Inject constructor(
    private val locations: MyLocationManager,
    private val compass: CompassSensorManager,
) {
    fun start(
        displayRotation: () -> Int,
        onReading: (CompassReading) -> Unit,
        onLocation: (bearing: Float?, declination: Float) -> Unit,
    ): Boolean {
        compass.onDirectionChanged = onReading
        locations.onLocationReceived = { location ->
            if (location == null) {
                onLocation(null, 0f)
            } else {
                val field = GeomagneticField(
                    location.latitude.toFloat(),
                    location.longitude.toFloat(),
                    location.altitude.toFloat(),
                    System.currentTimeMillis(),
                )
                compass.setMagneticReference(field.fieldStrength / 1_000f, field.inclination)
                val bearing = calculateQiblaDirection(location.latitude, location.longitude)
                onLocation(bearing.toFloat(), field.declination)
            }
        }
        return compass.registerListeners(displayRotation)
    }

    fun requestLocation() = locations.getCurrentLocation()

    fun stop() {
        compass.unregisterListeners()
        compass.onDirectionChanged = null
        locations.cancelRequest()
        locations.onLocationReceived = null
    }
}
