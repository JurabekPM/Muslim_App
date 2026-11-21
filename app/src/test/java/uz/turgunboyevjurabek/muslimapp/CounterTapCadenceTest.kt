package uz.turgunboyevjurabek.muslimapp

import org.junit.Assert.assertEquals
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.CounterTapCadence

class CounterTapCadenceTest {
    @Test
    fun durationFollowsRecentTapIntervals() {
        val cadence = CounterTapCadence()

        assertEquals(460, cadence.recordTap(1_000))
        assertEquals(80, cadence.recordTap(1_100))
        assertEquals(80, cadence.recordTap(1_140))
        assertEquals(240, cadence.recordTap(1_440))
        assertEquals(460, cadence.recordTap(2_200))
    }

    @Test
    fun startsSlowAgainAfterReset() {
        val cadence = CounterTapCadence()
        cadence.recordTap(1_000)
        assertEquals(80, cadence.recordTap(1_100))

        cadence.reset()

        assertEquals(460, cadence.recordTap(1_150))
    }
}
