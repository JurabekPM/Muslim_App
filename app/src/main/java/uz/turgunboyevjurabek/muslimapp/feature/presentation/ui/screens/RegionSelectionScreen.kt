package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import android.content.pm.PackageManager
import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.feature.data.location.RegionLocationResult
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionHierarchy
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionKind
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.RegionSelection
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.SelectRegionViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.shimmers.ShimmerPlaceholder
import uz.turgunboyevjurabek.muslimapp.R

private const val gpsBadgeExitMillis = 180

@Composable
internal fun RegionSelectionScreen(
    selectRegionViewModel: SelectRegionViewModel,
    lookupLocation: suspend () -> RegionLocationResult,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val selection by selectRegionViewModel.selection.collectAsStateWithLifecycle()
    var outgoingSelection by remember { mutableStateOf<RegionSelection?>(null) }
    var groupSlug by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
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

    val group = groupSlug?.let(RegionHierarchy.groupsBySlug::get)
    val displayedSelection = outgoingSelection ?: selection
    val listState = rememberLazyListState()
    val matching = remember(query, group) {
        when {
            query.isNotBlank() -> Regions.search(query).let { results ->
                if (group == null) results else results.filter { it in group.children || it == group.region }
            }
            group != null -> listOf(group.region) + group.children
            else -> RegionHierarchy.groups.map { it.region }
        }
    }

    fun showGroup(slug: String?) {
        focusManager.clearFocus()
        keyboard?.hide()
        query = ""
        groupSlug = slug
        listState.requestScrollToItem(0)
    }

    fun goBack() {
        if (group != null) showGroup(null) else onClose()
    }

    BackHandler(enabled = group != null || saving) {
        if (!saving) goBack()
    }

    fun saveSelection(next: RegionSelection) {
        if (saving) return
        findingLocation = false
        saving = true
        message = null
        focusManager.clearFocus()
        keyboard?.hide()
        scope.launch {
            val previous = selection
            try {
                if (previous != next) {
                    val animateBadge = previous?.fromGps == true && !next.fromGps &&
                        listState.layoutInfo.visibleItemsInfo.any { it.key == "location" }
                    if (animateBadge) outgoingSelection = previous
                    selectRegionViewModel.saveRegion(next)
                    if (animateBadge) delay(gpsBadgeExitMillis.toLong())
                }
                onClose()
            } catch (_: IOException) {
                message = "Hududni saqlab bo‘lmadi. Qayta tanlang."
            } finally {
                outgoingSelection = null
                saving = false
            }
        }
    }

    LaunchedEffect(findingLocation) {
        if (!findingLocation) return@LaunchedEffect
        val result = lookupLocation()
        findingLocation = false
        when (result) {
            is RegionLocationResult.Found -> saveSelection(RegionSelection(result.region, fromGps = true))
            RegionLocationResult.LocationOff -> message = "Telefon sozlamalarida joylashuvni yoqing."
            RegionLocationResult.NoFix -> message = "Joylashuv aniqlanmadi. Qayta urinib ko‘ring."
            RegionLocationResult.AddressUnavailable -> message = "Manzil aniqlanmadi. Internetni tekshiring yoki hududni tanlang."
            RegionLocationResult.NoMatch -> message = "Mos shahar yoki tuman topilmadi. Hududni ro‘yxatdan tanlang."
        }
    }

    Column(
        Modifier.fillMaxSize().safeDrawingPadding().imePadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val interaction = remember { MutableInteractionSource() }
            IconButton(
                onClick = ::goBack,
                enabled = !saving,
                modifier = Modifier.pressScale(rememberPressScale(interaction)),
                interactionSource = interaction,
            ) {
                Surface(shape = RoundedCornerShape(13.dp), color = colors.surfaceContainerHigh) {
                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            painterResource(R.drawable.ic_qibla_back),
                            contentDescription = "Orqaga",
                            modifier = Modifier.size(21.dp),
                        )
                    }
                }
            }
            Text(
                "Hududni tanlash",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
        ) {
            item(key = "location") {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = colors.surfaceContainerHigh,
                    tonalElevation = 1.dp,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        displayedSelection?.let {
                            SelectedRegion(it.region, it.fromGps && outgoingSelection == null)
                        }
                        CurrentLocationButton(
                            finding = findingLocation || requestingPermission,
                            enabled = !saving && !findingLocation && !requestingPermission,
                        ) {
                            message = null
                            focusManager.clearFocus()
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
                        message?.let {
                            Text(
                                it,
                                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                                color = colors.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
            item(key = "search") {
                Box(Modifier.padding(top = 12.dp, bottom = 6.dp)) {
                    RegionSearch(query, onValueChange = { query = it }, onSearch = { keyboard?.hide() })
                }
            }
            item(key = "heading") {
                Crossfade(
                    targetState = group?.region?.nameRegion ?: "Asosiy hududlar",
                    animationSpec = tween(160),
                    label = "Region group title",
                ) { title ->
                    Text(
                        title,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            if (matching.isEmpty()) {
                item(key = "empty") {
                    Text(
                        "Hudud topilmadi",
                        modifier = Modifier.animateItem().fillMaxWidth().padding(vertical = 24.dp),
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            items(matching, key = { "${groupSlug.orEmpty()}/${it.slug}" }) { region ->
                val opensGroup = group == null && region.slug in RegionHierarchy.groupsBySlug
                val wholeGroup = group?.region == region
                val name = region.displayName()
                Column(Modifier.animateItem(fadeInSpec = tween(170), fadeOutSpec = tween(120))) {
                    RegionRow(
                        label = if (wholeGroup) "$name bo‘yicha" else name,
                        opensGroup = opensGroup,
                        selected = !opensGroup && region == selection?.region,
                        enabled = !saving,
                    ) {
                        if (opensGroup) showGroup(region.slug)
                        else saveSelection(RegionSelection(region, fromGps = false))
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 38.dp),
                        color = colors.outlineVariant.copy(alpha = 0.55f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedRegion(region: Regions, showGpsBadge: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_qibla_location), null, Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(
                region.displayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AnimatedVisibility(
                visible = showGpsBadge,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                exit = fadeOut(tween(gpsBadgeExitMillis)) +
                    shrinkVertically(tween(gpsBadgeExitMillis), shrinkTowards = Alignment.Top),
            ) {
                Surface(
                    modifier = Modifier.padding(top = 3.dp),
                    shape = CircleShape,
                    color = colors.surfaceContainerHighest,
                ) {
                    Text(
                        "Hozirgi joylashuv",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        Surface(shape = CircleShape, color = colors.secondaryContainer, contentColor = colors.onSecondaryContainer) {
            Box(Modifier.size(27.dp), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_qibla_check), null, Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun RegionRow(
    label: String,
    opensGroup: Boolean,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().pressScale(rememberPressScale(interaction)),
        interactionSource = interaction,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) colors.surfaceContainerLow else Color.Transparent,
        contentColor = colors.onSurface,
    ) {
        Row(
            modifier = Modifier.heightIn(min = 50.dp).padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ic_qibla_location), null, Modifier.size(18.dp), tint = colors.onSurfaceVariant)
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (opensGroup) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(20.dp), tint = colors.onSurfaceVariant)
            } else if (selected) {
                Icon(painterResource(R.drawable.ic_qibla_check), null, Modifier.size(18.dp), tint = colors.tertiary)
            }
        }
    }
}

private fun Regions.displayName(): String = when {
    kind == RegionKind.City && !nameRegion.endsWith(" shahri") -> "$nameRegion shahri"
    kind == RegionKind.District && !nameRegion.endsWith(" tumani") -> "$nameRegion tumani"
    else -> nameRegion
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
        modifier = Modifier.fillMaxWidth().pressScale(rememberPressScale(interaction)),
        shape = CircleShape,
        color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
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
