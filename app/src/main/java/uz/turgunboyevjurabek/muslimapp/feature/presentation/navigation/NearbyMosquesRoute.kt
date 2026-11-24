package uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosqueCatalogData
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueArea
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueBrowsePreferences
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueCoordinates
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.NearbyMosque
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.nearbyMosques
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.NearbyMosquesScreen
import java.io.IOException

@Composable
internal fun NearbyMosquesRoute(
    loadCatalog: suspend () -> MosqueCatalogData,
    findDeviceLocation: suspend () -> MosqueCoordinates?,
    language: AppLanguage,
    darkTheme: Boolean,
    canLoadMap: Boolean,
    preferences: MosqueBrowsePreferences,
    onPreferencesChange: ((MosqueBrowsePreferences) -> MosqueBrowsePreferences) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
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
    var manualCityId by rememberSaveable { mutableStateOf<String?>(null) }
    val city = remember(catalog, manualCityId) {
        manualCityId?.let { id -> catalog?.cities?.firstOrNull { it.id == id } }
    }
    val usesDeviceLocation = area == MosqueArea.Nearby && manualCityId == null
    var locationAttempt by rememberSaveable { mutableIntStateOf(0) }
    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    var requestingPermission by rememberSaveable { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { requestingPermission = false }
    var deviceOrigin by remember(usesDeviceLocation, locationAttempt) { mutableStateOf<MosqueCoordinates?>(null) }
    var locationError by remember(usesDeviceLocation, locationAttempt) { mutableStateOf<Int?>(null) }
    LaunchedEffect(usesDeviceLocation, locationAttempt, requestingPermission) {
        if (!usesDeviceLocation || requestingPermission) return@LaunchedEffect
        if (mosqueLocationPermissions.any {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            deviceOrigin = findDeviceLocation()
            if (deviceOrigin == null) locationError = R.string.location_unavailable
        } else if (!permissionRequested) {
            permissionRequested = true
            requestingPermission = true
            permission.launch(mosqueLocationPermissions)
        } else {
            locationError = R.string.mosque_location_permission
        }
    }
    val origin = city?.coordinates ?: deviceOrigin
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
        lastCityId = preferences.cityId,
        origin = origin,
        results = results,
        loadFailed = loadFailed,
        onRetry = { loadAttempt++ },
        locationError = locationError,
        onRetryLocation = {
            permissionRequested = false
            locationAttempt++
        },
        onAreaChange = { selected ->
            manualCityId = null
            if (selected == MosqueArea.Nearby) locationAttempt++
            onPreferencesChange { it.copy(area = selected) }
        },
        onShowMapChange = { showMap -> onPreferencesChange { it.copy(showMap = showMap) } },
        onCity = { selected ->
            manualCityId = selected.id
            onPreferencesChange { it.copy(cityId = selected.id) }
        },
        onClose = onClose,
    )
}

private val mosqueLocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)
