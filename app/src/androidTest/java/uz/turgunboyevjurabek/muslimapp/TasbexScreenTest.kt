package uz.turgunboyevjurabek.muslimapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
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
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.DataStoreTasbihRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.DhikrSession
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedCounter
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.TasbexHistorySheet
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
        val viewModel = CounterViewModel(DataStoreTasbihRepository(preferences))
        val darkTheme = mutableStateOf(true)
        viewModels.put("tasbih", viewModel)
        fun text(id: Int) = context.getString(id)
        fun waitForCount(expected: Int) {
            compose.waitUntil(5_000) { viewModel.state.value?.count == expected }
            compose.onNodeWithText(expected.toString()).assertIsDisplayed()
        }

        try {
            runBlocking {
                assertEquals(0, withTimeout(5_000) { viewModel.state.filterNotNull().first().count })
                preferences.edit { it[intPreferencesKey("counter")] = 2 }
            }
            compose.setContent {
                MuslimAppTheme(darkTheme = darkTheme.value) {
                    Surface {
                        Box(Modifier.fillMaxSize()) { TasbexScreen(viewModel) }
                    }
                }
            }
            waitForCount(2)
            compose.onNodeWithText(text(R.string.continue_dhikr)).assertIsDisplayed()
            val counter = compose.onNodeWithContentDescription(text(R.string.dhikr))
            assertDirectionalShadowAndPressGlow(counter)
            waitForCount(3)
            compose.runOnIdle { darkTheme.value = false }
            assertDirectionalShadowAndPressGlow(counter)
            waitForCount(4)
            runBlocking { preferences.edit { it[intPreferencesKey("counter")] = 2 } }
            waitForCount(2)
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
            compose.onNodeWithText(text(R.string.tasbih_current_count)).assertIsDisplayed()
            compose.onNode(hasText("4") and hasAnyAncestor(isDialog())).assertIsDisplayed()
            compose.onNodeWithText(text(R.string.cancel)).performClick()
            waitForCount(4)
            assertEquals(emptyList<Any>(), viewModel.state.value?.history)

            compose.onNodeWithText(reset).performClick()
            compose.onNode(hasText(reset) and hasAnyAncestor(isDialog())).performClick()
            waitForCount(0)
            compose.onNodeWithText(text(R.string.new_dhikr)).assertIsDisplayed()
            assertEquals(0, runBlocking { preferences.data.first()[intPreferencesKey("counter")] })
            compose.onNodeWithText("−1").assertIsNotEnabled()
            assertEquals(4, viewModel.state.value?.history?.single()?.count)

            compose.onNodeWithText(text(R.string.tasbih_history_short)).performClick()
            compose.onNodeWithText(text(R.string.dhikr_history)).assertIsDisplayed()
            compose.onNodeWithText("4").assertIsDisplayed()
            compose.onNodeWithText(text(R.string.close)).performClick()

            counter.performTouchInput { longClick() }
            waitForCount(1)
            assertEquals(1, runBlocking { preferences.data.first()[intPreferencesKey("counter")] })
            assertEquals(4, viewModel.state.value?.history?.single()?.count)

            counter.performTouchInput { click() }
            waitForCount(2)
            counter.performTouchInput {
                repeat(6) {
                    click()
                    advanceEventTime(45)
                }
            }
            waitForCount(8)
            compose.onNodeWithText("−1").performTouchInput {
                repeat(6) {
                    click()
                    advanceEventTime(45)
                }
            }
            waitForCount(2)
        } finally {
            compose.runOnIdle { viewModels.clear() }
            scope.cancel()
            file.delete()
        }
    }

    @Test
    fun historySheetKeepsCloseVisibleWhenScrollingManyEntries() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val history = mutableStateOf(emptyList<DhikrSession>())
        val showing = mutableStateOf(true)
        val darkTheme = mutableStateOf(true)
        compose.setContent {
            MuslimAppTheme(darkTheme = darkTheme.value) {
                if (showing.value) TasbexHistorySheet(history.value) { showing.value = false }
            }
        }
        val close = compose.onNodeWithText(context.getString(R.string.close))
        compose.onNodeWithText(context.getString(R.string.no_dhikr_history)).assertIsDisplayed()
        close.assertIsDisplayed()

        compose.runOnIdle {
            history.value = List(100) { index ->
                DhikrSession(count = 101 + index, finishedAtMillis = 1_790_000_000_000L - index * 3_600_000L)
            }
        }
        compose.onNodeWithText("101").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(99)
        compose.onNodeWithText("200").assertIsDisplayed()
        close.assertIsDisplayed()

        compose.runOnIdle { darkTheme.value = false }
        close.assertIsDisplayed().performClick()
        close.assertDoesNotExist()
    }

    @Test
    fun resetSlidesLeadingDigitsDownAndCentersTheLastZero() {
        val count = mutableIntStateOf(1234)
        compose.setContent {
            MuslimAppTheme(darkTheme = true) {
                Surface {
                    AnimatedCounter(count.intValue, resetId = if (count.intValue == 0) 1L else null)
                }
            }
        }
        val before = compose.onNodeWithText("1234").captureToImage()
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { count.intValue = 0 }
        compose.mainClock.advanceTimeBy(300)
        val early = compose.onNodeWithText("0").captureToImage()
        compose.mainClock.advanceTimeBy(460)
        val rolling = compose.onNodeWithText("0").captureToImage()
        compose.mainClock.advanceTimeBy(440)
        val leaving = compose.onNodeWithText("0").captureToImage()
        compose.mainClock.advanceTimeBy(192)
        val zerosLanded = compose.onNodeWithText("0").captureToImage()
        compose.mainClock.advanceTimeBy(268)
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
        assertEquals("Completed leading zeros leave immediately", 0.0, zerosLanded.glyphInk(leftColumns).weight, 0.01)
        assertTrue("Leading digits fade", slidingLeading.weight < beforeExit.weight)
        assertEquals(0.0, settled.glyphInk(leftColumns).weight, 0.01)
        val zero = settled.glyphInk(0 until settled.width)
        assertTrue("The final zero is centered", abs(zero.centerX - settled.width / 2.0) < settled.width * 0.01)
    }

    @Test
    fun risingAndFallingDigitsFadeThroughClearEdgesInBothThemes() {
        val count = mutableIntStateOf(4)
        val darkTheme = mutableStateOf(true)
        compose.setContent {
            MuslimAppTheme(darkTheme = darkTheme.value) {
                Surface { AnimatedCounter(count.intValue, rollDurationMillis = 140) }
            }
        }

        listOf(true, false).forEach { dark ->
            compose.mainClock.autoAdvance = true
            compose.runOnIdle { darkTheme.value = dark }
            val still = compose.onNodeWithText("4").captureToImage()
            compose.mainClock.autoAdvance = false

            compose.runOnIdle { count.intValue = 5 }
            compose.mainClock.advanceTimeBy(112)
            val rising = compose.onNodeWithText("5").captureToImage()
            compose.mainClock.advanceTimeBy(160)

            compose.runOnIdle { count.intValue = 4 }
            compose.mainClock.advanceTimeBy(112)
            val falling = compose.onNodeWithText("4").captureToImage()
            compose.mainClock.advanceTimeBy(160)

            assertNotEquals(still.glyphFrameHash(), rising.glyphFrameHash())
            assertNotEquals(still.glyphFrameHash(), falling.glyphFrameHash())
            assertTrue(rising.hasClearEdges())
            assertTrue(falling.hasClearEdges())
        }
    }

    private fun assertDirectionalShadowAndPressGlow(counter: SemanticsNodeInteraction) {
        val idle = counter.captureToImage()
        val idlePixels = idle.toPixelMap()
        val x = idle.width / 2
        val inset = idle.height / 30
        assertTrue(
            "The shadow is darker below the shape than above it",
            idlePixels[x, idle.height - 1 - inset].red < idlePixels[x, inset].red,
        )

        counter.performTouchInput { down(center) }
        val pressed = counter.captureToImage()
        val lowerProbe = idle.height - 1 - inset
        assertTrue(
            "Press pulls the lower shadow closer to the button",
            pressed.toPixelMap()[x, lowerProbe].red > idlePixels[x, lowerProbe].red,
        )
        counter.performTouchInput { up() }
        val released = counter.captureToImage()

        val idleWarmth = idle.upperRimWarmth()
        val pressedWarmth = pressed.upperRimWarmth()
        assertTrue("Press adds a warm rim ($idleWarmth → $pressedWarmth)", pressedWarmth > idleWarmth + 0.04f)
        assertEquals("Releasing leaves the neutral shadow", idle.glyphFrameHash(), released.glyphFrameHash())
    }

    private fun ImageBitmap.upperRimWarmth(): Float {
        val pixels = toPixelMap()
        var warmth = 0f
        for (y in 0 until height / 3 step 2) {
            for (x in 0 until width step 2) {
                val color = pixels[x, y]
                warmth = maxOf(warmth, color.red - color.blue)
            }
        }
        return warmth
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
