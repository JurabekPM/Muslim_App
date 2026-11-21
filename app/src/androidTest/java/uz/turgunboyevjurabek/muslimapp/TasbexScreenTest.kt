package uz.turgunboyevjurabek.muslimapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModelStore
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.CounterViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedCounter
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.TasbexScreen
import java.io.File
import java.util.UUID
import kotlin.math.abs

class TasbexScreenTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun tapsCountOnceAndResetArchivesAfterConfirmation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val file = File(context.cacheDir, "${UUID.randomUUID()}.preferences_pb")
        val preferences = PreferenceDataStoreFactory.create(scope = scope) { file }
        val viewModels = ViewModelStore()
        val viewModel = CounterViewModel(preferences, testCount = 1234)
        viewModels.put("tasbih", viewModel)
        fun text(id: Int) = context.getString(id)
        fun waitForCount(expected: Int) {
            compose.waitUntil(5_000) { viewModel.state.value?.count == expected }
            compose.onNodeWithText(expected.toString()).assertIsDisplayed()
        }

        try {
            runBlocking {
                assertEquals(1234, withTimeout(5_000) { viewModel.state.filterNotNull().first().count })
                preferences.edit { it[intPreferencesKey("counter")] = 2 }
            }
            compose.setContent {
                MuslimAppTheme(darkTheme = true) {
                    Surface {
                        Box(Modifier.fillMaxSize()) { TasbexScreen(viewModel) }
                    }
                }
            }
            waitForCount(2)
            val counter = compose.onNodeWithContentDescription(text(R.string.dhikr))
            counter.performTouchInput { click() }
            waitForCount(3)
            counter.performTouchInput { click() }
            waitForCount(4)
            counter.performTouchInput { click(Offset(4f, center.y)) }
            waitForCount(5)
            compose.onNodeWithText("−1").performClick()
            waitForCount(4)

            val reset = text(R.string.tasbih_reset_short)
            compose.onNodeWithText(reset).performClick()
            compose.onNodeWithText(text(R.string.cancel)).performClick()
            waitForCount(4)
            assertEquals(emptyList<Any>(), viewModel.state.value?.history)

            compose.onNodeWithText(reset).performClick()
            compose.onNode(hasText(reset) and hasAnyAncestor(isDialog())).performClick()
            waitForCount(0)
            assertEquals(0, runBlocking { preferences.data.first()[intPreferencesKey("counter")] })
            compose.onNodeWithText("−1").assertIsNotEnabled()
            assertEquals(4, viewModel.state.value?.history?.single()?.count)

            compose.onNodeWithText(text(R.string.tasbih_history_short)).performClick()
            compose.onNodeWithText(text(R.string.dhikr_history)).assertIsDisplayed()
            compose.onNodeWithText("4").assertIsDisplayed()
            compose.onNodeWithText(text(R.string.close)).performClick()

            counter.performTouchInput { longClick() }
            waitForCount(1234)
            assertEquals(1234, runBlocking { preferences.data.first()[intPreferencesKey("counter")] })
            assertEquals(4, viewModel.state.value?.history?.single()?.count)

            counter.performTouchInput { click() }
            waitForCount(1235)
        } finally {
            compose.runOnIdle { viewModels.clear() }
            scope.cancel()
            file.delete()
        }
    }

    @Test
    fun resetSlidesLeadingDigitsDownAndCentersTheLastZero() {
        val count = mutableIntStateOf(124)
        compose.setContent {
            MuslimAppTheme(darkTheme = true) {
                Surface {
                    AnimatedCounter(count.intValue, resetId = if (count.intValue == 0) 1L else null)
                }
            }
        }
        val before = compose.onNodeWithText("124").captureToImage()
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { count.intValue = 0 }
        compose.mainClock.advanceTimeBy(300)
        val early = compose.onNodeWithText("0").captureToImage()
        compose.mainClock.advanceTimeBy(460)
        val rolling = compose.onNodeWithText("0").captureToImage()
        compose.mainClock.advanceTimeBy(440)
        val leaving = compose.onNodeWithText("0").captureToImage()
        compose.mainClock.advanceTimeBy(460)
        val settled = compose.onNodeWithText("0").captureToImage()

        assertEquals(before.width, rolling.width)
        assertEquals(rolling.width, settled.width)
        assertNotEquals(before.glyphFrameHash(), early.glyphFrameHash())
        assertNotEquals(early.glyphFrameHash(), rolling.glyphFrameHash())
        assertNotEquals(rolling.glyphFrameHash(), leaving.glyphFrameHash())
        assertNotEquals(leaving.glyphFrameHash(), settled.glyphFrameHash())
        assertTrue(rolling.hasClearEdges())
        assertTrue(leaving.hasClearEdges())

        val leftColumns = 0 until (before.width / 2 - before.height / 4)
        val beforeExit = rolling.glyphInk(leftColumns)
        val slidingLeading = leaving.glyphInk(leftColumns)
        assertTrue("Leading digits are still present halfway through reset", beforeExit.weight > 0)
        assertTrue("Leading digits remain briefly visible", slidingLeading.weight > 0)
        assertTrue("Leading digits slide down", slidingLeading.centerY > beforeExit.centerY + 1)
        assertTrue("Leading digits fade", slidingLeading.weight < beforeExit.weight)
        assertEquals(0.0, settled.glyphInk(leftColumns).weight, 0.01)
        val zero = settled.glyphInk(0 until settled.width)
        assertTrue("The final zero is centered", abs(zero.centerX - settled.width / 2.0) < settled.width * 0.01)
    }

    private fun ImageBitmap.glyphInk(columns: IntRange): GlyphInk {
        val pixels = toPixelMap()
        val background = pixels[0, 0].red
        var weight = 0.0
        var weightedX = 0.0
        var weightedY = 0.0
        for (y in 0 until height) {
            for (x in columns) {
                val brightness = (pixels[x, y].red - background).coerceAtLeast(0f).toDouble()
                weight += brightness
                weightedX += x * brightness
                weightedY += y * brightness
            }
        }
        return GlyphInk(weight, weightedX / weight, weightedY / weight)
    }

    private data class GlyphInk(val weight: Double, val centerX: Double, val centerY: Double)

    private fun ImageBitmap.hasClearEdges(): Boolean {
        val pixels = toPixelMap()
        val background = pixels[0, 0]
        fun isClear(x: Int, y: Int): Boolean {
            val pixel = pixels[x, y]
            return abs(pixel.red - background.red) < 0.02f &&
                abs(pixel.green - background.green) < 0.02f &&
                abs(pixel.blue - background.blue) < 0.02f
        }
        return (0 until width).all { x -> isClear(x, 0) && isClear(x, height - 1) } &&
            (0 until height).all { y -> isClear(0, y) && isClear(width - 1, y) }
    }

    private fun ImageBitmap.glyphFrameHash(): Int {
        val pixels = toPixelMap()
        var hash = 1
        for (y in 0 until height step 3) {
            for (x in 0 until width step 3) {
                hash = 31 * hash + pixels[x, y].hashCode()
            }
        }
        return hash
    }
}
