package uz.turgunboyevjurabek.muslimapp

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.AppThemeMode
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.MenuScreen

class PrivacyPolicyTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun opensPolicyTextFromMenuAndClosesIt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val title = context.getString(R.string.privacy_policy)
        val summary = context.getString(R.string.privacy_policy_summary)
        val close = context.getString(R.string.close)
        compose.setContent {
            MuslimAppTheme {
                MenuScreen(
                    theme = AppThemeMode.System,
                    onThemeChange = {},
                    language = AppLanguage.Uzbek,
                    onLanguageChange = {},
                    onNearbyMosques = {},
                    topPadding = 0.dp,
                    bottomPadding = 0.dp,
                )
            }
        }

        compose.onNode(hasText(title) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)

        val inDialog = hasAnyAncestor(isDialog())
        compose.onNode(hasText(title) and inDialog).assertIsDisplayed()
        compose.onNode(hasText(summary) and inDialog).assertIsDisplayed()
        compose.onNode(hasText(close) and inDialog).performSemanticsAction(SemanticsActions.OnClick)
        compose.onNode(isDialog()).assertDoesNotExist()
    }
}
