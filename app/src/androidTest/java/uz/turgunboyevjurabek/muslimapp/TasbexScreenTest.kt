package uz.turgunboyevjurabek.muslimapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
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
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.TasbihFeedbackOption
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedCounter
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.TasbexHistorySheet
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.TasbexScreen
import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
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
        fun assertCaptionAboveActions(id: Int, count: Int) {
            val caption = compose.onNodeWithText(text(id)).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val number = compose.onNodeWithText(count.toString()).fetchSemanticsNode().boundsInRoot
            val history = compose.onNodeWithText(text(R.string.tasbih_history_short)).fetchSemanticsNode().boundsInRoot
            assertTrue("The caption stays below the counter and clear of the action row",
                caption.top >= number.bottom && caption.bottom < history.top)
        }

        try {
            runBlocking {
                assertEquals(0, withTimeout(5_000) { viewModel.state.filterNotNull().first().count })
                preferences.edit { it[intPreferencesKey("counter")] = 2 }
            }
            compose.setContent {
                MuslimAppTheme(darkTheme = darkTheme.value) {
                    Surface {
                        Box(Modifier.fillMaxSize().padding(vertical = 48.dp)) { TasbexScreen(viewModel) }
                    }
                }
            }
            waitForCount(2)
            assertCaptionAboveActions(R.string.continue_dhikr, 2)
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
            assertCaptionAboveActions(R.string.new_dhikr, 0)
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
    fun selectsGoalsAndDrawsSunsetOnThePressingShapeInBothThemes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val file = File(context.cacheDir, "${UUID.randomUUID()}.preferences_pb")
        val store = PreferenceDataStoreFactory.create(scope = scope) { file }
        val viewModels = ViewModelStore()
        val viewModel = CounterViewModel(DataStoreTasbihRepository(store))
        viewModels.put("tasbih", viewModel)
        val darkTheme = mutableStateOf(true)

        fun text(id: Int) = context.getString(id)
        fun times(count: Int) = context.resources.getQuantityString(R.plurals.tasbih_count_times, count, count)
        fun waitForTarget(target: Int?, count: Int) {
            compose.waitUntil(5_000) { viewModel.state.value?.let { it.target == target && it.count == count } == true }
        }
        val counter = compose.onNodeWithContentDescription(text(R.string.dhikr))

        try {
            runBlocking { store.edit { it[intPreferencesKey("counter")] = 18 } }
            compose.setContent {
                MuslimAppTheme(darkTheme = darkTheme.value) {
                    Surface {
                        Box(Modifier.fillMaxSize().padding(vertical = 48.dp)) { TasbexScreen(viewModel) }
                    }
                }
            }
            waitForTarget(null, 18)
            val hint = compose.onNodeWithText(text(R.string.continue_dhikr))
            hint.assertIsDisplayed()
            compose.mainClock.autoAdvance = false
            compose.mainClock.advanceTimeBy(5_500)
            hint.assertDoesNotExist()
            compose.mainClock.autoAdvance = true

            val free = compose.onNodeWithText(text(R.string.tasbih_free_count))
            free.performClick()
            compose.onNodeWithText(times(33)).performClick()
            waitForTarget(33, 18)
            assertSunsetPress(counter, 18f / 33)
            waitForTarget(33, 19)

            compose.runOnIdle { darkTheme.value = false }
            compose.onNodeWithText(times(33)).performClick()
            compose.onNodeWithText(times(99)).performClick()
            waitForTarget(99, 19)
            compose.onNodeWithText(times(99)).performClick()
            compose.onNodeWithText(text(R.string.tasbih_custom_count)).performClick()
            val value = compose.onNodeWithText(text(R.string.tasbih_enter_count))
            val save = compose.onNodeWithText(text(R.string.tasbih_save_count))
            value.performTextReplacement("0")
            save.assertIsNotEnabled()
            value.performTextReplacement("54")
            save.performClick()

            waitForTarget(54, 19)
            assertSunsetPress(counter, 19f / 54)
            waitForTarget(54, 20)
            compose.onNodeWithText(times(54)).performClick()
            free.performClick()
            waitForTarget(null, 20)
            counter.assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.ProgressBarRangeInfo))
        } finally {
            compose.mainClock.autoAdvance = true
            compose.runOnIdle { viewModels.clear() }
            scope.cancel()
            file.delete()
        }
    }

    @Test
    fun feedbackSettingsAreIndependentAndCompletionAppearsThenFades() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val file = File(context.cacheDir, "${UUID.randomUUID()}.preferences_pb")
        val store = PreferenceDataStoreFactory.create(scope = scope) { file }
        val repository = DataStoreTasbihRepository(store)
        val viewModel = CounterViewModel(repository)
        val viewModels = ViewModelStore().apply { put("tasbih", viewModel) }
        val darkTheme = mutableStateOf(false)
        fun label(id: Int) = compose.onNodeWithText(context.getString(id))

        try {
            runBlocking {
                store.edit { it[intPreferencesKey("counter")] = 32 }
                repository.setTarget(33)
            }
            compose.setContent {
                MuslimAppTheme(darkTheme = darkTheme.value) {
                    Surface {
                        Box(Modifier.fillMaxSize().padding(vertical = 48.dp)) { TasbexScreen(viewModel) }
                    }
                }
            }
            compose.waitUntil(5_000) { viewModel.state.value?.count == 32 }
            val settings = compose.onNodeWithContentDescription(context.getString(R.string.tasbih_feedback_settings))
            settings.performClick()
            val tapSound = label(R.string.tasbih_tap_sound).assertIsOff()
            val tapVibration = label(R.string.tasbih_tap_vibration).assertIsOn()
            val completionSound = label(R.string.tasbih_completion_sound).assertIsOff()
            val completionVibration = label(R.string.tasbih_completion_vibration).assertIsOn()
            tapSound.performClick()
            tapVibration.performClick()
            completionSound.performClick()
            completionVibration.performClick()
            val expected = setOf(TasbihFeedbackOption.TapSound, TasbihFeedbackOption.CompletionSound)
            compose.waitUntil(5_000) { viewModel.state.value?.feedback == expected }
            label(R.string.close).performClick()

            compose.runOnIdle { darkTheme.value = true }
            settings.performClick()
            tapSound.assertIsOn()
            tapVibration.assertIsOff()
            completionSound.assertIsOn()
            completionVibration.assertIsOff().performClick()
            compose.waitUntil(5_000) {
                viewModel.state.value?.feedback == expected + TasbihFeedbackOption.CompletionVibration
            }
            label(R.string.close).performClick()

            val completed = context.resources.getQuantityString(R.plurals.tasbih_goal_completed, 33, 33)
            val message = compose.onNodeWithText(completed)
            val counter = compose.onNodeWithContentDescription(context.getString(R.string.dhikr))
            compose.mainClock.autoAdvance = false
            counter.performTouchInput { click() }
            compose.waitUntil(5_000) { viewModel.state.value?.count == 33 }
            compose.mainClock.advanceTimeBy(400)
            message.assertIsDisplayed()
            compose.mainClock.advanceTimeBy(2_400)
            message.assertDoesNotExist()
            counter.performTouchInput { click() }
            compose.waitUntil(5_000) { viewModel.state.value?.count == 34 }
            compose.mainClock.advanceTimeBy(400)
            message.assertDoesNotExist()
        } finally {
            compose.mainClock.autoAdvance = true
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
                LocalDate.of(2026, 11, 21).session(count = 101 + index, minutesBeforeEvening = index.toLong())
            }
        }
        compose.onNodeWithText("101").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(101)
        compose.onNodeWithText("200").assertIsDisplayed()
        close.assertIsDisplayed()

        compose.runOnIdle { darkTheme.value = false }
        close.assertIsDisplayed().performClick()
        close.assertDoesNotExist()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun historyExpandsMonthsAndJumpsToTheChosenDayInBothThemes() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val darkTheme = mutableStateOf(true)
        val today = LocalDate.of(2026, 11, 21)
        val olderDay = LocalDate.of(2026, 10, 7)
        val history = listOf(
            olderDay.session(734),
            today.session(731),
            olderDay.plusDays(1).session(733),
        )
        compose.setContent {
            MuslimAppTheme(darkTheme = darkTheme.value) {
                TasbexHistorySheet(history) {}
            }
        }
        val monthNames = context.resources.getStringArray(R.array.calendar_month_names)
        val locale = context.resources.configuration.locales[0]
        fun monthName(month: Int) = monthNames[month - 1].replaceFirstChar { it.titlecase(locale) } + " 2026"
        val recentMonth = compose.onNodeWithText(monthName(11))
        val olderMonth = compose.onNodeWithText(monthName(10))
        val dateAction = context.getString(R.string.tasbih_history_select_date)
        val confirm = context.getString(R.string.tasbih_history_open_date)
        val dayLabel = checkNotNull(
            DatePickerDefaults.dateFormatter().formatDate(
                olderDay.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
                locale,
                forContentDescription = true,
            ),
        )

        listOf(true, false).forEach { dark ->
            compose.runOnIdle { darkTheme.value = dark }
            recentMonth.assertIsSelected()
            compose.onNodeWithText("731").assertIsDisplayed()
            olderMonth.assertIsNotSelected().performClick()
            olderMonth.assertIsSelected()
            recentMonth.assertIsNotSelected()
            compose.onNodeWithText("733").assertIsDisplayed()
            compose.onNodeWithText(dateAction).performClick()
            compose.onNode(hasText(dayLabel, substring = true) and hasClickAction()).performClick()
            compose.onNodeWithText(confirm).performClick()
            compose.onNodeWithText(confirm).assertDoesNotExist()
            compose.onNodeWithText("734").assertIsDisplayed()
            compose.onNodeWithText(context.getString(R.string.close)).assertIsDisplayed()
            recentMonth.performClick()
        }
    }

    private fun LocalDate.session(count: Int, minutesBeforeEvening: Long = 0) =
        DhikrSession(count, atTime(22, 0).minusMinutes(minutesBeforeEvening)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())

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

    private fun assertSunsetPress(counter: SemanticsNodeInteraction, fraction: Float) {
        assertTouchRimLight(counter)
        val progress = SemanticsMatcher.expectValue(
            SemanticsProperties.ProgressBarRangeInfo,
            ProgressBarRangeInfo(fraction, 0f..1f),
        )
        val idle = counter.assert(progress).captureToImage()
        compose.mainClock.autoAdvance = false
        val (flash, swept, held) = try {
            counter.performTouchInput { down(center) }
            compose.mainClock.advanceTimeBy(128)
            val flash = counter.captureToImage()
            compose.mainClock.advanceTimeBy(320)
            val swept = counter.captureToImage()
            compose.mainClock.advanceTimeBy(352)
            val held = counter.captureToImage()
            counter.performTouchInput { up() }
            Triple(flash, swept, held)
        } finally {
            compose.mainClock.autoAdvance = true
        }
        assertTrue("Idle contour has the Sunset fill", idle.hasSunsetStroke())
        assertTrue("Pressed contour has the Sunset fill", held.hasSunsetStroke())
        assertTrue("The slower bead sweep is still visible at 448 ms", swept.highlightedBeadPixels() > held.highlightedBeadPixels())
        val flashPixels = flash.pearlPixelsAtRim()
        assertTrue("The Sunset tip has a distinct bright head on press", flashPixels > idle.pearlPixelsAtRim() + 20)
        assertTrue("The Sunset tip settles after its initial flash", flashPixels > held.pearlPixelsAtRim())
        assertNotEquals(idle.glyphFrameHash(), held.glyphFrameHash())
    }

    private fun assertTouchRimLight(counter: SemanticsNodeInteraction) {
        compose.mainClock.autoAdvance = false
        val (heldLeft, heldRight, ended) = try {
            counter.performTouchInput { down(center - Offset(center.x / 2f, 0f)) }
            compose.mainClock.advanceTimeBy(800)
            val heldLeft = counter.captureToImage()
            counter.performTouchInput {
                moveTo(center + Offset(0f, center.y / 2f))
            }
            compose.mainClock.advanceTimeBy(160)
            counter.performTouchInput {
                moveTo(center + Offset(center.x / 2f, 0f))
            }
            compose.mainClock.advanceTimeBy(224)
            val heldRight = counter.captureToImage()
            counter.performTouchInput { up() }
            compose.mainClock.advanceTimeBy(384)
            Triple(heldLeft, heldRight, counter.captureToImage())
        } finally {
            compose.mainClock.autoAdvance = true
        }
        val leftTravel = heldLeft.rimWarmth(left = true) - heldRight.rimWarmth(left = true)
        val rightTravel = heldRight.rimWarmth(left = false) - heldLeft.rimWarmth(left = false)
        val rightFade = heldRight.rimWarmth(left = false) - ended.rimWarmth(left = false)
        assertTrue("The glow travels with one held pointer (left=$leftTravel, right=$rightTravel)",
            leftTravel > 10f && rightTravel > 10f)
        assertTrue("The glow fades after releasing a drag (right=$rightFade)", rightFade > 10f)
    }

    private fun ImageBitmap.rimWarmth(left: Boolean): Float {
        val pixels = toPixelMap()
        val firstColumn = if (left) 0 else width * 2 / 3
        var warmth = 0f
        for (y in height * 2 / 5 until height * 3 / 5 step 3) {
            for (x in firstColumn until firstColumn + width / 3 step 3) {
                val pixel = pixels[x, y]
                warmth += pixel.red - pixel.blue
            }
        }
        return warmth
    }

    private fun ImageBitmap.pearlPixelsAtRim(): Int {
        val pixels = toPixelMap()
        var brightPixels = 0
        for (y in 0 until height step 2) {
            for (x in 0 until width step 2) {
                if (abs(x - width / 2) < width / 6 && abs(y - height / 2) < height / 6) continue
                val pixel = pixels[x, y]
                if (pixel.red > 0.98f && pixel.green > 0.98f && pixel.blue > 0.96f) brightPixels++
            }
        }
        return brightPixels
    }

    private fun ImageBitmap.hasSunsetStroke(): Boolean {
        val pixels = toPixelMap()
        var coloredPixels = 0
        for (y in 0 until height step 3) {
            for (x in 0 until width step 3) {
                val pixel = pixels[x, y]
                if (pixel.red > 0.65f && pixel.red - pixel.green > 0.16f && pixel.green - pixel.blue > 0.14f) {
                    coloredPixels++
                }
            }
        }
        return coloredPixels > 30
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

        compose.mainClock.autoAdvance = false
        val pressed = try {
            counter.performTouchInput { down(center) }
            compose.mainClock.advanceTimeBy(352)
            counter.captureToImage()
        } finally {
            compose.mainClock.autoAdvance = true
        }
        val lowerProbe = idle.height - 1 - inset
        assertTrue(
            "Press pulls the lower shadow closer to the button",
            pressed.toPixelMap()[x, lowerProbe].red > idlePixels[x, lowerProbe].red,
        )
        counter.performTouchInput { up() }
        val released = counter.captureToImage()

        assertTrue("Press lights the beads", pressed.highlightedBeadPixels() > idle.highlightedBeadPixels())
        assertEquals("Releasing leaves the neutral shadow", idle.glyphFrameHash(), released.glyphFrameHash())
    }

    private fun ImageBitmap.highlightedBeadPixels(): Int {
        val pixels = toPixelMap()
        var highlighted = 0
        for (y in height * 5 / 12 until height * 7 / 12) {
            for (x in width * 5 / 12 until width * 7 / 12) {
                val color = pixels[x, y]
                if (color.red > 0.94f && color.green > 0.85f && color.blue > 0.72f && color.red - color.blue > 0.04f) {
                    highlighted++
                }
            }
        }
        return highlighted
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
