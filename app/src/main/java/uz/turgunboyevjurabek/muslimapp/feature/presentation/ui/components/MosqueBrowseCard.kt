package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosquePlace
import kotlin.math.roundToInt

@Composable
internal fun MosqueBrowseCard(
    place: MosquePlace,
    distanceMeters: Double?,
    languageKey: String,
    format: MeasureFormat,
    onDirections: () -> Unit,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
    onClose: (() -> Unit)? = null,
) {
    val name = mosqueDisplayName(place, languageKey)
    val distance = mosqueDistanceText(distanceMeters, format)
    val subtitle = distance ?: place.address ?: place.locality?.let {
        if (place.localityApproximate) stringResource(R.string.mosque_near_locality, it) else it
    }
    val colors = MaterialTheme.colorScheme
    val background = when {
        featured && colors.surface.luminance() > 0.5f -> colors.tertiaryContainer.copy(alpha = 0.6f)
        else -> colors.surfaceContainerLow
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = background,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = if (onClose != null) 4.dp else 0.dp,
    ) {
        Row(
            Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MosqueThumbnail(place.id, name, Modifier.size(if (featured) 104.dp else 58.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (featured) {
                    Surface(shape = CircleShape, color = colors.secondaryContainer, contentColor = colors.onSecondaryContainer) {
                        Text(
                            stringResource(R.string.mosque_nearest),
                            Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Text(
                    name,
                    style = if (featured) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (featured || onClose != null) {
                    Button(
                        onClick = onDirections,
                        modifier = Modifier.padding(top = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.secondaryContainer,
                            contentColor = colors.onSecondaryContainer,
                        ),
                    ) {
                        Text(stringResource(R.string.mosque_directions))
                    }
                }
            }
            when {
                onClose != null -> IconButton(onClick = onClose, modifier = Modifier.align(Alignment.Top)) {
                    Icon(Icons.Filled.Close, stringResource(R.string.close), Modifier.size(20.dp))
                }
                !featured -> IconButton(onClick = onDirections) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.mosque_directions))
                }
            }
        }
    }
}

@Composable
internal fun mosqueDisplayName(place: MosquePlace, languageKey: String): String =
    place.label(languageKey) ?: stringResource(
        if (place.locality != null) R.string.mosque_generic else R.string.mosque_unnamed,
    )

@Composable
internal fun mosqueDistanceText(meters: Double?, format: MeasureFormat): String? = remember(meters, format) {
    meters?.let {
        val measure = if (it < 1_000) Measure(it.roundToInt(), MeasureUnit.METER)
        else Measure(it / 1_000, MeasureUnit.KILOMETER)
        format.formatMeasures(measure)
    }
}
