package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
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
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueBrowseCard
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueBrowseFilters
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueChoiceRow
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueDetailSheet
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueDirectionsDialog
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueGlass
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueGlassBackButton
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueGlassHeaderBackground
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueOverlayHeader
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueSearchDock
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MosqueSheet
import java.text.Collator

private val mosqueSwitcherSpace = 72.dp

private data class MosqueListOrder(
    val places: List<MosquePlace>,
    val distances: Map<String, Double> = emptyMap(),
)

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
    lastCityId: String?,
    origin: MosqueCoordinates?,
    results: List<NearbyMosque>?,
    loadFailed: Boolean,
    onRetry: () -> Unit,
    @StringRes locationError: Int?,
    onRetryLocation: () -> Unit,
    onAreaChange: (MosqueArea) -> Unit,
    onShowMapChange: (Boolean) -> Unit,
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
    var searchMap by rememberSaveable { mutableStateOf(false) }
    var searchSortByName by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var requestSearchFocus by remember { mutableStateOf(false) }
    val searchText = rememberTextFieldState()
    val query = searchText.text.toString()
    var focusedId by rememberSaveable { mutableStateOf<String?>(null) }
    var focusedFromSearch by rememberSaveable { mutableStateOf(false) }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var preferredSortByName by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val focusedMosque = remember(focusedId, mosques) { focusedId?.let { id -> mosques.firstOrNull { it.id == id } } }
    val detailPlace = remember(detailId, mosques) { detailId?.let { id -> mosques.firstOrNull { it.id == id } } }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val dismissKeyboard = {
        requestSearchFocus = false
        focusManager.clearFocus()
        keyboard?.hide()
    }
    val closeSearch = {
        dismissKeyboard()
        searching = false
        searchText.clearText()
    }
    val goBack: () -> Unit = {
        when {
            focusedId != null -> {
                focusedId = null
                searching = focusedFromSearch
            }
            searching -> closeSearch()
            else -> onClose()
        }
    }
    BackHandler(enabled = searching || focusedId != null, onBack = goBack)
    val isNearby = area == MosqueArea.Nearby
    val canSortByDistance = origin != null
    val sortByName = !canSortByDistance || (preferredSortByName ?: !isNearby)
    val visiblePlaces = remember(area, mosques, results) {
        area.selectMosques(mosques, results.orEmpty().map(NearbyMosque::place))
    }
    val nearbyDistances = remember(results) { results.orEmpty().associate { it.place.id to it.distanceMeters } }
    val nearest = visiblePlaces.firstOrNull().takeIf { isNearby }
    val listPlaces = remember(visiblePlaces, nearest) {
        if (nearest == null) visiblePlaces else visiblePlaces.drop(1)
    }
    val loading = mosques.isEmpty() || (isNearby && origin != null && results == null)
    val waitingForOrigin = isNearby && origin == null
    val activeMap = if (searching) searchMap else showMap
    val showingList = focusedId == null && !activeMap && !loading && !waitingForOrigin && !loadFailed
    var listOrder by remember(listPlaces) { mutableStateOf<MosqueListOrder?>(null) }
    LaunchedEffect(showingList, listPlaces, sortByName, isNearby, origin, language) {
        if (showingList && listPlaces.isNotEmpty()) {
            listOrder = when {
                sortByName -> withContext(Dispatchers.Default) {
                    val collator = Collator.getInstance(language.locale)
                    MosqueListOrder(listPlaces.sortedWith(compareBy(collator) {
                        it.label(languageKey) ?: it.locality.orEmpty()
                    }))
                }
                !isNearby -> withContext(Dispatchers.Default) {
                    val distances = listPlaces.associate { it.id to origin.distanceTo(it.coordinates) }
                    MosqueListOrder(listPlaces.sortedBy { distances.getValue(it.id) }, distances)
                }
                else -> MosqueListOrder(listPlaces)
            }
        }
    }
    val distances = if (isNearby) nearbyDistances else listOrder?.distances.orEmpty()
    val searchTerm = query.trim().takeIf { searching && it.length >= 2 }
    var searchIndex by remember(visiblePlaces, cities) { mutableStateOf<MosqueSearchIndex?>(null) }
    val needsSearchIndex = searchTerm != null && visiblePlaces.isNotEmpty()
    LaunchedEffect(needsSearchIndex, visiblePlaces, cities) {
        if (needsSearchIndex && searchIndex == null) {
            searchIndex = withContext(Dispatchers.Default) { MosqueSearchIndex(visiblePlaces, cities) }
        }
    }
    var lookup by remember(searchIndex) { mutableStateOf<Pair<String, List<MosquePlace>>?>(null) }
    LaunchedEffect(searchIndex, searchTerm) {
        val index = searchIndex
        when {
            searchTerm == null -> lookup = null
            index != null -> {
                delay(120)
                lookup = searchTerm to withContext(Dispatchers.Default) { index.search(searchTerm) }
            }
        }
    }
    val displayedLookup = lookup.takeIf { searchTerm != null }
    val matchedPlaces = displayedLookup?.second
    val orderedSearchPlaces by produceState<Pair<List<MosquePlace>, List<MosquePlace>>?>(
        null, matchedPlaces, searchSortByName, origin, language,
    ) {
        value = matchedPlaces?.let { matches ->
            val ordered = when {
                searchSortByName == true -> withContext(Dispatchers.Default) {
                    val collator = Collator.getInstance(language.locale)
                    matches.sortedWith(compareBy(collator) { it.label(languageKey) ?: it.locality.orEmpty() })
                }
                searchSortByName == false && origin != null -> withContext(Dispatchers.Default) {
                    matches.map { it to origin.distanceTo(it.coordinates) }.sortedBy { it.second }.map { it.first }
                }
                else -> matches
            }
            matches to ordered
        }
    }
    val browseList = key(area, if (isNearby) origin else null) { rememberLazyListState() }
    val searchList = key(displayedLookup?.first) { rememberLazyListState() }
    val activeList = if (matchedPlaces != null) searchList else browseList
    val mapPlaces = matchedPlaces ?: visiblePlaces
    val mapVisible = canLoadMap && activeMap && !loading && !waitingForOrigin && mapPlaces.isNotEmpty()
    val scrolled by remember(activeList) {
        derivedStateOf { activeList.firstVisibleItemIndex > 0 || activeList.firstVisibleItemScrollOffset > 96 }
    }
    val compactHeader = focusedId == null && !activeMap && scrolled
    val backdrop = remember { HazeState() }
    val glassInput = remember(backdrop) { HazeInput.Sources(backdrop) }
    val glass = MosqueGlass(glassInput, darkTheme, mapVisible)
    var directionsFailed by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val openDirections: (MosqueCoordinates) -> Unit = { coordinates ->
        try {
            uriHandler.openUri("https://www.google.com/maps/dir/?api=1&destination=${coordinates.latitude}%2C${coordinates.longitude}")
        } catch (_: IllegalArgumentException) {
            directionsFailed = true
        }
    }

    MosqueChromeLayout(
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        header = {
            Box(Modifier.fillMaxWidth()) {
                MosqueGlassHeaderBackground(glass, Modifier.matchParentSize())
                Column {
                    AnimatedVisibility(visible = !compactHeader) {
                        TopAppBar(
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Transparent,
                                scrolledContainerColor = Color.Transparent,
                            ),
                            title = {
                                Text(stringResource(R.string.nearby_mosques), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            navigationIcon = { MosqueGlassBackButton(glass, enabled = !compactHeader, goBack) },
                        )
                    }
                    if (focusedId == null) {
                        Column(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            val nearbyCityName = city?.label(languageKey).takeIf { isNearby }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedVisibility(
                                    visible = compactHeader,
                                    enter = expandHorizontally(expandFrom = Alignment.Start),
                                    exit = shrinkHorizontally(shrinkTowards = Alignment.Start),
                                ) {
                                    MosqueGlassBackButton(glass, enabled = compactHeader, goBack)
                                }
                                MosqueBrowseFilters(
                                    glass = glass,
                                    label = nearbyCityName ?: mosqueAreaLabel(area),
                                    showSort = !activeMap,
                                    sortByName = if (searching) searchSortByName ?: sortByName else sortByName,
                                    canSortByDistance = canSortByDistance,
                                    onAreaClick = { dismissKeyboard(); showAreas = true },
                                    onSortChange = {
                                        if (searching) searchSortByName = it else preferredSortByName = it
                                    },
                                )
                            }
                            if (matchedPlaces == null && !compactHeader && !mapVisible && !loading &&
                                !waitingForOrigin && !loadFailed && (!isNearby || activeMap)
                            ) {
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
                }
            }
        },
    ) { headerHeight ->
        val belowHeader = Modifier.padding(top = headerHeight)
        Box(Modifier.fillMaxSize().imePadding()) {
            Box(Modifier.matchParentSize().hazeSource(backdrop)) {
                when {
                    loadFailed -> MosqueStatus(
                        stringResource(R.string.mosque_catalog_error), modifier = belowHeader, retry = onRetry,
                    )
                    focusedMosque != null -> {
                        Column(Modifier.fillMaxSize().then(belowHeader)) {
                            if (canLoadMap) {
                                Box(Modifier.weight(1f).fillMaxWidth()) {
                                    key(focusedMosque.id) {
                                        NearbyMosqueMap(
                                            origin = focusedMosque.coordinates,
                                            mosques = listOf(focusedMosque),
                                            selectedMosque = focusedMosque,
                                            darkTheme = darkTheme,
                                            onSelect = { detailId = it },
                                        )
                                    }
                                }
                            } else {
                                MosqueStatus(stringResource(R.string.mosque_map_offline))
                            }
                            MosqueBrowseCard(
                                focusedMosque, distances[focusedMosque.id], languageKey, distanceFormat,
                                onDirections = { openDirections(focusedMosque.coordinates) },
                                onDetails = { detailId = focusedMosque.id },
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                    loading -> MosqueStatus(stringResource(R.string.mosque_catalog_loading), loading = true, modifier = belowHeader)
                    waitingForOrigin -> if (locationError == null) {
                        MosqueStatus(stringResource(R.string.locating), loading = true, modifier = belowHeader)
                    } else {
                        MosqueStatus(stringResource(locationError), retry = onRetryLocation, modifier = belowHeader)
                    }
                    visiblePlaces.isEmpty() -> MosqueStatus(
                        if (isNearby) stringResource(R.string.mosque_no_results, nearbyMosqueRadiusKm)
                        else stringResource(R.string.mosque_search_empty),
                        modifier = belowHeader,
                    )
                    activeMap && matchedPlaces?.isEmpty() == true ->
                        MosqueStatus(stringResource(R.string.mosque_search_empty), modifier = belowHeader)
                    activeMap && !canLoadMap -> MosqueStatus(stringResource(R.string.mosque_map_offline), modifier = belowHeader)
                    activeMap -> {
                        Box(Modifier.fillMaxSize()) {
                            key(area, if (isNearby) origin else null) {
                                NearbyMosqueMap(
                                    origin = if (isNearby && matchedPlaces == null) checkNotNull(origin)
                                    else mapPlaces.first().coordinates,
                                    mosques = mapPlaces,
                                    selectedMosque = detailPlace,
                                    darkTheme = darkTheme,
                                    onSelect = { detailId = it },
                                    overview = if (isNearby || matchedPlaces != null) null else area,
                                    fitResults = matchedPlaces != null,
                                    topOverlay = headerHeight,
                                    bottomOverlay = mosqueSwitcherSpace,
                                )
                            }
                        }
                    }
                    matchedPlaces == null && listOrder == null && listPlaces.isNotEmpty() ->
                        MosqueStatus(stringResource(R.string.mosque_catalog_loading), loading = true, modifier = belowHeader)
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().testTag("mosque-browse-list"),
                            state = activeList,
                            contentPadding = PaddingValues(
                                start = 16.dp, top = headerHeight + 12.dp, end = 16.dp, bottom = mosqueSwitcherSpace,
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (matchedPlaces?.isEmpty() == true) item {
                                MosqueStatus(stringResource(R.string.mosque_search_empty))
                            }
                            if (matchedPlaces == null && nearest != null) item(key = nearest.id) {
                                MosqueBrowseCard(
                                    nearest, distances[nearest.id], languageKey, distanceFormat,
                                    onDirections = { openDirections(nearest.coordinates) },
                                    onDetails = { detailId = nearest.id }, featured = true,
                                )
                            }
                            val places = if (matchedPlaces != null) {
                                orderedSearchPlaces?.takeIf { it.first === matchedPlaces }?.second ?: matchedPlaces
                            } else listOrder?.places.orEmpty()
                            items(places, key = MosquePlace::id) { mosque ->
                                MosqueBrowseCard(
                                    mosque, distances[mosque.id], languageKey, distanceFormat,
                                    onDirections = { openDirections(mosque.coordinates) },
                                    onDetails = { dismissKeyboard(); detailId = mosque.id },
                                )
                            }
                            if (isNearby && matchedPlaces == null) item {
                                Text(
                                    stringResource(R.string.mosque_distance_hint),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            item {
                                Text(
                                    "© OpenStreetMap",
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            if (focusedId == null) {
                MosqueSearchDock(
                    glass = glass, showMap = activeMap, searching = searching,
                    searchAvailable = mosques.isNotEmpty(), searchText = searchText, requestFocus = requestSearchFocus,
                    onFocusRequested = { requestSearchFocus = false },
                    onSearchOpen = {
                        searchMap = showMap
                        searchSortByName = null
                        requestSearchFocus = true
                        searching = true
                    },
                    onSearchClose = closeSearch,
                    onViewChange = {
                        if (searching) {
                            dismissKeyboard()
                            searchMap = it
                        } else onShowMapChange(it)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }

    if (showAreas) {
        MosqueAreaSheet(
            area,
            onClose = { showAreas = false },
            onChooseCity = {
                showAreas = false
                showCities = true
            }.takeIf { isNearby && cities.isNotEmpty() },
        ) {
            showAreas = false
            if (searching) closeSearch()
            onAreaChange(it)
        }
    }
    if (showCities) {
        MosqueCitySheet(cities, lastCityId, languageKey, onClose = { showCities = false }) {
            showCities = false
            onCity(it)
        }
    }
    if (detailPlace != null) {
        MosqueDetailSheet(
            detailPlace,
            distances[detailPlace.id],
            languageKey,
            distanceFormat,
            onDirections = { openDirections(detailPlace.coordinates) },
            onClose = { detailId = null },
            onShowOnMap = {
                detailId = null
                focusedFromSearch = searching
                focusedId = detailPlace.id
                searching = false
            }.takeIf { focusedId == null && !activeMap },
        )
    }
    if (directionsFailed) {
        MosqueDirectionsDialog { directionsFailed = false }
    }
}

@Composable
private fun MosqueChromeLayout(
    modifier: Modifier,
    header: @Composable () -> Unit,
    content: @Composable (Dp) -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val appBar = subcompose("appBar", header).single()
            .measure(constraints.copy(minHeight = 0))
        val body = subcompose("mosques") { content(appBar.height.toDp()) }.single().measure(constraints)
        layout(body.width, body.height) {
            body.placeRelative(0, 0)
            appBar.placeRelative(0, 0)
        }
    }
}

@Composable
private fun mosqueAreaLabel(area: MosqueArea): String = when (area) {
    MosqueArea.Uzbekistan -> stringResource(R.string.mosque_area_country)
    MosqueArea.Nearby -> stringResource(R.string.mosque_area_nearby)
    else -> Regions.resolve(area.regionSlug).displayName()
}

@Composable
private fun MosqueAreaSheet(
    selected: MosqueArea,
    onClose: () -> Unit,
    onChooseCity: (() -> Unit)?,
    onSelect: (MosqueArea) -> Unit,
) {
    MosqueSheet(onDismiss = onClose) { finish ->
        MosqueOverlayHeader(stringResource(R.string.mosque_choose_area)) { finish(onClose) }
        if (onChooseCity != null) {
            TextButton(onClick = { finish(onChooseCity) }) { Text(stringResource(R.string.mosque_choose_city)) }
        }
        LazyColumn(
            Modifier.heightIn(max = 440.dp).testTag("mosque-area-options"),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(listOf(MosqueArea.Nearby, MosqueArea.Uzbekistan), key = { it.name }) { area ->
                MosqueChoiceRow(
                    mosqueAreaLabel(area), area == selected,
                    onSelect = { finish { onSelect(area) } },
                    icon = if (area == MosqueArea.Uzbekistan) R.drawable.ic_mosque_country else R.drawable.ic_qibla_location,
                    outlined = true,
                )
            }
            item {
                HorizontalDivider(Modifier.padding(top = 4.dp, bottom = 8.dp))
                Text(stringResource(R.string.mosque_regions), style = MaterialTheme.typography.titleSmall)
            }
            items(MosqueArea.entries.drop(2), key = { it.name }) { area ->
                MosqueChoiceRow(
                    mosqueAreaLabel(area), area == selected,
                    onSelect = { finish { onSelect(area) } },
                    outlined = true,
                )
            }
        }
    }
}

@Composable
internal fun MosqueStatus(
    message: String,
    modifier: Modifier = Modifier,
    retry: (() -> Unit)? = null,
    loading: Boolean = false,
) {
    Column(
        modifier.fillMaxWidth().padding(24.dp),
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
private fun MosqueCitySheet(
    cities: List<MosquePlace>,
    selectedId: String?,
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
    MosqueSheet(onDismiss = onClose) { finish ->
        MosqueOverlayHeader(stringResource(R.string.mosque_choose_city)) { finish(onClose) }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(stringResource(R.string.mosque_search_city)) },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth().testTag("mosque-city-search"),
        )
        if (matches.isEmpty()) Text(stringResource(R.string.mosque_no_city))
        LazyColumn(Modifier.heightIn(max = 340.dp)) {
            items(matches, key = MosquePlace::id) { city ->
                MosqueChoiceRow(
                    city.label(languageKey).orEmpty(), city.id == selectedId,
                    onSelect = { finish { onSelect(city) } },
                )
                if (city.id != selectedId) {
                    HorizontalDivider(
                        Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

private val mosqueApostrophes = Regex("[ʻʼ‘’']")
