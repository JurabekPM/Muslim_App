package uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
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
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueCoordinates
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.NearbyMosque
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.nearbyMosques
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.NearbyMosquesScreen
import java.io.IOException

@Composable
internal fun NearbyMosquesRoute(
    loadCatalog: suspend () -> MosqueCatalogData,
    locate: suspend () -> Location?,
    language: AppLanguage,
    darkTheme: Boolean,
    canLoadMap: Boolean,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun canLocate() = permissions.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    var cityId by rememberSaveable { mutableStateOf<String?>(null) }
    var latitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitude by rememberSaveable { mutableStateOf<Double?>(null) }
    var finding by remember { mutableStateOf(false) }
    var locationMessage by remember { mutableStateOf<Int?>(null) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) finding = true else locationMessage = R.string.mosque_location_permission
    }

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
    LaunchedEffect(Unit) {
        if (cityId == null && latitude == null && canLocate()) finding = true
    }
    LaunchedEffect(finding) {
        if (!finding) return@LaunchedEffect
        val location = locate()
        if (!finding) return@LaunchedEffect
        if (location == null) {
            locationMessage = R.string.location_unavailable
        } else {
            latitude = location.latitude
            longitude = location.longitude
            cityId = null
        }
        finding = false
    }

    val city = catalog?.cities?.firstOrNull { it.id == cityId }
    val gps = latitude?.let { lat -> longitude?.let { lon -> MosqueCoordinates(lat, lon) } }
    val origin = city?.coordinates ?: gps
    var results by remember(origin, catalog) { mutableStateOf<List<NearbyMosque>?>(null) }
    LaunchedEffect(origin, catalog) {
        val places = catalog?.mosques
        if (origin != null && places != null) {
            results = withContext(Dispatchers.Default) { nearbyMosques(origin, places) }
        }
    }
    NearbyMosquesScreen(
        language = language,
        darkTheme = darkTheme,
        canLoadMap = canLoadMap,
        cities = catalog?.cities.orEmpty(),
        mosques = catalog?.mosques.orEmpty(),
        city = city,
        gps = gps,
        origin = origin,
        results = results,
        findingLocation = finding,
        locationMessage = locationMessage,
        loadFailed = loadFailed,
        onRetry = { loadAttempt++ },
        onLocate = {
            locationMessage = null
            if (canLocate()) finding = true else permission.launch(permissions)
        },
        onCity = {
            finding = false
            locationMessage = null
            cityId = it.id
            latitude = null
            longitude = null
        },
        onClose = onClose,
    )
}
