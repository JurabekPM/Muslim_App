package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosquePhoto
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosquePhotos

@Composable
internal fun MosqueThumbnail(
    mosqueId: String,
    name: String,
    modifier: Modifier = Modifier,
    showPhotoCount: Boolean = false,
    onOpen: (() -> Unit)? = null,
) {
    val photos = MosquePhotos[mosqueId]
    val cover = photos.firstOrNull()
    val thumbnail = modifier.size(60.dp).clip(RoundedCornerShape(14.dp))
    val colors = MaterialTheme.colorScheme
    if (cover == null) {
        Box(
            modifier = thumbnail.background(lerp(colors.surfaceContainerHigh, colors.tertiaryContainer, 0.28f))
                .testTag("mosque-photo-placeholder"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_mosque),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = colors.tertiary,
            )
        }
        return
    }

    var showPhotos by rememberSaveable(mosqueId) { mutableStateOf(false) }
    Box(
        modifier = thumbnail.testTag("mosque-photo").clickable(
            role = Role.Button,
            onClickLabel = stringResource(R.string.mosque_photos_open),
            onClick = onOpen ?: { showPhotos = true },
        ),
    ) {
        Image(
            painterResource(cover.thumbnail),
            contentDescription = stringResource(R.string.mosque_photo_description, name),
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        if (photos.size > 1) {
            Row(
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(colors.tertiaryContainer, CircleShape)
                    .padding(horizontal = 5.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_mosque_gallery), null,
                    Modifier.size(if (showPhotoCount) 14.dp else 12.dp),
                    tint = colors.onTertiaryContainer,
                )
                if (showPhotoCount) {
                    Text(
                        stringResource(R.string.mosque_photo_count, photos.size),
                        color = colors.onTertiaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
    if (showPhotos) MosquePhotoViewer(name, photos, onClose = { showPhotos = false })
}

@Composable
internal fun MosquePhotoViewer(name: String, photos: List<MosquePhoto>, onClose: () -> Unit) {
    val pager = rememberPagerState { photos.size }
    val scope = rememberCoroutineScope()
    val current = pager.currentPage
    val photo = photos[current]
    var showSource by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        val view = LocalView.current
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window
            if (window != null) {
                val lightSurface = colors.surface.luminance() > 0.5f
                val insets = WindowCompat.getInsetsController(window, view)
                insets.isAppearanceLightStatusBars = lightSurface
                insets.isAppearanceLightNavigationBars = lightSurface
            }
        }
        Surface(Modifier.fillMaxSize(), color = colors.surface, contentColor = colors.onSurface) {
            Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
                Column(
                    Modifier.widthIn(max = 640.dp).fillMaxSize().padding(16.dp)
                        .testTag("mosque-photo-viewer"),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MosqueOverlayHeader(name, onClose)
                    HorizontalPager(
                        state = pager,
                        modifier = Modifier.weight(1f).fillMaxWidth().testTag("mosque-photo-pager"),
                        userScrollEnabled = photos.size > 1,
                    ) { page ->
                        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            val painter = painterResource(photos[page].thumbnail)
                            val ratio = painter.intrinsicSize.width / painter.intrinsicSize.height
                            Image(
                                painter,
                                contentDescription = stringResource(R.string.mosque_photo_description, name),
                                modifier = Modifier.width(minOf(maxWidth, maxHeight * ratio))
                                    .aspectRatio(ratio)
                                    .clip(RoundedCornerShape(18.dp)),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                    if (photos.size > 1) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            GalleryArrow(
                                previous = true,
                                enabled = current > 0,
                                onClick = { scope.launch { pager.animateScrollToPage(current - 1) } },
                            )
                            Text(
                                stringResource(R.string.mosque_photo_position, current + 1, photos.size),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            GalleryArrow(
                                previous = false,
                                enabled = current < photos.lastIndex,
                                onClick = { scope.launch { pager.animateScrollToPage(current + 1) } },
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceContainerLow,
                        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.36f)),
                    ) {
                        Column {
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 50.dp)
                                    .clickable(role = Role.Button) { showSource = !showSource }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(painterResource(R.drawable.ic_mosque_gallery), null, Modifier.size(20.dp))
                                Text(
                                    stringResource(R.string.mosque_photo_source),
                                    Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Icon(
                                    if (showSource) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    null, Modifier.size(22.dp),
                                )
                            }
                            AnimatedVisibility(showSource) {
                                Column(
                                    Modifier.fillMaxWidth().heightIn(max = 136.dp)
                                        .verticalScroll(rememberScrollState())
                                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.mosque_photo_credit, photo.author),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(photo.license.title, style = MaterialTheme.typography.labelMedium)
                                    SelectionContainer {
                                        Text(
                                            "${photo.sourceUrl}\n${photo.license.url}",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryArrow(previous: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = if (enabled) 0.62f else 0.22f)),
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(
                if (previous) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                stringResource(if (previous) R.string.mosque_photo_previous else R.string.mosque_photo_next),
            )
        }
    }
}
