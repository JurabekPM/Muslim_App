package uz.turgunboyevjurabek.muslimapp.core.utils

import kotlin.math.abs
import kotlin.math.exp

internal fun signedAngle(angle: Float): Float = ((angle + 180f) % 360f + 360f) % 360f - 180f

internal class HeadingFilter {
    var heading: Float? = null
        private set
    private var lastTimestamp = 0L

    fun update(rawHeading: Float, timestamp: Long): Float {
        val previous = heading
        if (previous == null || timestamp - lastTimestamp > 500_000_000L) {
            heading = rawHeading
            lastTimestamp = timestamp
            return rawHeading
        }
        if (timestamp <= lastTimestamp) return previous

        val elapsed = (timestamp - lastTimestamp) / 1_000_000_000.0
        lastTimestamp = timestamp
        val delta = signedAngle(rawHeading - previous)
        val timeConstant = if (abs(delta) > 10f) 0.055 else 0.12
        val smoothed = previous + delta * (1.0 - exp(-elapsed / timeConstant)).toFloat()
        heading = smoothed
        return smoothed
    }

    fun reset() {
        heading = null
        lastTimestamp = 0L
    }
}
