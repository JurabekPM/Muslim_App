package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueArea
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueCoordinates
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosquePlace
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueSearchIndex
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.NearbyMosque
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.nearbyMosqueRadiusKm
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.displayName
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueAreaButton
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueBrowseCard
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueLocationBar
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueSortButton
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueThumbnail
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueViewSwitcher
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.mosqueDisplayName
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.mosqueDistanceText
import java.text.Collator

private val mosqueSwitcherSpace = 72.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NearbyMosquesScreen(
    language: AppLanguage,
    darkTheme: Boolean,
    canLoadMap: Boolean,
    area: MosqueArea,
    showMap: Boolean,
    cities: List<MosquePlace>,
    mosques: List<MosquePlace>,
    city: MosquePlace?,
    gps: MosqueCoordinates?,
    origin: MosqueCoordinates?,
    results: List<NearbyMosque>?,
    findingLocation: Boolean,
    locationMessage: Int?,
    loadFailed: Boolean,
    onRetry: () -> Unit,
    onAreaChange: (MosqueArea) -> Unit,
    onShowMapChange: (Boolean) -> Unit,
    onLocate: () -> Unit,
    onCity: (MosquePlace) -> Unit,
    onClose: () -> Unit,
) {
    val languageKey = if (language == AppLanguage.UzbekCyrillic) "uz-Cyrl" else language.locale.language
    val distanceFormat = remember(language) {
        val numbers = NumberFormat.getNumberInstance(language.locale).apply { maximumFractionDigits = 1 }
        MeasureFormat.getInstance(language.locale, MeasureFormat.FormatWidth.SHORT, numbers)
    }
    var showAreas by rememberSaveable { mutableStateOf(false) }
    var showCities by rememberSaveable { mutableStateOf(false) }
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var focusedId by rememberSaveable { mutableStateOf<String?>(null) }
    var sortByName by rememberSaveable { mutableStateOf(false) }
    val focusedMosque = remember(focusedId, mosques) { mosques.firstOrNull { it.id == focusedId } }
    val goBack: () -> Unit = {
        when {
            focusedId != null -> {
                focusedId = null
                searching = true
            }
            searching -> searching = false
            else -> onClose()
        }
    }
    BackHandler(enabled = searching || focusedId != null, onBack = goBack)
    val isNearby = area == MosqueArea.Nearby
    var selectedId by remember(area, origin) { mutableStateOf<String?>(null) }
    val visiblePlaces = remember(area, mosques, results) {
        area.selectMosques(mosques, results.orEmpty().map(NearbyMosque::place))
    }
    val distances = remember(isNearby, results) {
        if (isNearby) results.orEmpty().associate { it.place.id to it.distanceMeters } else emptyMap()
    }
    val loading = mosques.isEmpty() || (isNearby && origin != null && results == null)
    val waitingForOrigin = isNearby && origin == null
    val mapVisible = canLoadMap && !searching && (focusedMosque != null ||
        (showMap && !loading && !waitingForOrigin && visiblePlaces.isNotEmpty()))
    val browsing = !searching && focusedId == null
    var directionsFailed by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val openDirections: (MosqueCoordinates) -> Unit = { coordinates ->
        try {
            uriHandler.openUri("https://www.google.com/maps/dir/?api=1&destination=${coordinates.latitude}%2C${coordinates.longitude}")
        } catch (_: IllegalArgumentException) {
            directionsFailed = true
        }
    }

    Column(Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
        TopAppBar(
            title = { Text(stringResource(R.string.nearby_mosques), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                IconButton(onClick = goBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                }
            },
            actions = {
                if (!searching && focusedId == null && mosques.isNotEmpty()) {
                    IconButton(onClick = { searching = true }) {
                        Icon(Icons.Filled.Search, stringResource(R.string.mosque_search))
                    }
                }
            },
        )
        if (browsing) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val areaLabel = mosqueAreaLabel(area)
                if (!showMap) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MosqueAreaButton(areaLabel, { showAreas = true }, Modifier.weight(1f))
                        if (isNearby) {
                            MosqueSortButton(sortByName, { sortByName = it }, Modifier.weight(1f))
                        }
                    }
                }
                if (isNearby || showMap) {
                    val usingGps = isNearby && city == null && gps != null
                    val cityName = if (isNearby) city?.label(languageKey) else null
                    val title = when {
                        !isNearby -> areaLabel
                        usingGps -> stringResource(R.string.current_location)
                        else -> cityName ?: if (showMap) areaLabel else stringResource(R.string.mosque_choose_city)
                    }
                    val subtitle = when {
                        usingGps -> stringResource(R.string.mosque_gps_source)
                        cityName != null -> stringResource(R.string.mosque_city_source)
                        else -> null
                    }
                    MosqueLocationBar(
                        title = title,
                        subtitle = subtitle,
                        isGps = usingGps,
                        onSelect = {
                            if (showMap) showAreas = true
                            else if (cities.isNotEmpty()) showCities = true
                        },
                        selectLabel = stringResource(if (showMap) R.string.mosque_choose_area else R.string.mosque_choose_city),
                        onLocate = onLocate.takeIf { isNearby },
                        locating = findingLocation,
                    )
                    val status = if (findingLocation) R.string.finding_location else locationMessage
                    if (isNearby && status != null) {
                        Text(
                            stringResource(status),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (!mapVisible && !loading && !waitingForOrigin && !loadFailed) {
                    val count = remember(language, visiblePlaces.size) {
                        NumberFormat.getIntegerInstance(language.locale).format(visiblePlaces.size)
                    }
                    Text(
                        stringResource(R.string.mosque_catalog_count, count),
                        modifier = Modifier.padding(start = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                loadFailed -> MosqueStatus(stringResource(R.string.mosque_catalog_error), onRetry)
                searching -> MosqueSearch(
                    mosques = mosques,
                    cities = cities,
                    query = query,
                    onQueryChange = { query = it },
                    languageKey = languageKey,
                    format = distanceFormat,
                    openDirections = openDirections,
                    onShowOnMap = {
                        focusedId = it.id
                        searching = false
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                focusedMosque != null -> {
                    Column(Modifier.fillMaxSize()) {
                        if (canLoadMap) {
                            Box(Modifier.weight(1f).fillMaxWidth()) {
                                key(focusedMosque.id) {
                                    NearbyMosqueMap(
                                        origin = focusedMosque.coordinates,
                                        gps = gps,
                                        mosques = listOf(focusedMosque),
                                        selectedMosque = focusedMosque,
                                        darkTheme = darkTheme,
                                        onSelect = {},
                                    )
                                }
                            }
                        } else {
                            MosqueStatus(stringResource(R.string.mosque_map_offline))
                        }
                        MosqueRow(focusedMosque, null, languageKey, distanceFormat, openDirections, Modifier.padding(16.dp))
                    }
                }
                loading -> MosqueStatus(stringResource(R.string.mosque_catalog_loading), loading = true)
                waitingForOrigin -> MosqueStatus(stringResource(R.string.mosque_choose_origin))
                visiblePlaces.isEmpty() -> MosqueStatus(
                    if (isNearby) stringResource(R.string.mosque_no_results, nearbyMosqueRadiusKm)
                    else stringResource(R.string.mosque_search_empty)
                )
                showMap && !canLoadMap -> MosqueStatus(stringResource(R.string.mosque_map_offline))
                showMap -> {
                    val selected = remember(visiblePlaces, selectedId) {
                        visiblePlaces.firstOrNull { it.id == selectedId }
                    }
                    Box(Modifier.fillMaxSize()) {
                        key(area, if (isNearby) origin else null) {
                            NearbyMosqueMap(
                                origin = if (isNearby) checkNotNull(origin) else visiblePlaces.first().coordinates,
                                gps = gps,
                                mosques = visiblePlaces,
                                selectedMosque = selected,
                                darkTheme = darkTheme,
                                onSelect = { selectedId = it },
                                overview = if (isNearby) null else area,
                                bottomOverlay = mosqueSwitcherSpace,
                            ) {
                                selected?.let {
                                    MosqueBrowseCard(
                                        it,
                                        distances[it.id],
                                        languageKey,
                                        distanceFormat,
                                        onDirections = { openDirections(it.coordinates) },
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        onClose = { selectedId = null },
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    val nearest = visiblePlaces.firstOrNull().takeIf { isNearby }
                    val otherPlaces = remember(visiblePlaces, nearest, sortByName, language) {
                        val entries = if (nearest != null) visiblePlaces.drop(1) else visiblePlaces
                        if (nearest != null && sortByName) {
                            val collator = Collator.getInstance(language.locale)
                            entries.sortedWith { first, second ->
                                collator.compare(
                                    first.label(languageKey) ?: first.locality.orEmpty(),
                                    second.label(languageKey) ?: second.locality.orEmpty(),
                                )
                            }
                        } else entries
                    }
                    key(area, if (isNearby) origin else null) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = mosqueSwitcherSpace),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (nearest != null) item(key = nearest.id) {
                                MosqueBrowseCard(
                                    nearest, distances[nearest.id], languageKey, distanceFormat,
                                    onDirections = { openDirections(nearest.coordinates) }, featured = true,
                                )
                            }
                            items(otherPlaces, key = MosquePlace::id) { mosque ->
                                MosqueBrowseCard(
                                    mosque, distances[mosque.id], languageKey, distanceFormat,
                                    onDirections = { openDirections(mosque.coordinates) },
                                )
                            }
                            if (isNearby) item {
                                Text(
                                    stringResource(R.string.mosque_distance_hint),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            item {
                                TextButton(onClick = { uriHandler.openUri("https://www.openstreetmap.org/copyright") }) {
                                    Text("© OpenStreetMap", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
            if (browsing) {
                MosqueViewSwitcher(
                    showMap, onShowMapChange,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }

    if (showAreas) {
        MosqueAreaDialog(
            area,
            onClose = { showAreas = false },
            onChooseCity = {
                showAreas = false
                showCities = true
            }.takeIf { showMap && isNearby && cities.isNotEmpty() },
        ) {
            showAreas = false
            onAreaChange(it)
        }
    }
    if (showCities) {
        MosqueCityDialog(cities, languageKey, onClose = { showCities = false }) {
            showCities = false
            onCity(it)
        }
    }
    if (directionsFailed) {
        AlertDialog(
            onDismissRequest = { directionsFailed = false },
            text = { Text(stringResource(R.string.mosque_directions_error)) },
            confirmButton = { TextButton(onClick = { directionsFailed = false }) { Text(stringResource(R.string.close)) } },
        )
    }
}

@Composable
private fun mosqueAreaLabel(area: MosqueArea): String = when (area) {
    MosqueArea.Uzbekistan -> stringResource(R.string.mosque_area_country)
    MosqueArea.Nearby -> stringResource(R.string.mosque_area_nearby)
    else -> Regions.resolve(area.regionSlug).displayName()
}

@Composable
private fun MosqueAreaDialog(
    selected: MosqueArea,
    onClose: () -> Unit,
    onChooseCity: (() -> Unit)?,
    onSelect: (MosqueArea) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.mosque_choose_area)) },
        text = {
            LazyColumn(Modifier.heightIn(max = 400.dp).testTag("mosque-area-options")) {
                items(MosqueArea.entries) { area ->
                    Row(
                        Modifier.fillMaxWidth()
                            .selectable(area == selected, role = Role.RadioButton, onClick = { onSelect(area) })
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadioButton(selected = area == selected, onClick = null)
                        Text(mosqueAreaLabel(area))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.close)) } },
        dismissButton = {
            if (onChooseCity != null) {
                TextButton(onClick = onChooseCity) { Text(stringResource(R.string.mosque_choose_city)) }
            }
        },
    )
}

@Composable
internal fun MosqueStatus(message: String, retry: (() -> Unit)? = null, loading: Boolean = false) {
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (loading) CircularProgressIndicator()
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (retry != null) {
            TextButton(onClick = retry) { Text(stringResource(R.string.retry)) }
        }
    }
}

@Composable
private fun MosqueRow(
    place: MosquePlace,
    distanceMeters: Double?,
    languageKey: String,
    format: MeasureFormat,
    openDirections: (MosqueCoordinates) -> Unit,
    modifier: Modifier = Modifier,
    onShowOnMap: (() -> Unit)? = null,
) {
    val name = mosqueDisplayName(place, languageKey)
    val distance = mosqueDistanceText(distanceMeters, format)
    Surface(modifier.fillMaxWidth(), MaterialTheme.shapes.large, MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            Modifier.padding(start = 12.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            MosqueThumbnail(place.id, name)
            Column(Modifier.weight(1f)) {
                Text(
                    name,
                    style = MaterialTheme.typography.titleSmall,
                )
                place.address?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                place.locality?.takeIf { place.address == null }?.let { locality ->
                    Text(
                        if (place.localityApproximate) stringResource(R.string.mosque_near_locality, locality) else locality,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (distance != null) Text(distance, style = MaterialTheme.typography.bodySmall)
                if (onShowOnMap != null) {
                    TextButton(onClick = onShowOnMap) { Text(stringResource(R.string.mosque_show_on_map)) }
                }
                TextButton(onClick = { openDirections(place.coordinates) }) {
                    Text(stringResource(R.string.mosque_directions))
                }
            }
        }
    }
}

@Composable
private fun MosqueSearch(
    mosques: List<MosquePlace>,
    cities: List<MosquePlace>,
    query: String,
    onQueryChange: (String) -> Unit,
    languageKey: String,
    format: MeasureFormat,
    openDirections: (MosqueCoordinates) -> Unit,
    onShowOnMap: (MosquePlace) -> Unit,
    modifier: Modifier = Modifier,
) {
    val index by produceState<MosqueSearchIndex?>(null, mosques, cities) {
        value = withContext(Dispatchers.Default) { MosqueSearchIndex(mosques, cities) }
    }
    val matches by produceState<List<MosquePlace>?>(null, index, query) {
        val current = index ?: return@produceState
        if (query.trim().length >= 2) {
            delay(120)
            value = withContext(Dispatchers.Default) { current.search(query) }
        } else {
            value = null
        }
    }
    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.mosque_search)) },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        val results = matches
        when {
            query.trim().length < 2 -> MosqueStatus(stringResource(R.string.mosque_search_hint))
            results == null -> MosqueStatus(stringResource(R.string.mosque_catalog_loading), loading = true)
            results.isEmpty() -> MosqueStatus(stringResource(R.string.mosque_search_empty))
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(results, key = MosquePlace::id) { place ->
                    MosqueRow(place, null, languageKey, format, openDirections, onShowOnMap = { onShowOnMap(place) })
                }
            }
        }
    }
}

@Composable
private fun MosqueCityDialog(
    cities: List<MosquePlace>,
    languageKey: String,
    onClose: () -> Unit,
    onSelect: (MosquePlace) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val matches = remember(cities, query) {
        val search = query.trim().replace(mosqueApostrophes, "")
        cities.filter { city ->
            sequenceOf(city.name).plus(city.names.orEmpty().values)
                .filterNotNull().any { it.replace(mosqueApostrophes, "").contains(search, ignoreCase = true) }
        }
    }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(stringResource(R.string.mosque_choose_city)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.mosque_search_city)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (matches.isEmpty()) Text(stringResource(R.string.mosque_no_city))
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(matches, key = MosquePlace::id) { city ->
                        TextButton(onClick = { onSelect(city) }, modifier = Modifier.fillMaxWidth()) {
                            Text(city.label(languageKey).orEmpty(), Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.close)) } },
    )
}

private val mosqueApostrophes = Regex("[ʻʼ‘’']")
