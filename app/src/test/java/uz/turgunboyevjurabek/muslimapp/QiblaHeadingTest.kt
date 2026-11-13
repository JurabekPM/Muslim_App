package uz.turgunboyevjurabek.muslimapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import uz.turgunboyevjurabek.muslimapp.core.utils.isMagneticFieldUsable
import uz.turgunboyevjurabek.muslimapp.core.utils.isQiblaAligned
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.core.utils.QiblaAlignment
import uz.turgunboyevjurabek.muslimapp.core.utils.HeadingFilter
import uz.turgunboyevjurabek.muslimapp.core.utils.CompassHeading
import uz.turgunboyevjurabek.muslimapp.core.utils.calculateQiblaDirection
import uz.turgunboyevjurabek.muslimapp.core.utils.signedAngle
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.qiblaStatus
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla.QiblaViewModel
import uz.turgunboyevjurabek.muslimapp.feature.data.location.CompassReading
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class QiblaHeadingTest {
    @Test
    fun compassKeepsTheSameBearingWhenThePhoneIsRaisedUpright() {
        val compass = CompassHeading()
        val heading = 243f
        for (pitch in 0..105 step 5) {
            val azimuth = compass.azimuth(displayMatrix(heading, pitch))!!
            assertEquals(0f, signedAngle(azimuth - heading), 0.01f)
        }
        for (pitch in 105 downTo 0 step 5) {
            val azimuth = compass.azimuth(displayMatrix(heading, pitch))!!
            assertEquals(0f, signedAngle(azimuth - heading), 0.01f)
        }
    }

    @Test
    fun uprightPhoneConfirmsOnlyWhenItsBackFacesQibla() {
        val compass = CompassHeading()
        val viewModel = QiblaViewModel()
        viewModel.updateQiblaDirection(243f)

        fun holdAt(heading: Float, startTime: Long) {
            val azimuth = compass.azimuth(displayMatrix(heading, 90))!!
            for (offset in 0L..400_000_000L step 100_000_000L) {
                viewModel.updateCurrentDirection(
                    CompassReading(azimuth, azimuth, reliable = true, canConfirm = true, startTime + offset)
                )
            }
        }

        holdAt(63f, 1_000_000_000L)
        assertFalse(viewModel.qiblaState.value.aligned)
        holdAt(243f, 2_000_000_000L)
        assertEquals("Qibla topildi", qiblaStatus(viewModel.qiblaState.value, compassAvailable = true).title)
    }

    private fun displayMatrix(heading: Float, pitch: Int): FloatArray {
        val yaw = Math.toRadians(heading.toDouble())
        val tilt = Math.toRadians(pitch.toDouble())
        val east = sin(yaw).toFloat()
        val north = cos(yaw).toFloat()
        val horizontal = cos(tilt).toFloat()
        val vertical = sin(tilt).toFloat()
        return floatArrayOf(
            north, east * horizontal, -east * vertical,
            -east, north * horizontal, -north * vertical,
            0f, vertical, horizontal,
        )
    }

    @Test
    fun qiblaStatusRespondsToAlignmentAtBothSensorAccuracyLevels() {
        val viewModel = QiblaViewModel()
        viewModel.updateQiblaDirection(243f)
        fun report(heading: Float, timestamp: Long, reliable: Boolean) {
            viewModel.updateCurrentDirection(
                CompassReading(heading, heading, reliable, reliable, timestamp)
            )
        }
        fun title() = qiblaStatus(viewModel.qiblaState.value, compassAvailable = true).title

        report(220f, 900_000_000L, reliable = false)
        assertEquals("Kompas aniqligi past", title())
        report(243f, 1_000_000_000L, reliable = false)
        assertEquals("Qiblaga yaqin", title())
        for (time in 1_100_000_000L..1_400_000_000L step 100_000_000L) {
            report(243f, time, reliable = false)
        }
        assertEquals("Qiblaga taxminan mos", title())
        assertFalse(viewModel.qiblaState.value.aligned)

        for (time in 1_500_000_000L..1_900_000_000L step 100_000_000L) {
            report(243f, time, reliable = true)
        }
        assertEquals("Qibla topildi", title())
        viewModel.updateCurrentDirection(CompassReading(243f))
        assertEquals("Kompas aniqligi past", title())
        assertEquals(0f, viewModel.qiblaState.value.turn!!, 0f)
    }

    @Test
    fun bearingUsesGeographicNorth() {
        assertEquals(240.3, calculateQiblaDirection(41.2995, 69.2401), 0.3)
        assertEquals(244.6, calculateQiblaDirection(40.3864, 71.7864), 0.3)
    }

    @Test
    fun filterTakesShortestPathAcrossNorth() {
        val clockwise = HeadingFilter()
        clockwise.update(359f, 1_000_000_000L)
        assertTrue(clockwise.update(1f, 1_020_000_000L) in 359f..361f)

        val counterclockwise = HeadingFilter()
        counterclockwise.update(1f, 1_000_000_000L)
        assertTrue(counterclockwise.update(359f, 1_020_000_000L) in -1f..1f)
        assertEquals(-2f, signedAngle(358f), 0.001f)
        assertEquals(2f, signedAngle(-358f), 0.001f)
    }

    @Test
    fun filterReducesJitterAndCatchesUpWithRealTurns() {
        val filter = HeadingFilter()
        filter.update(90f, 1_000_000_000L)
        assertTrue(filter.update(92f, 1_020_000_000L) in 90f..91f)
        val firstTurn = filter.update(180f, 1_040_000_000L)
        assertTrue(firstTurn in 100f..150f)
        repeat(25) { step -> filter.update(180f, 1_060_000_000L + step * 20_000_000L) }
        assertTrue(abs(filter.heading!! - 180f) < 1f)
    }
    @Test
    fun disturbedMagneticFieldCannotGuideTheCompass() {
        assertTrue(isMagneticFieldUsable(52f, 50f, -0.8f, -0.85f))
        assertFalse(isMagneticFieldUsable(72f, 50f, -0.8f, -0.85f))
        assertFalse(isMagneticFieldUsable(50f, 50f, 0.15f, -0.85f))
    }

    @Test
    fun alignmentRequiresReliableRawAndFilteredHeadings() {
        assertTrue(isQiblaAligned(1.5f, 2f, canConfirm = true, alreadyAligned = false))
        assertFalse(isQiblaAligned(1.5f, 12f, canConfirm = true, alreadyAligned = true))
        assertFalse(isQiblaAligned(1.5f, 2f, canConfirm = false, alreadyAligned = true))
        assertFalse(isQiblaAligned(0f, null, canConfirm = true, alreadyAligned = true))
    }

    @Test
    fun alignmentNeedsUninterruptedFreshSamples() {
        val alignment = QiblaAlignment()
        assertFalse(alignment.update(0f, 0f, true, 1_000_000_000L))
        assertFalse(alignment.update(0f, 0f, true, 1_400_000_000L))
        assertFalse(alignment.update(0f, 0f, true, 1_550_000_000L))
        assertFalse(alignment.update(0f, 0f, true, 1_700_000_000L))
        assertTrue(alignment.update(0f, 0f, true, 1_760_000_000L))
        assertFalse(alignment.update(0f, 18f, true, 1_780_000_000L))
    }
}
