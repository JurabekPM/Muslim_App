package uz.turgunboyevjurabek.muslimapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
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
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.CounterViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.TasbexScreen
import java.io.File
import java.util.UUID

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
        val viewModel = CounterViewModel(preferences)
        viewModels.put("tasbih", viewModel)
        fun text(id: Int) = context.getString(id)
        fun waitForCount(expected: Int) {
            compose.waitUntil(5_000) { viewModel.state.value?.count == expected }
            compose.onNodeWithText(expected.toString()).assertIsDisplayed()
        }

        try {
            runBlocking { preferences.edit { it[intPreferencesKey("counter")] = 2 } }
            compose.setContent {
                MuslimAppTheme(darkTheme = true) {
                    Surface {
                        Box(Modifier.fillMaxSize()) { TasbexScreen(viewModel) }
                    }
                }
            }
            waitForCount(2)
            val counter = compose.onNodeWithText(text(R.string.dhikr))
            counter.performTouchInput { click() }
            waitForCount(3)
            counter.performTouchInput { longClick() }
            waitForCount(4)
            compose.onNodeWithText("−1").performClick()
            waitForCount(3)

            val reset = text(R.string.tasbih_reset_short)
            compose.onNodeWithText(reset).performClick()
            compose.onNodeWithText(text(R.string.cancel)).performClick()
            waitForCount(3)
            assertEquals(emptyList<Any>(), viewModel.state.value?.history)

            compose.onNodeWithText(reset).performClick()
            compose.onNode(hasText(reset) and hasAnyAncestor(isDialog())).performClick()
            waitForCount(0)
            compose.onNodeWithText("−1").assertIsNotEnabled()
            assertEquals(3, viewModel.state.value?.history?.single()?.count)

            compose.onNodeWithText(text(R.string.tasbih_history_short)).performClick()
            compose.onNodeWithText(text(R.string.dhikr_history)).assertIsDisplayed()
            compose.onNodeWithText("3").assertIsDisplayed()
        } finally {
            compose.runOnIdle { viewModels.clear() }
            scope.cancel()
            file.delete()
        }
    }
}
