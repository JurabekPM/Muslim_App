package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.turgunboyevjurabek.muslimapp.core.utils.QiblaAlignment
import uz.turgunboyevjurabek.muslimapp.core.utils.signedAngle
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.CompassReading
import javax.inject.Inject

@HiltViewModel
class QiblaViewModel @Inject constructor() : ViewModel() {
    private val alignment = QiblaAlignment()
    private val approximateAlignment = QiblaAlignment()
    private var latestReading = CompassReading(null)
    private var declination = 0f
    private val _qiblaState = MutableStateFlow(QiblaState())
    val qiblaState = _qiblaState.asStateFlow()

    fun beginLocationLookup() = setLocation(null, 0f, unavailable = false)

    fun updateQiblaDirection(direction: Float?, declination: Float = 0f) =
        setLocation(direction, declination, unavailable = direction == null)

    fun setCompassAvailable(available: Boolean) {
        _qiblaState.update { it.copy(compassAvailable = available) }
    }

    fun updateCurrentDirection(reading: CompassReading = CompassReading(null)) {
        latestReading = reading
        val next = _qiblaState.value.withReading()
        val rawTurn = turnTo(next.qiblaDirection, reading.rawHeading)
        val aligned = alignment.update(next.turn, rawTurn, reading.canConfirm, reading.timestamp)
        val approximatelyAligned = approximateAlignment.update(next.turn, rawTurn, true, reading.timestamp)
        _qiblaState.value = next.copy(aligned = aligned, approximatelyAligned = approximatelyAligned)
    }

    private fun setLocation(direction: Float?, declination: Float, unavailable: Boolean) {
        this.declination = declination
        alignment.reset()
        approximateAlignment.reset()
        _qiblaState.update {
            it.copy(
                qiblaDirection = direction, locationUnavailable = unavailable,
                aligned = false, approximatelyAligned = false,
            ).withReading()
        }
    }

    private fun QiblaState.withReading() = copy(
        trueHeading = latestReading.heading?.plus(declination),
        turn = turnTo(qiblaDirection, latestReading.heading),
        hasRawTurn = qiblaDirection != null && latestReading.rawHeading != null,
        reliable = latestReading.reliable,
        canConfirm = latestReading.canConfirm,
    )

    private fun turnTo(bearing: Float?, heading: Float?): Float? {
        if (bearing == null || heading == null) return null
        return signedAngle(bearing - (heading + declination))
    }
}
