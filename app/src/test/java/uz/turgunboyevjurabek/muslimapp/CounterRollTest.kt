package uz.turgunboyevjurabek.muslimapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.CounterRoll
import kotlin.math.floor

class CounterRollTest {
    @Test
    fun carryRollsUpAndBorrowRollsDown() {
        val carry = CounterRoll.settled(99).retarget(100, 1f)
        val borrow = CounterRoll.settled(100).retarget(99, 1f)

        assertTrue(carry.wheels.all { it.end > it.start })
        assertTrue(borrow.wheels.all { it.end < it.start })
        assertEquals(listOf(0, 0, 1), carry.wheels.map { it.end.toInt().mod(10) })
        assertEquals(listOf(9, 9, 0), borrow.wheels.map { it.end.toInt().mod(10) })
    }

    @Test
    fun resetRollsEveryPlaceBackToZeroWithBoundedMotion() {
        val reset = CounterRoll.settled(Int.MAX_VALUE).retarget(0, 1f)

        assertEquals(10, reset.wheels.size)
        reset.wheels.forEach { wheel ->
            val midpoint = wheel.positionAt(0.5f)
            assertTrue(midpoint < wheel.start && midpoint > wheel.end)
            assertTrue(wheel.start - wheel.end <= 10f)
            assertEquals(0, wheel.end.toInt().mod(10))
        }
    }

    @Test
    fun newCountResumesFromTheDigitsAlreadyOnScreen() {
        val reset = CounterRoll.settled(124).retarget(0, 1f)
        val resumed = reset.retarget(1, 0.3f)

        reset.wheels.zip(resumed.wheels).forEach { (before, after) ->
            val position = before.positionAt(0.3f)
            assertEquals(position - floor(position / 10f) * 10f, after.start - floor(after.start / 10f) * 10f, 0.001f)
        }
        assertEquals(listOf(1, 0, 0), resumed.wheels.map { it.end.toInt().mod(10) })
    }
}
