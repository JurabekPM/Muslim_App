package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import android.icu.text.MeasureFormat
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosquePhotos
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosquePlace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MosqueSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(finish: (() -> Unit) -> Unit) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val sheet = rememberBottomSheetState(SheetValue.Hidden, setOf(SheetValue.Hidden, SheetValue.Expanded))
    val scope = rememberCoroutineScope()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheet,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.surfaceContainer,
        contentColor = colors.onSurface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content { action ->
                scope.launch {
                    sheet.hide()
                    if (!sheet.isVisible) action()
                }
            }
        }
    }
}

@Composable
internal fun MosqueOverlayHeader(title: String, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        MosqueCloseButton(onClose)
    }
}

@Composable
private fun MosqueCloseButton(onClose: () -> Unit) {
    PressIconButton(onClick = onClose) {
        Icon(Icons.Filled.Close, stringResource(R.string.close), Modifier.size(22.dp))
    }
}

@Composable
internal fun MosqueChoiceRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    @DrawableRes icon: Int = R.drawable.ic_qibla_location,
    outlined: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier.fillMaxWidth().clip(shape)
            .selectable(selected, role = Role.RadioButton, onClick = onSelect),
        shape = shape,
        color = if (selected) MosqueActionColor.copy(alpha = 0.20f) else colors.surfaceContainer,
        border = if (outlined) BorderStroke(
            1.dp,
            if (selected) colors.tertiary.copy(alpha = 0.75f) else colors.outline.copy(alpha = 0.36f),
        ) else null,
    ) {
        Row(
            Modifier.heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(painterResource(icon), null, Modifier.size(19.dp))
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            if (selected) Icon(Icons.Filled.Check, null, Modifier.size(20.dp), tint = colors.tertiary)
        }
    }
}

@Composable
internal fun MosqueDetailSheet(
    place: MosquePlace,
    distanceMeters: Double?,
    languageKey: String,
    format: MeasureFormat,
    onDirections: () -> Unit,
    onClose: () -> Unit,
    onShowOnMap: (() -> Unit)?,
) {
    val colors = MaterialTheme.colorScheme
    val name = mosqueDisplayName(place, languageKey)
    val distance = mosqueDistanceText(distanceMeters, format)
    val locality = place.locality?.let {
        if (place.localityApproximate) stringResource(R.string.mosque_near_locality, it) else it
    }
    val subtitle = listOfNotNull(distance, locality).joinToString(" · ")
    val address = place.address?.takeUnless { it == place.locality }
    val photos = MosquePhotos[place.id]
    var showPhotos by rememberSaveable(place.id) { mutableStateOf(false) }

    MosqueSheet(onDismiss = onClose, modifier = Modifier.testTag("mosque-detail-sheet")) { finish ->
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
                MosqueThumbnail(place.id, name, Modifier.size(104.dp), showPhotoCount = true, onOpen = { showPhotos = true })
                Column(Modifier.weight(1f).padding(top = 7.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    if (subtitle.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Place, null, Modifier.size(16.dp), tint = colors.onSurfaceVariant)
                            Text(
                                subtitle, Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant,
                            )
                        }
                    }
                    if (address != null) {
                        Text(address, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                }
                MosqueCloseButton { finish(onClose) }
            }
            if (photos.isNotEmpty()) {
                TextButton(onClick = { showPhotos = true }) {
                    Icon(painterResource(R.drawable.ic_mosque_gallery), null, Modifier.size(20.dp))
                    Text(stringResource(R.string.mosque_photos_open), Modifier.padding(horizontal = 9.dp))
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(18.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onShowOnMap != null) {
                    OutlinedButton(
                        onClick = { finish(onShowOnMap) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                    ) {
                        Icon(painterResource(R.drawable.ic_mosque_map), null, Modifier.size(18.dp))
                        Text(
                            stringResource(R.string.mosque_show_on_map),
                            Modifier.padding(start = 6.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Button(
                    onClick = onDirections,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MosqueActionColor,
                        contentColor = colors.onSecondaryContainer,
                    ),
                ) {
                    Icon(painterResource(R.drawable.ic_mosque_route), null, Modifier.size(18.dp))
                    Text(
                        stringResource(R.string.mosque_directions),
                        Modifier.padding(start = 6.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
    if (showPhotos && photos.isNotEmpty()) {
        MosquePhotoViewer(name, photos, onClose = { showPhotos = false })
    }
}

@Composable
internal fun MosqueDirectionsDialog(onClose: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = colors.surfaceContainer,
            contentColor = colors.onSurface,
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(52.dp).background(MosqueActionColor.copy(alpha = 0.20f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_mosque_route), null, Modifier.size(24.dp), tint = colors.tertiary)
                }
                Text(
                    stringResource(R.string.mosque_directions_unavailable),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.mosque_directions_error),
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MosqueActionColor,
                        contentColor = colors.onSecondaryContainer,
                    ),
                ) {
                    Text(stringResource(R.string.mosque_directions_acknowledge))
                }
            }
        }
    }
}
