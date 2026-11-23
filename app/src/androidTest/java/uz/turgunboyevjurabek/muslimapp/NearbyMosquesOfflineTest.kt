package uz.turgunboyevjurabek.muslimapp

import android.icu.text.NumberFormat
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosqueCatalog
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueArea
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.nearbyMosques
import uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation.NearbyMosquesRoute
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme

class NearbyMosquesOfflineTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun countryAndRegionUseTheSameScopeOnListAndMapTabs() {
        val context = AppLanguage.Uzbek.localize(InstrumentationRegistry.getInstrumentation().targetContext)
        val catalog = MosqueCatalog(context)
        val data = runBlocking { catalog.load() }
        val regions = MosqueArea.entries.mapNotNull { it.regionCode }.toSet()
        assertEquals(regions, data.mosques.map { it.regionCode }.toSet())
        val regionalCount = data.mosques.count { it.regionCode == MosqueArea.Samarqand.regionCode }
        val numberFormat = NumberFormat.getIntegerInstance(AppLanguage.Uzbek.locale)
        fun countLabel(value: Int) = context.getString(R.string.mosque_catalog_count, numberFormat.format(value))

        compose.setContent {
            MuslimAppTheme {
                NearbyMosquesRoute(
                    loadCatalog = catalog::load,
                    locate = { null },
                    language = AppLanguage.Uzbek,
                    darkTheme = false,
                    canLoadMap = false,
                    onClose = {},
                )
            }
        }
        compose.waitUntil(5_000) { compose.onNodeWithText(countLabel(data.mosques.size)).isDisplayed() }
        compose.onNodeWithContentDescription(context.getString(R.string.mosque_choose_area)).performClick()
        val region = hasText("Samarqand viloyati") and hasClickAction()
        compose.onNodeWithTag("mosque-area-options").performScrollToNode(region)
        compose.onNode(region).performClick()
        compose.onNodeWithText(countLabel(regionalCount)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.mosque_map)).performClick()
        compose.onNodeWithText(countLabel(regionalCount)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.mosque_map_offline)).assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.mosque_choose_area)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_area_country)).performClick()
        compose.onNodeWithText(countLabel(data.mosques.size)).assertIsDisplayed()
    }

    @Test
    fun findsVerifiedMosqueAcrossUzbekistanWithoutLocationOrInternet() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val catalog = MosqueCatalog(context)
        compose.setContent {
            MuslimAppTheme {
                NearbyMosquesRoute(
                    loadCatalog = catalog::load,
                    locate = { null },
                    language = AppLanguage.Uzbek,
                    darkTheme = false,
                    canLoadMap = false,
                    onClose = {},
                )
            }
        }

        val search = context.getString(R.string.mosque_search)
        compose.waitUntil(5_000) { compose.onNodeWithContentDescription(search).isDisplayed() }
        compose.onNodeWithContentDescription(search).performClick()
        compose.onNodeWithText(search).performTextInput("Faqih Abulays")
        compose.waitUntil(5_000) {
            compose.onNode(hasText("Faqih Abulays") and !hasSetTextAction()).isDisplayed()
        }
        compose.onNodeWithText(context.getString(R.string.mosque_show_on_map)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_map_offline)).assertIsDisplayed()
        compose.onNodeWithText("Faqih Abulays").assertIsDisplayed()
    }

    @Test
    fun manualCityShowsBundledMosquesAndMapExplainsOfflineState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val catalog = MosqueCatalog(context)
        val data = runBlocking { catalog.load() }
        val city = data.cities.first { it.name == "Toshkent" }
        val firstMosque = nearbyMosques(city.coordinates, data.mosques).first().place.name
            ?: context.getString(R.string.mosque_unnamed)

        compose.setContent {
            MuslimAppTheme {
                NearbyMosquesRoute(
                    loadCatalog = catalog::load,
                    locate = { null },
                    language = AppLanguage.Uzbek,
                    darkTheme = false,
                    canLoadMap = false,
                    onClose = {},
                )
            }
        }

        compose.onNodeWithContentDescription(context.getString(R.string.mosque_choose_area)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_area_nearby)).performClick()
        compose.waitUntil(5_000) {
            compose.onNodeWithText(context.getString(R.string.mosque_choose_origin))
                .isDisplayed()
        }
        compose.onNodeWithText(context.getString(R.string.mosque_choose_city)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_search_city)).performTextInput(city.name.orEmpty())
        compose.onNode(hasText(city.name.orEmpty()) and hasClickAction() and !hasSetTextAction())
            .performClick()
        compose.waitUntil(5_000) { compose.onNodeWithText(firstMosque).isDisplayed() }

        compose.onNodeWithTag("nearby-mosque-map").assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.mosque_map)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_map_offline)).assertIsDisplayed()
        compose.onNodeWithTag("nearby-mosque-map").assertDoesNotExist()

        compose.onNodeWithText(context.getString(R.string.mosque_list)).performClick()
        compose.onNodeWithText(firstMosque).assertIsDisplayed()
    }
}
