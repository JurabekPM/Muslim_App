package uz.turgunboyevjurabek.muslimapp

import android.icu.text.NumberFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
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
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueBrowsePreferences
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.nearbyMosques
import uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation.NearbyMosquesRoute
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme

class NearbyMosquesOfflineTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun displaysBundledPhotoAndShowsItsAuthorAndLicense() {
        val context = AppLanguage.Uzbek.localize(InstrumentationRegistry.getInstrumentation().targetContext)
        val catalog = MosqueCatalog(context)
        showMosques(catalog)
        val search = context.getString(R.string.mosque_search)
        compose.waitUntil(5_000) { compose.onNodeWithContentDescription(search).isDisplayed() }
        compose.onNodeWithContentDescription(search).performClick()
        compose.onNodeWithText(search).performTextInput("Минор")
        compose.waitUntil(5_000) { compose.onNodeWithTag("mosque-photo").isDisplayed() }
        compose.onNodeWithTag("mosque-photo").performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_photo_credit, "Yuriy Pavlovich")).assertIsDisplayed()
        compose.onNodeWithText("CC0 1.0").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.mosque_photo_source)).assertIsDisplayed()
    }

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

        showMosques(catalog)
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
        showMosques(catalog)

        val search = context.getString(R.string.mosque_search)
        compose.waitUntil(5_000) { compose.onNodeWithContentDescription(search).isDisplayed() }
        compose.onNodeWithContentDescription(search).performClick()
        compose.onNodeWithText(search).performTextInput("Faqih Abulays")
        compose.waitUntil(5_000) {
            compose.onNode(hasText("Faqih Abulays") and !hasSetTextAction()).isDisplayed()
        }
        compose.onNodeWithTag("mosque-photo-placeholder").assertIsDisplayed()
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

        showMosques(catalog)

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
        compose.onNodeWithText(context.getString(R.string.mosque_nearest)).assertIsDisplayed()
        val byDistance = context.getString(R.string.mosque_sort_distance)
        val byName = context.getString(R.string.mosque_sort_name)
        compose.onNodeWithText(byDistance).performClick()
        compose.onNodeWithText(byName).performClick()
        compose.onNodeWithText(firstMosque).assertIsDisplayed()

        compose.onNodeWithTag("nearby-mosque-map").assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.mosque_map)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_map_offline)).assertIsDisplayed()
        compose.onNodeWithTag("nearby-mosque-map").assertDoesNotExist()
        compose.onNodeWithText(byName).assertDoesNotExist()
        compose.onNodeWithContentDescription(context.getString(R.string.mosque_choose_area)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_choose_city)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.close)).performClick()

        compose.onNodeWithText(context.getString(R.string.mosque_list)).performClick()
        compose.onNodeWithText(firstMosque).assertIsDisplayed()
        compose.onNodeWithText(byName).assertIsDisplayed()
    }

    @Test
    fun restoresMapAndRegionWithAnOfflineListFallback() {
        val context = AppLanguage.Uzbek.localize(InstrumentationRegistry.getInstrumentation().targetContext)
        val catalog = MosqueCatalog(context)
        val places = runBlocking { catalog.load() }
        val count = places.mosques.count { it.regionCode == MosqueArea.Andijon.regionCode }
        val amount = NumberFormat.getIntegerInstance(AppLanguage.Uzbek.locale).format(count)
        val countyCount = context.getString(R.string.mosque_catalog_count, amount)
        showMosques(catalog, MosqueBrowsePreferences(area = MosqueArea.Andijon, showMap = true))

        val offline = context.getString(R.string.mosque_map_offline)
        compose.waitUntil(5_000) { compose.onNodeWithText(offline).isDisplayed() }
        compose.onNodeWithText("Andijon viloyati").assertIsDisplayed()
        compose.onNodeWithText(countyCount).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.mosque_map)).assertIsSelected()

        val list = compose.onNodeWithText(context.getString(R.string.mosque_list))
        list.performClick().assertIsSelected()
        compose.onNodeWithText(offline).assertDoesNotExist()
        compose.onNodeWithText(countyCount).assertIsDisplayed()
    }

    private fun showMosques(
        catalog: MosqueCatalog,
        initialPreferences: MosqueBrowsePreferences = MosqueBrowsePreferences(),
    ) {
        compose.setContent {
            var preferences by remember { mutableStateOf(initialPreferences) }
            MuslimAppTheme {
                NearbyMosquesRoute(
                    loadCatalog = catalog::load,
                    locate = { null },
                    language = AppLanguage.Uzbek,
                    darkTheme = false,
                    canLoadMap = false,
                    preferences = preferences,
                    onPreferencesChange = { change -> preferences = change(preferences) },
                    onClose = {},
                )
            }
        }
    }
}
