package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraFit
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.DpPadding
import org.maplibre.spatialk.geojson.BoundingBox
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueArea
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueCoordinates
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosquePlace

@Composable
@UiComposable
internal fun NearbyMosqueMap(
    origin: MosqueCoordinates,
    gps: MosqueCoordinates?,
    mosques: List<MosquePlace>,
    selectedId: String?,
    darkTheme: Boolean,
    onSelect: (String) -> Unit,
    overview: MosqueArea? = null,
) {
    val mosquePins by produceState<GeoJsonData>(emptyMosquePins, mosques) {
        value = withContext(Dispatchers.Default) {
            featureCollection(mosques.map { pointFeature(it.coordinates, it.id) })
        }
    }
    val bounds = remember(overview, mosques) {
        when {
            overview == MosqueArea.Uzbekistan -> BoundingBox(
                Position(longitude = 55.9, latitude = 37.1),
                Position(longitude = 73.3, latitude = 45.7),
            )
            overview != null && mosques.size > 1 -> BoundingBox(
                Position(
                    longitude = mosques.minOf { it.coordinates.longitude } - 0.015,
                    latitude = mosques.minOf { it.coordinates.latitude } - 0.015,
                ),
                Position(
                    longitude = mosques.maxOf { it.coordinates.longitude } + 0.015,
                    latitude = mosques.maxOf { it.coordinates.latitude } + 0.015,
                ),
            )
            else -> null
        }
    }
    val gpsPin = remember(gps) { gps?.let { featureCollection(listOf(pointFeature(it))) } }
    val selectedPin = remember(selectedId, mosques) {
        mosques.firstOrNull { it.id == selectedId }?.let {
            featureCollection(listOf(pointFeature(it.coordinates)))
        }
    }
    val marker = if (darkTheme) Color(0xFFF4CF75) else Color(0xFF785213)
    val currentLocation = if (darkTheme) Color(0xFF69BEFF) else Color(0xFF136AC0)
    val style = if (darkTheme) "dark" else "liberty"
    var attempt by remember { mutableIntStateOf(0) }

    key(attempt) {
        var clusterCenter by remember { mutableStateOf<Position?>(null) }
        val state = rememberMapState(
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/$style"),
            initialCameraPosition = CameraPosition(
                center = if (overview == MosqueArea.Uzbekistan) Position(longitude = 64.6, latitude = 41.5)
                else Position(longitude = origin.longitude, latitude = origin.latitude),
                zoom = when (overview) {
                    null -> 12.3
                    MosqueArea.Uzbekistan -> 3.7
                    else -> 7.0
                },
            ),
        ) {
            gpsPin?.let {
                CircleLayer(
                    id = "muslim-app-location",
                    source = rememberGeoJsonSource(it),
                    radius = const(7.dp),
                    color = const(currentLocation),
                    strokeWidth = const(2.dp),
                    strokeColor = const(Color.White),
                )
            }
            val source = rememberGeoJsonSource(mosquePins) {
                cluster = overview != null
                clusterRadius = 24
                clusterMaxZoom = 14
            }
            if (overview != null) {
                CircleLayer(
                    id = "muslim-app-mosque-clusters",
                    source = source,
                    filter = feature.has("cluster_id"),
                    radius = const(13.dp),
                    color = const(marker),
                    strokeColor = const(Color.White),
                    strokeWidth = const(1.5.dp),
                    hitPadding = 8.dp,
                    onClick = { features ->
                        val point = features.firstOrNull()?.geometry as? Point
                        if (point == null) ClickResult.Pass else {
                            clusterCenter = point.coordinates
                            ClickResult.Consume
                        }
                    },
                )
                SymbolLayer(
                    id = "muslim-app-mosque-cluster-counts",
                    source = source,
                    filter = feature.has("cluster_id"),
                    textField = feature["point_count"].convertToString(),
                    textFont = const(listOf("Noto Sans Regular")),
                    textSize = const(11.sp),
                    textColor = const(if (darkTheme) Color(0xFF29200E) else Color.White),
                    textAllowOverlap = const(true),
                )
            }
            CircleLayer(
                id = "muslim-app-mosques",
                source = source,
                filter = feature.has("mosqueId"),
                radius = const(7.dp),
                color = const(marker),
                strokeWidth = const(2.dp),
                strokeColor = const(Color.White),
                hitPadding = 12.dp,
                onClick = { features ->
                    val id = (features.firstOrNull()?.properties?.get("mosqueId") as? JsonPrimitive)?.content
                    if (id == null) {
                        ClickResult.Pass
                    } else {
                        onSelect(id)
                        ClickResult.Consume
                    }
                },
            )
            selectedPin?.let {
                CircleLayer(
                    id = "muslim-app-mosque-selected",
                    source = rememberGeoJsonSource(it),
                    radius = const(9.dp),
                    color = const(marker),
                    strokeWidth = const(3.dp),
                    strokeColor = const(Color.White),
                )
            }
        }

        LaunchedEffect(state, bounds) {
            if (bounds != null) {
                state.fitCameraToBounds(
                    bounds,
                    CameraFit(fitPadding = DpPadding(left = 22.dp, top = 24.dp, right = 22.dp, bottom = 36.dp)),
                )
            }
        }
        LaunchedEffect(clusterCenter) {
            val center = clusterCenter ?: return@LaunchedEffect
            state.animateCamera(CameraUpdate(center = center, zoom = (state.cameraPosition.zoom + 2).coerceAtMost(16.0)))
            clusterCenter = null
        }
        val loadState = state.style.loadState
        var timedOut by remember { mutableStateOf(false) }
        LaunchedEffect(loadState) {
            if (loadState == StyleLoadState.Loading || loadState == StyleLoadState.Pending) {
                delay(15_000)
                timedOut = true
            }
        }
        Box(Modifier.fillMaxSize()) {
            MaplibreMap(
                state = state,
                modifier = Modifier.fillMaxSize().testTag("nearby-mosque-map"),
                overlay = {},
            )
            when {
                loadState == StyleLoadState.Ready -> Unit
                loadState is StyleLoadState.Failed || timedOut -> Surface(Modifier.align(Alignment.Center), MaterialTheme.shapes.large) {
                    MosqueStatus(stringResource(R.string.mosque_map_error), retry = { attempt++ })
                }
                else -> Surface(Modifier.align(Alignment.Center), MaterialTheme.shapes.large) {
                    MosqueStatus(stringResource(R.string.mosque_map_loading), loading = true)
                }
            }
            MapCredits(Modifier.align(Alignment.BottomEnd).padding(8.dp))
        }
    }
}

