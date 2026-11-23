package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R

@Composable
internal fun MosqueAreaButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(label, Modifier.weight(1f, fill = false), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Icon(
            Icons.Filled.KeyboardArrowDown,
            stringResource(R.string.mosque_choose_area),
            Modifier.size(18.dp),
        )
    }
}

@Composable
internal fun MosqueSortButton(byName: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val distance = stringResource(R.string.mosque_sort_distance)
    val name = stringResource(R.string.mosque_sort_name)
    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                if (byName) name else distance,
                Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(18.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(distance) },
                onClick = {
                    expanded = false
                    onChange(false)
                },
            )
            DropdownMenuItem(
                text = { Text(name) },
                onClick = {
                    expanded = false
                    onChange(true)
                },
            )
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
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainerLow,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f).heightIn(min = 52.dp)
                    .clickable(role = Role.Button, onClickLabel = selectLabel, onClick = onSelect)
                    .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(shape = CircleShape, color = colors.surfaceContainerHigh) {
                    Icon(
                        painterResource(R.drawable.ic_qibla_location),
                        contentDescription = selectLabel,
                        modifier = Modifier.padding(6.dp).size(19.dp),
                        tint = if (isGps) Color(0xFF258BE5) else colors.onSurface,
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (subtitle != null) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
                Icon(Icons.Filled.KeyboardArrowDown, null, Modifier.size(18.dp))
            }
            if (onLocate != null) {
                IconButton(onClick = onLocate, enabled = !locating) {
                    val description = stringResource(R.string.current_location)
                    if (isGps) Icon(Icons.Filled.Refresh, description)
                    else Icon(painterResource(R.drawable.ic_mosque_recenter), description)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MosqueViewSwitcher(showMap: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.widthIn(max = 256.dp).fillMaxWidth(),
        shape = CircleShape,
        color = colors.surfaceContainer.copy(alpha = 0.97f),
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 3.dp,
    ) {
        val buttonColors = SegmentedButtonDefaults.colors(
            activeContainerColor = colors.secondaryContainer,
            activeContentColor = colors.onSecondaryContainer,
            activeBorderColor = Color.Transparent,
            inactiveContainerColor = Color.Transparent,
            inactiveContentColor = colors.onSurface,
            inactiveBorderColor = Color.Transparent,
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            SegmentedButton(
                selected = !showMap,
                onClick = { onChange(false) },
                shape = CircleShape,
                colors = buttonColors,
                icon = {},
            ) { Text(stringResource(R.string.mosque_list), fontWeight = FontWeight.SemiBold) }
            SegmentedButton(
                selected = showMap,
                onClick = { onChange(true) },
                shape = CircleShape,
                colors = buttonColors,
                icon = {},
            ) { Text(stringResource(R.string.mosque_map), fontWeight = FontWeight.SemiBold) }
        }
    }
}
