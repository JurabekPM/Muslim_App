package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueCoordinates
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosquePlace
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueSearchIndex
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.NearbyMosque
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.nearbyMosqueRadiusKm
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NearbyMosquesScreen(
    language: AppLanguage,
    darkTheme: Boolean,
    canLoadMap: Boolean,
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
    onLocate: () -> Unit,
    onCity: (MosquePlace) -> Unit,
    onClose: () -> Unit,
) {
    val languageKey = if (language == AppLanguage.UzbekCyrillic) "uz-Cyrl" else language.locale.language
    val distanceFormat = remember(language) {
        val numbers = NumberFormat.getNumberInstance(language.locale).apply { maximumFractionDigits = 1 }
        MeasureFormat.getInstance(language.locale, MeasureFormat.FormatWidth.SHORT, numbers)
    }
    var showMap by rememberSaveable { mutableStateOf(false) }
    var showCities by rememberSaveable { mutableStateOf(false) }
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var focusedId by rememberSaveable { mutableStateOf<String?>(null) }
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
    var selectedId by remember(origin) { mutableStateOf<String?>(null) }
    val nearbyPlaces = remember(results) { results.orEmpty().map(NearbyMosque::place) }
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
        CenterAlignedTopAppBar(
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
        if (!searching && focusedId == null) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onLocate, enabled = !findingLocation, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.current_location), textAlign = TextAlign.Center)
                    }
                    OutlinedButton(onClick = { showCities = true }, enabled = cities.isNotEmpty(), modifier = Modifier.weight(1f)) {
                        Text(city?.label(languageKey) ?: stringResource(R.string.mosque_choose_city), textAlign = TextAlign.Center)
                    }
                }
                val status = if (findingLocation) R.string.finding_location else locationMessage
                if (status != null) {
                    Text(
                        stringResource(status),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !showMap,
                        onClick = { showMap = false },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text(stringResource(R.string.mosque_list)) }
                    SegmentedButton(
                        selected = showMap,
                        onClick = { showMap = true },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text(stringResource(R.string.mosque_map)) }
                }
            }
        }

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
                modifier = Modifier.weight(1f),
            )
            focusedMosque != null -> {
                if (canLoadMap) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        key(focusedMosque.id) {
                            NearbyMosqueMap(
                                origin = focusedMosque.coordinates,
                                gps = gps,
                                mosques = listOf(focusedMosque),
                                selectedId = focusedMosque.id,
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
            cities.isEmpty() || origin != null && results == null ->
                MosqueStatus(stringResource(R.string.mosque_catalog_loading), loading = true)
            origin == null -> MosqueStatus(stringResource(R.string.mosque_choose_origin))
            showMap && !canLoadMap -> MosqueStatus(stringResource(R.string.mosque_map_offline))
            showMap -> {
                Box(Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp)) {
                    key(origin) {
                        NearbyMosqueMap(
                            origin = origin,
                            gps = gps,
                            mosques = nearbyPlaces,
                            selectedId = selectedId,
                            darkTheme = darkTheme,
                            onSelect = { selectedId = it },
                        )
                    }
                }
                results?.firstOrNull { it.place.id == selectedId }?.let { selected ->
                    MosqueRow(selected.place, selected.distanceMeters, languageKey, distanceFormat, openDirections, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
            results.isNullOrEmpty() -> MosqueStatus(stringResource(R.string.mosque_no_results, nearbyMosqueRadiusKm))
            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        Text(
                            stringResource(R.string.mosque_distance_hint),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(results, key = { it.place.id }) { mosque ->
                        MosqueRow(mosque.place, mosque.distanceMeters, languageKey, distanceFormat, openDirections)
                    }
                }
            }
        }
        if (searching || !canLoadMap || focusedMosque == null && (!showMap || origin == null || results == null)) {
            TextButton(onClick = { uriHandler.openUri("https://www.openstreetmap.org/copyright") }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("© OpenStreetMap", style = MaterialTheme.typography.labelSmall)
            }
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
    val distance = remember(distanceMeters, format) {
        distanceMeters?.let {
            val measure = if (it < 1_000) Measure(it.roundToInt(), MeasureUnit.METER)
            else Measure(it / 1_000, MeasureUnit.KILOMETER)
            format.formatMeasures(measure)
        }
    }
    Surface(modifier.fillMaxWidth(), MaterialTheme.shapes.large, MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(
                    place.label(languageKey) ?: stringResource(
                        if (place.locality != null) R.string.mosque_generic else R.string.mosque_unnamed
                    ),
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
