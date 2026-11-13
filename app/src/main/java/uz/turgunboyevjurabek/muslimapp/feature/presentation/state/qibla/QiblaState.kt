package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla

import uz.turgunboyevjurabek.muslimapp.core.utils.signedAngle
import uz.turgunboyevjurabek.muslimapp.feature.data.location.CompassReading

data class QiblaState(
    val qiblaDirection: Float? = null,
    val compass: CompassReading = CompassReading(null),
    val declination: Float = 0f,
    val locationUnavailable: Boolean = false,
    val aligned: Boolean = false,
    val approximatelyAligned: Boolean = false,
) {
    val trueHeading: Float? get() = compass.heading?.plus(declination)
    val turn: Float? get() = turnFor(compass.heading)
    val rawTurn: Float? get() = turnFor(compass.rawHeading)

    private fun turnFor(heading: Float?): Float? {
        val bearing = qiblaDirection ?: return null
        return heading?.let { signedAngle(bearing - (it + declination)) }
    }
}
