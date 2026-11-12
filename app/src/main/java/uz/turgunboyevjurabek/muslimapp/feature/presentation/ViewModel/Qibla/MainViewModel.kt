package uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.Qibla

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.UIutils.QiblaState
import uz.turgunboyevjurabek.qiblafinderexample.service.CompassReading
import uz.turgunboyevjurabek.muslimapp.core.utils.QiblaAlignment
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val alignment = QiblaAlignment()
    private val approximateAlignment = QiblaAlignment()
    private val _qiblaState = MutableStateFlow(QiblaState())
    val qiblaState = _qiblaState.asStateFlow()

    fun beginLocationLookup() {
        alignment.reset()
        approximateAlignment.reset()
        _qiblaState.update { it.copy(qiblaDirection = null, declination = 0f, locationUnavailable = false, aligned = false, approximatelyAligned = false) }
    }

    fun updateQiblaDirection(direction: Float?, declination: Float = 0f) {
        alignment.reset()
        approximateAlignment.reset()
        _qiblaState.update {
            it.copy(qiblaDirection = direction, declination = declination, locationUnavailable = direction == null, aligned = false, approximatelyAligned = false)
        }
    }

    fun updateCurrentDirection(reading: CompassReading = CompassReading(null)) {
        val next = _qiblaState.value.copy(compass = reading)
        val aligned = alignment.update(next.turn, next.rawTurn, reading.canConfirm, reading.timestamp)
        val approximatelyAligned = approximateAlignment.update(next.turn, next.rawTurn, true, reading.timestamp)
        _qiblaState.value = next.copy(aligned = aligned, approximatelyAligned = approximatelyAligned)
    }
}
