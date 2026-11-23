package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlin.math.abs

internal class AppBarScrollState(
    private val hideDistance: Float,
    private val showDistance: Float,
) : NestedScrollConnection {
    var controlsVisible by mutableStateOf(true)
        private set

    private var travel = 0f

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
        if (source != NestedScrollSource.UserInput) return Offset.Zero
        val delta = consumed.y
        if (!controlsVisible && delta >= 0f && available.y > 0f) {
            controlsVisible = true
            travel = 0f
        } else if (delta != 0f) {
            val towardNextState = if (controlsVisible) delta < 0f else delta > 0f
            travel = if (towardNextState) travel + abs(delta) else 0f
            val limit = if (controlsVisible) hideDistance else showDistance
            if (travel >= limit) {
                controlsVisible = !controlsVisible
                travel = 0f
            }
        }
        return Offset.Zero
    }
}
