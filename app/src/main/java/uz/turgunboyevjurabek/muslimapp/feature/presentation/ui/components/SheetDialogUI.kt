package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionKind
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.data.location.RegionLocationResult
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.ShimmerPlaceholder
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.RegionSelection
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.SelectRegionViewModel
import java.io.IOException

private const val gpsBadgeExitMillis = 180
private val regionFilters = listOf(
    null to "Barchasi",
    RegionKind.City to "Shaharlar",
    RegionKind.District to "Tumanlar",
    RegionKind.Province to "Viloyatlar",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetDialogUI(
    selectRegionViewModel: SelectRegionViewModel,
    lookupLocation: suspend () -> RegionLocationResult,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val selection by selectRegionViewModel.selection.collectAsStateWithLifecycle()
    var outgoingSelection by remember { mutableStateOf<RegionSelection?>(null) }
    val displayedSelection = outgoingSelection ?: selection
    val selectedRegion = displayedSelection?.region
    val showGpsBadge = displayedSelection?.fromGps == true && outgoingSelection == null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<RegionKind?>(null) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var findingLocation by remember { mutableStateOf(false) }
    var requestingPermission by rememberSaveable { mutableStateOf(false) }
    val requestLocationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        requestingPermission = false
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            findingLocation = true
        } else {
            message = "Aniq joylashuvga ruxsat bering yoki hududni ro‘yxatdan tanlang."
        }
    }
    val listState = rememberLazyListState()
    val matching = remember(query, filter, selectedRegion) {
        val results = Regions.search(query, filter)
        if (query.isBlank()) results.filterNot { it == selectedRegion } else results
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.surface,
        scrimColor = Color.Black.copy(alpha = 0.44f),
    ) {
        val keyboard = LocalSoftwareKeyboardController.current
        val dismissSheet: () -> Unit = {
            findingLocation = false
            keyboard?.hide()
            scope.launch {
                sheetState.hide()
                if (!sheetState.isVisible) onDismiss()
            }
        }
        val selectRegion: (RegionSelection) -> Unit = { next ->
            if (!saving) {
                findingLocation = false
                saving = true
                message = null
                keyboard?.hide()
                scope.launch {
                    val previous = selection
                    try {
                        if (previous != next && (previous?.region != next.region || next.fromGps)) {
                            val selectedKey = if (query.isBlank()) "current" else previous?.region?.slug
                            val animateBadge = previous?.fromGps == true && !next.fromGps &&
                                listState.layoutInfo.visibleItemsInfo.any { it.key == selectedKey }
                            if (animateBadge) outgoingSelection = previous
                            selectRegionViewModel.saveRegion(next)
                            if (animateBadge) delay(gpsBadgeExitMillis.toLong())
                        }
                        sheetState.hide()
                        if (!sheetState.isVisible) onDismiss()
                    } catch (_: IOException) {
                        message = "Hududni saqlab bo‘lmadi. Qayta tanlang."
                    } finally {
                        outgoingSelection = null
                        saving = false
                    }
                }
            }
        }

        LaunchedEffect(findingLocation) {
            if (!findingLocation) return@LaunchedEffect
            val result = lookupLocation()
            if (!findingLocation) return@LaunchedEffect
            findingLocation = false
            when (result) {
                is RegionLocationResult.Found -> selectRegion(RegionSelection(result.region, fromGps = true))
                RegionLocationResult.LocationOff -> message = "Telefon sozlamalarida joylashuvni yoqing."
                RegionLocationResult.NoFix -> message = "Joylashuv aniqlanmadi. Qayta urinib ko‘ring."
                RegionLocationResult.AddressUnavailable -> message = "Manzil aniqlanmadi. Internetni tekshiring yoki hududni tanlang."
                RegionLocationResult.NoMatch -> message = "Mos shahar yoki tuman topilmadi. Hududni ro‘yxatdan tanlang."
            }
        }

        val selectManually: (Regions) -> Unit = { selectRegion(RegionSelection(it, fromGps = false)) }
        Column(Modifier.fillMaxWidth().fillMaxHeight()) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Hududni tanlash",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    SheetIconButton("Yopish", dismissSheet)
                }
                RegionSearch(
                    value = query,
                    onValueChange = {
                        query = it
                        if (listState.firstVisibleItemIndex > 0) listState.requestScrollToItem(0)
                    },
                    onSearch = { keyboard?.hide() },
                )
                CurrentLocationButton(
                    finding = findingLocation || requestingPermission,
                    enabled = !saving && !findingLocation && !requestingPermission,
                ) {
                    message = null
                    keyboard?.hide()
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        findingLocation = true
                    } else {
                        requestingPermission = true
                        requestLocationPermission.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        )
                    }
                }
                message?.let { feedback ->
                    Text(
                        text = feedback,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        color = colors.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 12.dp),
            ) {
                if (query.isBlank()) {
                    selectedRegion?.let { current ->
                        item(key = "current") {
                            RegionRow(
                                current,
                                modifier = Modifier.animateItem(),
                                isSelected = true,
                                showGpsBadge = showGpsBadge,
                                enabled = !saving,
                                onClick = selectManually,
                            )
                        }
                    }
                }
                stickyHeader(key = "filters") {
                    RegionFilters(filter, enabled = !saving) {
                        filter = it
                        if (listState.firstVisibleItemIndex > 0) listState.requestScrollToItem(0)
                    }
                }
                if (matching.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = "Hudud topilmadi",
                            modifier = Modifier.animateItem().fillMaxWidth().padding(vertical = 24.dp),
                            color = colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                items(matching, key = Regions::slug) { region ->
                    Column(Modifier.animateItem(fadeInSpec = tween(170), fadeOutSpec = tween(120))) {
                        RegionRow(
                            region,
                            isSelected = region == selectedRegion,
                            showGpsBadge = showGpsBadge && region == selectedRegion,
                            enabled = !saving,
                            onClick = selectManually,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 38.dp),
                            color = colors.outlineVariant.copy(alpha = 0.55f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionSearch(value: String, onValueChange: (String) -> Unit, onSearch: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            .semantics { contentDescription = "Hudud qidirish" },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.tertiary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        decorationBox = { input ->
            Surface(
                shape = CircleShape,
                color = colors.surfaceContainerLow,
                border = BorderStroke(1.dp, if (focused) colors.tertiary else colors.outlineVariant),
            ) {
                Row(
                    modifier = Modifier.heightIn(min = 44.dp).padding(start = 14.dp, end = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = colors.onSurfaceVariant,
                    )
                    Box(Modifier.weight(1f).padding(vertical = 8.dp)) {
                        if (value.isEmpty()) {
                            Text("Hudud qidirish", color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                        input()
                    }
                    if (value.isNotEmpty()) {
                        IconButton(onClick = { onValueChange("") }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Qidiruvni tozalash", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun CurrentLocationButton(finding: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        modifier = Modifier.pressScale(rememberPressScale(interaction)).minimumInteractiveComponentSize(),
        shape = CircleShape,
        color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.heightIn(min = 34.dp).padding(horizontal = 12.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (finding) {
                ShimmerPlaceholder(
                    modifier = Modifier.size(16.dp),
                    cornerRadius = 8.dp,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_qibla_target),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = if (finding) "Joylashuv aniqlanmoqda…" else "Hozirgi joylashuvim",
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun RegionFilters(selected: RegionKind?, enabled: Boolean, onSelect: (RegionKind?) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(
        Modifier.fillMaxWidth().background(colors.surface).padding(vertical = 3.dp),
    ) {
        val cellWidth = maxWidth.coerceAtLeast(288.dp * fontScale) / regionFilters.size
        val position = animateDpAsState(
            targetValue = cellWidth * regionFilters.indexOfFirst { it.first == selected },
            animationSpec = spring(dampingRatio = 0.88f, stiffness = 480f),
            label = "Region filter position",
        )
        Box(Modifier.horizontalScroll(rememberScrollState()).width(cellWidth * regionFilters.size)) {
            Box(
                Modifier.align(Alignment.Center).fillMaxWidth().height(40.dp)
                    .background(colors.surfaceContainerLow, CircleShape),
            )
            Box(
                Modifier.align(Alignment.CenterStart).offset { IntOffset(position.value.roundToPx(), 0) }
                    .width(cellWidth).height(38.dp).padding(horizontal = 2.dp)
                    .background(colors.inverseSurface, CircleShape),
            )
            Row {
                regionFilters.forEach { (kind, label) ->
                    val isSelected = selected == kind
                    val interaction = remember { MutableInteractionSource() }
                    val textColor by animateColorAsState(
                        if (isSelected) colors.inverseOnSurface else colors.onSurfaceVariant,
                        animationSpec = tween(180),
                        label = "Region filter text",
                    )
                    Surface(
                        onClick = { onSelect(kind) },
                        enabled = enabled,
                        modifier = Modifier.width(cellWidth).heightIn(min = 48.dp)
                            .pressScale(rememberPressScale(interaction))
                            .semantics { this.selected = isSelected },
                        shape = CircleShape,
                        color = Color.Transparent,
                        interactionSource = interaction,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(label, color = textColor, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetIconButton(label: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        modifier = Modifier.pressScale(rememberPressScale(interaction)),
        interactionSource = interaction,
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Box(Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Close, contentDescription = label, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RegionRow(
    region: Regions,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    showGpsBadge: Boolean,
    enabled: Boolean,
    onClick: (Regions) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = { onClick(region) },
        enabled = enabled,
        interactionSource = interaction,
        modifier = modifier.fillMaxWidth()
            .pressScale(rememberPressScale(interaction))
            .semantics { selected = isSelected },
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) colors.surfaceContainerLow else Color.Transparent,
        contentColor = colors.onSurface,
        border = if (isSelected) BorderStroke(1.dp, colors.outlineVariant) else null,
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_qibla_location),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) colors.tertiary else colors.onSurfaceVariant,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = region.nameRegion,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                )
                AnimatedVisibility(
                    visible = showGpsBadge,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut(tween(gpsBadgeExitMillis)) +
                        shrinkVertically(tween(gpsBadgeExitMillis), shrinkTowards = Alignment.Top),
                ) {
                    Surface(
                        modifier = Modifier.padding(top = 2.dp),
                        shape = CircleShape,
                        color = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer,
                    ) {
                        Text(
                            text = "Hozirgi joylashuv",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_qibla_check),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = colors.tertiary,
                )
            }
        }
    }
}
