package uz.turgunboyevjurabek.muslimapp

import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.QiblaScreen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla.QiblaState
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme
import uz.turgunboyevjurabek.muslimapp.feature.data.location.CompassReading
import kotlin.math.roundToInt

class QiblaAnimationTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun confirmationClosesTurnArcAndKeepsGlowUntilLeavingQibla() {
        val state = MutableStateFlow(
            QiblaState(qiblaDirection = 243f, compass = CompassReading(183f, reliable = true)),
        )
        compose.setContent {
            MuslimAppTheme(darkTheme = true) {
                Surface {
                    QiblaScreen(qiblaState = state, compassAvailable = true)
                }
            }
        }
        compose.onNodeWithText("O‘ngga buriling").assertIsDisplayed()
        compose.onNodeWithContentDescription("Qibla yo‘nalishi 243°").assertIsDisplayed()
        val dial = compose.onNodeWithContentDescription("Qibla kompasi")
        val turning = dial.captureToImage()

        compose.mainClock.autoAdvance = false
        compose.runOnIdle {
            state.value = state.value.copy(compass = CompassReading(241f, reliable = true), aligned = true)
        }
        compose.waitForIdle()
        val flashFrames = List(5) {
            compose.mainClock.advanceTimeBy(80)
            dial.captureToImage()
        }
        compose.mainClock.advanceTimeBy(1_100)
        val settled = dial.captureToImage()
        compose.onNodeWithText("Qibla topildi").assertIsDisplayed()
        compose.mainClock.advanceTimeBy(1_200)
        val later = dial.captureToImage()

        compose.runOnIdle {
            state.value = state.value.copy(compass = CompassReading(183f, reliable = true), aligned = false)
        }
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(700)
        val turnedAway = dial.captureToImage()
        compose.onNodeWithText("O‘ngga buriling").assertIsDisplayed()

        assertTrue("The turning arc closes", turning.greenAt(0.779f, 0.221f) > settled.greenAt(0.779f, 0.221f) + 0.20f)
        val ringPeak = flashFrames.maxOf { it.greenAt(0.932f, 0.50f) }
        val badgePeak = flashFrames.maxOf { it.greenAt(0.55f, 0.147f) }
        val ringRest = settled.greenAt(0.932f, 0.50f)
        val badgeRest = settled.greenAt(0.55f, 0.147f)
        assertTrue("The outer ring brightens ($ringPeak vs $ringRest)", ringPeak > ringRest + 0.03f)
        assertTrue("The Kaaba halo brightens ($badgePeak vs $badgeRest)", badgePeak > badgeRest + 0.03f)
        assertTrue("The ring stays lit", later.greenAt(0.932f, 0.50f) > turnedAway.greenAt(0.932f, 0.50f) + 0.06f)
        assertTrue("The Kaaba halo stays lit", later.greenAt(0.55f, 0.147f) > turnedAway.greenAt(0.55f, 0.147f) + 0.04f)
        assertTrue("The outer flash does not repeat", later.greenAt(0.932f, 0.50f) <= ringRest + 0.01f)
    }

    private fun ImageBitmap.greenAt(x: Float, y: Float): Float =
        toPixelMap()[(width * x).roundToInt(), (height * y).roundToInt()].green
}