@Composable
private fun MapCredits(modifier: Modifier = Modifier) {
    val links = TextLinkStyles(style = SpanStyle(textDecoration = TextDecoration.Underline))
    val text = buildAnnotatedString {
        withLink(LinkAnnotation.Url("https://openfreemap.org/", links)) { append("OpenFreeMap") }
        append(" · ")
        withLink(LinkAnnotation.Url("https://openmaptiles.org/", links)) { append("© OpenMapTiles") }
        append(" · ")
        withLink(LinkAnnotation.Url("https://www.openstreetmap.org/copyright", links)) { append("© OpenStreetMap") }
    }
    Surface(modifier, RoundedCornerShape(8.dp), MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)) {
        Text(text, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
    }
}

private fun featureCollection(features: List<JSONObject>) = GeoJsonData.JsonString(
    JSONObject().put("type", "FeatureCollection").put("features", JSONArray(features)).toString(),
)

private val emptyMosquePins = GeoJsonData.JsonString("""{"type":"FeatureCollection","features":[]}""")

private fun pointFeature(coordinates: MosqueCoordinates, mosqueId: String? = null) = JSONObject()
    .put("type", "Feature")
    .put(
        "geometry",
        JSONObject()
            .put("type", "Point")
            .put("coordinates", JSONArray().put(coordinates.longitude).put(coordinates.latitude)),
    )
    .put("properties", JSONObject().put("mosqueId", mosqueId))
