package uz.turgunboyevjurabek.muslimapp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.AppBarScrollState

class AppBarScrollStateTest {
    private val state = AppBarScrollState(hideDistance = 16f, showDistance = 8f)

    @Test
    fun hidesAndShowsAfterContinuedVerticalMovementWithoutConsumingScroll() {
        scroll(-12f)
        assertTrue(state.controlsVisible)
        scroll(2f)
        scroll(-4f)
        assertTrue(state.controlsVisible)
        assertEquals(Offset.Zero, scroll(-12f))
        assertFalse(state.controlsVisible)
        scroll(4f)
        assertFalse(state.controlsVisible)
        scroll(4f)
        assertTrue(state.controlsVisible)
    }

    @Test
    fun ignoresFlingHorizontalAndUnconsumedUpwardScroll() {
        state.onPostScroll(Offset(0f, -80f), Offset.Zero, NestedScrollSource.SideEffect)
        state.onPostScroll(Offset(50f, 0f), Offset.Zero, NestedScrollSource.UserInput)
        state.onPostScroll(Offset.Zero, Offset(0f, -80f), NestedScrollSource.UserInput)
        assertTrue(state.controlsVisible)
    }

    @Test
    fun showsWhenTheUserPullsDownAtTheTopOfTheContent() {
        scroll(-16f)
        assertFalse(state.controlsVisible)
        state.onPostScroll(Offset.Zero, Offset(0f, 2f), NestedScrollSource.UserInput)
        assertTrue(state.controlsVisible)
    }

    private fun scroll(delta: Float): Offset =
        state.onPostScroll(Offset(0f, delta), Offset.Zero, NestedScrollSource.UserInput)
}
