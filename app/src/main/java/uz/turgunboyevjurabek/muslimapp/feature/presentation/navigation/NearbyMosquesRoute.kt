package uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosqueCatalogData
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueArea
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueBrowsePreferences
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.NearbyMosque
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.nearbyMosques
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.NearbyMosquesScreen
import java.io.IOException

@Composable
internal fun NearbyMosquesRoute(
    loadCatalog: suspend () -> MosqueCatalogData,
    language: AppLanguage,
    darkTheme: Boolean,
    canLoadMap: Boolean,
    preferences: MosqueBrowsePreferences,
    onPreferencesChange: ((MosqueBrowsePreferences) -> MosqueBrowsePreferences) -> Unit,
    onClose: () -> Unit,
) {
    val area = preferences.area
    var catalog by remember { mutableStateOf<MosqueCatalogData?>(null) }
    var loadFailed by remember { mutableStateOf(false) }
    var loadAttempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(loadAttempt) {
        loadFailed = false
        catalog = try {
            loadCatalog()
        } catch (_: IOException) {
            loadFailed = true
            null
        } catch (_: JsonParseException) {
            loadFailed = true
            null
        }
    }
    val city = remember(catalog, preferences.cityId) {
        catalog?.cities?.firstOrNull { it.id == preferences.cityId }
    }
    val origin = city?.coordinates
    var results by remember(origin, catalog) { mutableStateOf<List<NearbyMosque>?>(null) }
    LaunchedEffect(area, origin, catalog) {
        val places = catalog?.mosques
        if (area == MosqueArea.Nearby && origin != null && places != null && results == null) {
            results = withContext(Dispatchers.Default) { nearbyMosques(origin, places) }
        }
    }
    NearbyMosquesScreen(
        language = language,
        darkTheme = darkTheme,
        canLoadMap = canLoadMap,
        area = area,
        showMap = preferences.showMap,
        cities = catalog?.cities.orEmpty(),
        mosques = catalog?.mosques.orEmpty(),
        city = city,
        results = results,
        loadFailed = loadFailed,
        onRetry = { loadAttempt++ },
        onAreaChange = { selected -> onPreferencesChange { it.copy(area = selected) } },
        onShowMapChange = { showMap -> onPreferencesChange { it.copy(showMap = showMap) } },
        onCity = { selected -> onPreferencesChange { it.copy(cityId = selected.id) } },
        onClose = onClose,
    )
}
