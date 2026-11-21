package uz.turgunboyevjurabek.muslimapp.feature.domain.model

data class CompassReading(
    val heading: Float?,
    val rawHeading: Float? = null,
    val reliable: Boolean = false,
    val canConfirm: Boolean = false,
    val timestamp: Long = 0L,
)
