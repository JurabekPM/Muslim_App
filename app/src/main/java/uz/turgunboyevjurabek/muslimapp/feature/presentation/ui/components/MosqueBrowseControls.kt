package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R

internal val MosqueActionColor = Color(0xFFF5C361)

@Composable
internal fun MosqueAreaButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    MosqueFilterPill(
        label = label,
        icon = R.drawable.ic_qibla_location,
        description = stringResource(R.string.mosque_choose_area),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun MosqueFilterPill(
    label: String,
    @DrawableRes icon: Int,
    description: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        color = colors.surface,
        contentColor = colors.onSurface,
        border = BorderStroke(0.75.dp, colors.outline.copy(alpha = 0.66f)),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 34.dp).padding(horizontal = 11.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            Icon(painterResource(icon), description, Modifier.size(17.dp))
            Text(
                label,
                Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(16.dp))
        }
    }
}

@Composable
internal fun MosqueSortButton(byName: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val distance = stringResource(R.string.mosque_sort_distance)
    val name = stringResource(R.string.mosque_sort_name)
    val colors = MaterialTheme.colorScheme
    Box(modifier) {
        MosqueFilterPill(
            label = if (byName) name else distance,
            icon = R.drawable.ic_mosque_sort,
            description = null,
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.testTag("mosque-sort-menu"),
            shape = RoundedCornerShape(16.dp),
            containerColor = colors.surfaceContainer,
            shadowElevation = 4.dp,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.36f)),
        ) {
            listOf(false to distance, true to name).forEach { (option, label) ->
                val active = byName == option
                DropdownMenuItem(
                    text = { Text(label, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal) },
                    leadingIcon = {
                        if (option) Icon(Icons.AutoMirrored.Filled.List, null, Modifier.size(19.dp))
                        else Icon(painterResource(R.drawable.ic_mosque_sort), null, Modifier.size(19.dp))
                    },
                    trailingIcon = {
                        if (active) Icon(Icons.Filled.Check, null, Modifier.size(19.dp), tint = colors.tertiary)
                    },
                    modifier = Modifier.padding(horizontal = 5.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) MosqueActionColor.copy(alpha = 0.20f) else Color.Transparent)
                        .semantics { selected = active },
                    onClick = {
                        expanded = false
                        onChange(option)
                    },
                )
            }
        }
    }
}

@Composable
internal fun MosqueLocationBar(
    title: String,
    subtitle: String?,
    isGps: Boolean,
    onSelect: () -> Unit,
    selectLabel: String,
    onLocate: (() -> Unit)?,
    locating: Boolean,
    showChevron: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerLow,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f).heightIn(min = 50.dp)
                    .clickable(role = Role.Button, onClickLabel = selectLabel, onClick = onSelect)
                    .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(shape = CircleShape, color = colors.surfaceContainerHigh) {
                    Icon(
                        painterResource(R.drawable.ic_mosque_pin),
                        contentDescription = selectLabel,
                        modifier = Modifier.padding(6.dp).size(19.dp),
                        tint = if (isGps) Color(0xFF258BE5) else colors.onSurface,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
                if (showChevron) Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(18.dp))
            }
            if (onLocate != null) {
                IconButton(onClick = onLocate, enabled = !locating) {
                    val description = stringResource(R.string.current_location)
                    if (isGps) Icon(Icons.Filled.Refresh, description, Modifier.size(22.dp))
                    else Icon(painterResource(R.drawable.ic_mosque_recenter), description, Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
internal fun MosqueViewSwitcher(showMap: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val track = if (colors.surface.luminance() < 0.5f) colors.surfaceContainer else colors.surface
    Surface(
        modifier = modifier.widthIn(max = 264.dp).fillMaxWidth(),
        shape = CircleShape,
        color = track,
        border = BorderStroke(0.75.dp, colors.outline.copy(alpha = 0.62f)),
        shadowElevation = 1.dp,
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(3.dp)) {
            val tabWidth = maxWidth / 2
            val selectedOffset by animateDpAsState(
                targetValue = if (showMap) tabWidth else 0.dp,
                animationSpec = tween(180),
                label = "Mosque view selection",
            )
            Box(Modifier.matchParentSize()) {
                Box(
                    Modifier.offset(x = selectedOffset).width(tabWidth).fillMaxHeight()
                        .background(MosqueActionColor, CircleShape),
                )
            }
            Row(Modifier.fillMaxWidth().selectableGroup()) {
                MosqueViewTab(R.string.mosque_list, !showMap, { onChange(false) }, Modifier.weight(1f))
                MosqueViewTab(R.string.mosque_map, showMap, { onChange(true) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MosqueViewTab(label: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier.heightIn(min = 40.dp).clip(CircleShape)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) colors.onSecondaryContainer else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
