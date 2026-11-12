package uz.turgunboyevjurabek.muslimapp.core.utils

import kotlin.math.abs
import kotlin.math.max

internal fun isMagneticFieldUsable(
    measuredStrength: Float,
    expectedStrength: Float?,
    verticalFraction: Float,
    expectedVerticalFraction: Float?,
): Boolean {
    if (!measuredStrength.isFinite() || measuredStrength !in 15f..85f) return false
    if (expectedStrength != null && abs(measuredStrength - expectedStrength) > max(10f, expectedStrength * 0.30f)) {
        return false
    }
    return expectedVerticalFraction == null ||
        abs(verticalFraction - expectedVerticalFraction) <= 0.25f
}

internal fun isQiblaAligned(
    filteredTurn: Float?,
    rawTurn: Float?,
    canConfirm: Boolean,
    alreadyAligned: Boolean,
): Boolean {
    if (!canConfirm || filteredTurn == null || rawTurn == null) return false
    val tolerance = if (alreadyAligned) 4.5f else 3f
    return abs(filteredTurn) <= tolerance && abs(rawTurn) <= tolerance &&
        abs(signedAngle(filteredTurn - rawTurn)) <= 2.5f
}

internal class QiblaAlignment {
    private var startedAt = 0L
    private var lastSampleAt = 0L
    private var aligned = false

    fun update(filteredTurn: Float?, rawTurn: Float?, canConfirm: Boolean, timestamp: Long): Boolean {
        if (timestamp <= 0L || !isQiblaAligned(filteredTurn, rawTurn, canConfirm, aligned)) {
            reset()
            return false
        }
        if (timestamp <= lastSampleAt) return aligned
        if (startedAt == 0L || timestamp - lastSampleAt > 200_000_000L) {
            startedAt = timestamp
            aligned = false
        }
        lastSampleAt = timestamp
        aligned = timestamp - startedAt >= 350_000_000L
        return aligned
    }

    fun reset() {
        startedAt = 0L
        lastSampleAt = 0L
        aligned = false
    }
}
