package uz.turgunboyevjurabek.muslimapp

import android.Manifest
import android.icu.text.NumberFormat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosqueCatalog
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosquePhotos
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueArea
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueBrowsePreferences
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueCoordinates
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.nearbyMosques
import uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation.NearbyMosquesRoute
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.AppThemeMode
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.MenuScreen

class NearbyMosquesOfflineTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun swipesBundledPhotosWithoutASourcePanel() {
        val context = AppLanguage.Uzbek.localize(InstrumentationRegistry.getInstrumentation().targetContext)
        val catalog = MosqueCatalog(context)
        showMosques(catalog)
        val search = context.getString(R.string.mosque_search)
        compose.waitUntil(5_000) { compose.onNodeWithContentDescription(search).isDisplayed() }
        compose.onNodeWithContentDescription(search).performClick()
        compose.onNodeWithText(search).performTextInput("Минор")
        compose.waitUntil(5_000) { compose.onNodeWithTag("mosque-photo").isDisplayed() }
        compose.onNodeWithTag("mosque-photo").performClick()
        compose.onNodeWithTag("mosque-photo-viewer").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.mosque_photo_position, 1, 4)).assertIsDisplayed()

        compose.onNodeWithTag("mosque-photo-pager").performTouchInput { swipeLeft() }
        compose.onNodeWithText(context.getString(R.string.mosque_photo_position, 2, 4)).assertIsDisplayed()
        compose.onNodeWithText("Surat manbasi").assertDoesNotExist()
        compose.onNodeWithText("https://commons.wikimedia.org/", substring = true).assertDoesNotExist()
        compose.onNodeWithContentDescription(context.getString(R.string.mosque_photo_previous)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_photo_position, 1, 4)).assertIsDisplayed()
    }

    @Test
    fun bundledPhotoCreditsAreAvailableInAbout() {
        val context = AppLanguage.Uzbek.localize(InstrumentationRegistry.getInstrumentation().targetContext)
        compose.setContent {
            MuslimAppTheme(darkTheme = false) {
                MenuScreen(AppThemeMode.Light, {}, AppLanguage.Uzbek, {}, 0.dp, 0.dp)
            }
        }
        compose.onNodeWithText(context.getString(R.string.about_app)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_photo_licenses)).performClick()
        val firstPhoto = MosquePhotos.credits.first()
        compose.onNodeWithText(firstPhoto.fileName).assertIsDisplayed()
        compose.onNodeWithText("${firstPhoto.author} · ${firstPhoto.license.title}").assertIsDisplayed()
        compose.onNodeWithText("${firstPhoto.sourceUrl}\n${firstPhoto.license.url}").assertExists()
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
        compose.onNode(hasText("Faqih Abulays") and hasClickAction() and !hasSetTextAction())
            .performSemanticsAction(SemanticsActions.OnClick)
        compose.onNodeWithTag("mosque-detail-sheet").assertIsDisplayed()
        compose.onNode(
            hasAnyAncestor(hasTestTag("mosque-detail-sheet")) and hasTestTag("mosque-photo-placeholder"),
            useUnmergedTree = true,
        ).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.mosque_photos_open)).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.mosque_show_on_map)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_map_offline)).assertIsDisplayed()
        compose.onNodeWithText("Faqih Abulays").assertIsDisplayed()
    }

    @Test
    @SdkSuppress(minSdkVersion = 28)
    fun defaultNearbyAndReselectionUseDeviceCoordinatesWithoutACitySheet() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = AppLanguage.Uzbek.localize(instrumentation.targetContext)
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.ACCESS_COARSE_LOCATION)
        val catalog = MosqueCatalog(context)
        val data = runBlocking { catalog.load() }
        val minor = data.mosques.first { it.id == "relation/4827007" }
        val gishtlik = data.mosques.first { it.id == "way/451808369" }
        var requests = 0
        showMosques(
            catalog,
            MosqueBrowsePreferences(cityId = data.cities.first { it.name == "Samarqand" }.id),
            findDeviceLocation = { if (requests++ == 0) minor.coordinates else gishtlik.coordinates },
        )

        val chooseArea = context.getString(R.string.mosque_choose_area)
        val nearest = context.getString(R.string.mosque_nearest)
        val nearby = context.getString(R.string.mosque_area_nearby)
        val citySearch = compose.onNodeWithTag("mosque-city-search")
        compose.waitUntil(5_000) { compose.onNodeWithText(minor.name.orEmpty()).isDisplayed() }
        compose.onNodeWithText(nearest).assertIsDisplayed()
        citySearch.assertDoesNotExist()
        compose.onNodeWithContentDescription(chooseArea).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_area_country)).performClick()
        compose.onNodeWithContentDescription(chooseArea).performClick()
        compose.onNodeWithText(nearby).performClick()
        compose.waitUntil(5_000) { compose.onNodeWithText(gishtlik.name.orEmpty()).isDisplayed() }
        compose.onNodeWithText(nearest).assertIsDisplayed()
        citySearch.assertDoesNotExist()
        compose.runOnIdle { assertEquals(2, requests) }
    }

    @Test
    @SdkSuppress(minSdkVersion = 28)
    fun manualCityShowsBundledMosquesAndMapExplainsOfflineState() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.ACCESS_COARSE_LOCATION)
        val catalog = MosqueCatalog(context)
        val data = runBlocking { catalog.load() }
        val city = data.cities.first { it.name == "Toshkent" }
        val firstMosque = nearbyMosques(city.coordinates, data.mosques).first().place.name
            ?: context.getString(R.string.mosque_unnamed)

        showMosques(catalog)

        compose.onNodeWithContentDescription(context.getString(R.string.mosque_choose_area)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_area_nearby)).performClick()
        val searchCity = context.getString(R.string.mosque_search_city)
        compose.waitUntil(5_000) { compose.onNodeWithText(context.getString(R.string.location_unavailable)).isDisplayed() }
        compose.onNodeWithTag("mosque-city-search").assertDoesNotExist()
        compose.onNodeWithContentDescription(context.getString(R.string.mosque_choose_area)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_choose_city)).performClick()
        compose.onNodeWithText(searchCity).performTextInput(city.name.orEmpty())
        compose.onNode(hasText(city.name.orEmpty()) and hasClickAction() and !hasSetTextAction())
            .performClick()
        compose.waitUntil(5_000) { compose.onNodeWithText(firstMosque).isDisplayed() }
        compose.onNodeWithText(context.getString(R.string.mosque_nearest)).assertIsDisplayed()
        compose.onNodeWithText(city.name.orEmpty()).assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.current_location)).assertDoesNotExist()
        compose.onNodeWithText(firstMosque).performClick()
        compose.onNodeWithTag("mosque-detail-sheet").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.mosque_show_on_map)).assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.close)).performClick()
        val byDistance = context.getString(R.string.mosque_sort_distance)
        val byName = context.getString(R.string.mosque_sort_name)
        compose.onNodeWithText(byDistance).performClick()
        compose.onNodeWithText(byName).performClick()
        compose.onNodeWithText(byName).performClick()
        val nameOption = compose.onNode(hasAnyAncestor(hasTestTag("mosque-sort-menu")) and hasText(byName))
        nameOption.assertIsSelected().performClick()
        compose.onNodeWithText(firstMosque).assertIsDisplayed()

        compose.onNodeWithTag("nearby-mosque-map").assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.mosque_map)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_map_offline)).assertIsDisplayed()
        compose.onNodeWithTag("nearby-mosque-map").assertDoesNotExist()
        compose.onNodeWithText(byName).assertDoesNotExist()
        compose.onNodeWithContentDescription(context.getString(R.string.mosque_choose_area)).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_choose_city)).assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.close)).performClick()

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

    @Test
    fun darkDetailOpensInAppGalleryAndDismissibleDirectionsMessage() {
        val context = AppLanguage.Uzbek.localize(InstrumentationRegistry.getInstrumentation().targetContext)
        val catalog = MosqueCatalog(context)
        val minorName = runBlocking { catalog.load() }.mosques.first { it.id == "relation/4827007" }.label("uz").orEmpty()
        showMosques(catalog, darkTheme = true, rejectDirections = true)
        val search = context.getString(R.string.mosque_search)
        compose.waitUntil(5_000) { compose.onNodeWithContentDescription(search).isDisplayed() }
        compose.onNodeWithContentDescription(search).performClick()
        compose.onNodeWithText(search).performTextInput("Minor")
        val match = hasText(minorName) and hasClickAction() and !hasSetTextAction()
        compose.waitUntil(5_000) { compose.onNode(match).isDisplayed() }
        compose.onNode(match).performSemanticsAction(SemanticsActions.OnClick)
        compose.waitUntil(5_000) { compose.onNodeWithTag("mosque-detail-sheet").isDisplayed() }
        compose.onNodeWithTag("mosque-detail-sheet").assertIsDisplayed()
        compose.onNode(hasText(context.getString(R.string.mosque_photos_open)) and hasClickAction()).performClick()
        compose.onNodeWithTag("mosque-photo-viewer").assertIsDisplayed()
        compose.onNode(hasAnyAncestor(hasTestTag("mosque-photo-viewer"))
            and hasContentDescription(context.getString(R.string.close))).performClick()

        compose.onNode(hasAnyAncestor(hasTestTag("mosque-detail-sheet"))
            and hasText(context.getString(R.string.mosque_directions))).performClick()
        compose.onNodeWithText(context.getString(R.string.mosque_directions_unavailable)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.mosque_directions_acknowledge)).performClick()
        compose.onNodeWithTag("mosque-detail-sheet").assertIsDisplayed()
    }

    private fun showMosques(
        catalog: MosqueCatalog,
        initialPreferences: MosqueBrowsePreferences = MosqueBrowsePreferences(area = MosqueArea.Uzbekistan),
        darkTheme: Boolean = false,
        rejectDirections: Boolean = false,
        findDeviceLocation: suspend () -> MosqueCoordinates? = { null },
    ) {
        compose.setContent {
            var preferences by remember { mutableStateOf(initialPreferences) }
            MuslimAppTheme(darkTheme = darkTheme) {
                val handler = if (rejectDirections) remember {
                    object : UriHandler {
                        override fun openUri(uri: String) { throw IllegalArgumentException("Test URI handler") }
                    }
                } else LocalUriHandler.current
                CompositionLocalProvider(LocalUriHandler provides handler) {
                    NearbyMosquesRoute(
                        loadCatalog = catalog::load,
                        findDeviceLocation = findDeviceLocation,
                        language = AppLanguage.Uzbek,
                        darkTheme = darkTheme,
                        canLoadMap = false,
                        preferences = preferences,
                        onPreferencesChange = { change -> preferences = change(preferences) },
                        onClose = {},
                    )
                }
            }
        }
    }
}
