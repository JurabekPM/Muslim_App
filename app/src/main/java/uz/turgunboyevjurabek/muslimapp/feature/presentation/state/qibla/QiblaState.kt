package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla

data class QiblaState(
    val qiblaDirection: Float? = null,
    val trueHeading: Float? = null,
    val turn: Float? = null,
    val hasRawTurn: Boolean = false,
    val reliable: Boolean = false,
    val canConfirm: Boolean = false,
    val compassAvailable: Boolean = true,
    val locationUnavailable: Boolean = false,
    val aligned: Boolean = false,
    val approximatelyAligned: Boolean = false,
)
