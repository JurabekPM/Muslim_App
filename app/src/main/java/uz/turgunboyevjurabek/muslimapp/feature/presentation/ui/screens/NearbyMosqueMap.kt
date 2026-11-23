package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.CameraFit
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
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
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueArea
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueCoordinates
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosquePlace
import kotlin.math.cos

@Composable
@UiComposable
internal fun NearbyMosqueMap(
    origin: MosqueCoordinates,
    gps: MosqueCoordinates?,
    mosques: List<MosquePlace>,
    selectedMosque: MosquePlace?,
    darkTheme: Boolean,
    onSelect: (String?) -> Unit,
    overview: MosqueArea? = null,
    bottomOverlay: Dp = 0.dp,
    selectionCard: @Composable () -> Unit = {},
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
    val selectedPin = remember(selectedMosque) {
        selectedMosque?.let {
            featureCollection(listOf(pointFeature(it.coordinates)))
        } ?: emptyMosquePins
    }
    val ink = Color(0xFF233748)
    val neutralMarker = if (darkTheme) Color(0xFFE6EBEF) else Color(0xFFFFF8EA)
    val mosqueGlyph = painterResource(R.drawable.ic_mosque)
    val mosqueMarker = remember(mosqueGlyph, darkTheme) {
        MosqueMarkerPainter(
            mosqueGlyph, neutralMarker, ink,
        )
    }
    val selectedMarker = remember(mosqueGlyph) {
        MosqueMarkerPainter(mosqueGlyph, Color(0xFFF4CF75), Color(0xFF233748))
    }
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
                val locationSource = rememberGeoJsonSource(it)
                CircleLayer(
                    id = "muslim-app-location-halo",
                    source = locationSource,
                    radius = const(19.dp),
                    color = const(currentLocation.copy(alpha = 0.16f)),
                )
                CircleLayer(
                    id = "muslim-app-location",
                    source = locationSource,
                    radius = const(7.dp),
                    color = const(currentLocation),
                    strokeWidth = const(2.dp),
                    strokeColor = const(Color.White),
                )
            }
            val source = rememberGeoJsonSource(mosquePins) {
                cluster = overview != null
                clusterRadius = 36
                clusterMaxZoom = 14
            }
            if (overview != null) {
                CircleLayer(
                    id = "muslim-app-mosque-clusters",
                    source = source,
                    filter = feature.has("cluster_id"),
                    radius = const(15.dp),
                    color = const(neutralMarker),
                    strokeColor = const(Color.White),
                    strokeWidth = const(1.5.dp),
                    hitPadding = markerHitPadding,
                    onClick = { features ->
                        val point = features.closestTo(position)?.geometry as? Point
                        if (point == null) ClickResult.Pass else {
                            onSelect(null)
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
                    textColor = const(ink),
                    textAllowOverlap = const(true),
                )
            }
            SymbolLayer(
                id = "muslim-app-mosques",
                source = source,
                filter = feature.has("mosqueId"),
                iconImage = image(mosqueMarker, size = DpSize(33.dp, 33.dp)),
                iconAllowOverlap = const(true),
                iconIgnorePlacement = const(true),
                hitPadding = markerHitPadding,
                onClick = { features ->
                    val id = (features.closestTo(position)?.properties?.get("mosqueId") as? JsonPrimitive)?.content
                    if (id == null) {
                        ClickResult.Pass
                    } else {
                        onSelect(id)
                        ClickResult.Consume
                    }
                },
            )
            SymbolLayer(
                id = "muslim-app-mosque-selected",
                source = rememberGeoJsonSource(selectedPin),
                iconImage = image(selectedMarker, size = DpSize(40.dp, 40.dp)),
                iconAllowOverlap = const(true),
                iconIgnorePlacement = const(true),
            )
        }

        LaunchedEffect(state, bounds, bottomOverlay) {
            if (bounds != null) {
                state.fitCameraToBounds(
                    bounds,
                    CameraFit(fitPadding = DpPadding(left = 22.dp, top = 24.dp, right = 22.dp, bottom = 36.dp + bottomOverlay)),
                )
            }
        }
        LaunchedEffect(clusterCenter) {
            val center = clusterCenter ?: return@LaunchedEffect
            state.animateCamera(CameraUpdate(center = center, zoom = (state.cameraPosition.zoom + 2).coerceAtMost(16.0)))
            clusterCenter = null
        }
        val loadState = state.style.loadState
        val scope = rememberCoroutineScope()
        val surface = MaterialTheme.colorScheme.surface
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
                interactions = MapInteractions {
                    callbacks {
                        click {
                            onUnhandled {
                                if (selectedMosque == null) ClickResult.Pass else {
                                    onSelect(null)
                                    ClickResult.Consume
                                }
                            }
                        }
                    }
                },
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
            if (bottomOverlay > 0.dp) {
                Box(
                    Modifier.fillMaxWidth().height(24.dp)
                        .background(Brush.verticalGradient(listOf(surface, Color.Transparent))),
                )
            }
            Column(
                Modifier.align(Alignment.BottomEnd).padding(bottom = bottomOverlay),
                horizontalAlignment = Alignment.End,
            ) {
                if (gps != null && overview == null && bottomOverlay > 0.dp && selectedMosque == null && loadState == StyleLoadState.Ready) {
                    Surface(
                        modifier = Modifier.padding(end = 16.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shadowElevation = 3.dp,
                    ) {
                        IconButton(onClick = {
                            scope.launch {
                                state.animateCamera(
                                    CameraUpdate(center = Position(longitude = gps.longitude, latitude = gps.latitude)),
                                )
                            }
                        }) {
                            Icon(
                                painterResource(R.drawable.ic_mosque_recenter),
                                stringResource(R.string.mosque_recenter),
                            )
                        }
                    }
                }
                MapCredits(Modifier.padding(8.dp))
                selectionCard()
            }
        }
    }
}

private val markerHitPadding = 9.dp

private fun List<Feature<Geometry, JsonObject?>>.closestTo(position: Position?): Feature<Geometry, JsonObject?>? {
    if (position == null || size <= 1) return firstOrNull()
    val longitudeScale = cos(Math.toRadians(position.latitude))
    return minByOrNull { feature ->
        val point = feature.geometry as? Point ?: return@minByOrNull Double.POSITIVE_INFINITY
        val latitudeDelta = point.coordinates.latitude - position.latitude
        val longitudeDelta = (point.coordinates.longitude - position.longitude) * longitudeScale
        latitudeDelta * latitudeDelta + longitudeDelta * longitudeDelta
    }
}

private class MosqueMarkerPainter(
    private val glyph: Painter,
    private val background: Color,
    private val foreground: Color,
) : Painter() {
    override val intrinsicSize = Size.Unspecified

    override fun DrawScope.onDraw() {
        val radius = size.minDimension / 2
        drawCircle(Color.White, radius)
        drawCircle(background, radius - 1.25.dp.toPx())
        val glyphSize = radius * 1.18f
        translate(left = center.x - glyphSize / 2, top = center.y - glyphSize / 2) {
            with(glyph) {
                draw(Size(glyphSize, glyphSize), colorFilter = ColorFilter.tint(foreground))
            }
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
