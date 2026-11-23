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
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonPrimitive
import uz.turgunboyevjurabek.muslimapp.R
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
) {
    val mosquePins = remember(mosques) {
        featureCollection(mosques.map { pointFeature(it.coordinates, it.id) })
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
        val state = rememberMapState(
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/$style"),
            initialCameraPosition = CameraPosition(
                center = Position(longitude = origin.longitude, latitude = origin.latitude),
                zoom = 12.3,
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
            CircleLayer(
                id = "muslim-app-mosques",
                source = rememberGeoJsonSource(mosquePins),
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

private fun pointFeature(coordinates: MosqueCoordinates, mosqueId: String? = null) = JSONObject()
    .put("type", "Feature")
    .put(
        "geometry",
        JSONObject()
            .put("type", "Point")
            .put("coordinates", JSONArray().put(coordinates.longitude).put(coordinates.latitude)),
    )
    .put("properties", JSONObject().put("mosqueId", mosqueId))
