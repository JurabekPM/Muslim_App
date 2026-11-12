package uz.turgunboyevjurabek.muslimapp.core.utils

import kotlin.math.abs
import kotlin.math.atan2

internal class CompassHeading {
    private var usesBackDirection = false

    fun azimuth(displayMatrix: FloatArray): Float? {
        val screenNormalUp = abs(displayMatrix[8])
        // Both axes have a usable horizontal projection in this overlap.
        // Keep the current axis there so small tilt changes cannot switch it back and forth.
        usesBackDirection = if (usesBackDirection) screenNormalUp < 0.75f else screenNormalUp <= 0.60f
        // Android's +Z points out of the screen. When upright, the user faces along -Z.
        val east = if (usesBackDirection) -displayMatrix[2] else displayMatrix[1]
        val north = if (usesBackDirection) -displayMatrix[5] else displayMatrix[4]
        if (east * east + north * north < 0.25f) return null
        return Math.toDegrees(atan2(east, north).toDouble()).toFloat().takeIf { it.isFinite() }
    }

    fun reset() {
        usesBackDirection = false
    }
}
