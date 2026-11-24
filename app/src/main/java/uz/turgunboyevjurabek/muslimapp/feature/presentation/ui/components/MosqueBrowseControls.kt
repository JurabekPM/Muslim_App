package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R

internal val MosqueActionColor = Color(0xFFF5C361)

@Composable
internal fun MosqueBrowseFilters(
    label: String,
    showSort: Boolean,
    sortByName: Boolean,
    canSortByDistance: Boolean,
    onAreaClick: () -> Unit,
    onSortChange: (Boolean) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = CircleShape, color = colors.surfaceContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showSort) {
                MosqueSortButton(sortByName, canSortByDistance, onSortChange)
            }
            MosqueAreaButton(
                label = label,
                onClick = onAreaClick,
                modifier = Modifier.weight(1f),
                startPadding = if (showSort) 2.dp else 6.dp,
            )
        }
    }
}

@Composable
private fun MosqueAreaButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    startPadding: Dp,
) {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(R.string.mosque_choose_area)
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = description },
        shape = CircleShape,
        color = Color.Transparent,
    ) {
        Surface(
            modifier = Modifier.padding(start = startPadding, end = 6.dp, top = 6.dp, bottom = 6.dp),
            shape = CircleShape,
            color = colors.surface.copy(alpha = 0.78f),
            contentColor = colors.onSurface,
            border = BorderStroke(1.dp, colors.outlineVariant),
        ) {
            Row(
                Modifier.heightIn(min = 36.dp).padding(start = 12.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    label,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun MosqueSortButton(byName: Boolean, canSortByDistance: Boolean, onChange: (Boolean) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.mosque_sort)
    val distance = stringResource(R.string.mosque_sort_distance)
    val name = stringResource(R.string.mosque_sort_name)
    val colors = MaterialTheme.colorScheme
    Box(Modifier.width(52.dp)) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).semantics {
                contentDescription = description
                stateDescription = if (byName) name else distance
            },
            shape = CircleShape,
            color = Color.Transparent,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = colors.surface,
                    contentColor = colors.onSurface,
                    shadowElevation = 1.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_mosque_sort), null, Modifier.size(20.dp))
                    }
                }
            }
        }
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
                val enabled = option || canSortByDistance
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(label, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                            if (!enabled) {
                                Text(
                                    stringResource(R.string.mosque_sort_location_needed),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    },
                    enabled = enabled,
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
