package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
    featured: Boolean = false,
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
    val shape = RoundedCornerShape(if (featured) 16.dp else 14.dp)
    Surface(
        modifier = modifier.fillMaxWidth().clip(shape).clickable(role = Role.Button, onClick = onDetails),
        shape = shape,
        color = background,
        border = BorderStroke(0.75.dp, colors.outline.copy(alpha = 0.30f)),
    ) {
        Row(
            Modifier.padding(start = 8.dp, top = 6.dp, bottom = 6.dp, end = if (featured) 10.dp else 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val thumbnail = when {
                featured -> Modifier.weight(0.68f).height(136.dp)
                else -> Modifier.size(width = 74.dp, height = 58.dp)
            }
            MosqueThumbnail(place.id, name, thumbnail)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(if (featured) 4.dp else 2.dp)) {
                if (featured) {
                    Surface(shape = CircleShape, color = MosqueActionColor, contentColor = colors.onSecondaryContainer) {
                        Text(
                            stringResource(R.string.mosque_nearest),
                            Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (distance != null) {
                            Icon(
                                painterResource(R.drawable.ic_mosque_pin),
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = colors.onSurfaceVariant,
                            )
                        }
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (featured) {
                    Button(
                        onClick = onDirections,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MosqueActionColor,
                            contentColor = colors.onSecondaryContainer,
                        ),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                        ) {
                            Icon(painterResource(R.drawable.ic_mosque_route), null, Modifier.size(20.dp))
                            Text(stringResource(R.string.mosque_directions), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            if (!featured) MosqueDirectionsIconButton(onDirections)
        }
    }
}

@Composable
private fun MosqueDirectionsIconButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val isLight = colors.surface.luminance() > 0.5f
    val background = if (isLight) {
        lerp(colors.surfaceContainerLow, colors.tertiaryContainer, 0.75f)
    } else {
        colors.surfaceContainerHighest
    }
    val border = if (isLight) lerp(background, colors.tertiary, 0.20f) else colors.outline.copy(alpha = 0.55f)

    PressIconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(background, CircleShape)
                .border(1.dp, border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_mosque_direction),
                contentDescription = stringResource(R.string.mosque_directions),
                modifier = Modifier.size(24.dp),
                tint = if (isLight) colors.tertiary else MosqueActionColor,
            )
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
