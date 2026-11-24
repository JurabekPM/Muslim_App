package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosquePhoto
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosquePhotos

@Composable
internal fun MosqueThumbnail(mosqueId: String, name: String, modifier: Modifier = Modifier) {
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
            onClick = { showPhotos = true },
        ),
    ) {
        Image(
            painterResource(cover.thumbnail),
            contentDescription = stringResource(R.string.mosque_photo_description, name),
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        if (photos.size > 1) {
            Icon(
                painterResource(R.drawable.ic_mosque_gallery),
                contentDescription = null,
                modifier = Modifier.align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .background(colors.tertiaryContainer, CircleShape)
                    .padding(4.dp),
                tint = colors.onTertiaryContainer,
            )
        }
    }
    if (showPhotos) MosquePhotoViewer(name, photos, onClose = { showPhotos = false })
}

@Composable
private fun MosquePhotoViewer(name: String, photos: List<MosquePhoto>, onClose: () -> Unit) {
    val pager = rememberPagerState { photos.size }
    val scope = rememberCoroutineScope()
    val current = pager.currentPage
    val photo = photos[current]
    var showSource by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(name) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .testTag("mosque-photo-pager"),
                    userScrollEnabled = photos.size > 1,
                ) { page ->
                    Image(
                        painterResource(photos[page].thumbnail),
                        contentDescription = stringResource(R.string.mosque_photo_description, name),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
                if (photos.size > 1) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { scope.launch { pager.animateScrollToPage(current - 1) } },
                            enabled = current > 0,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.mosque_photo_previous))
                        }
                        Text(stringResource(R.string.mosque_photo_position, current + 1, photos.size))
                        IconButton(
                            onClick = { scope.launch { pager.animateScrollToPage(current + 1) } },
                            enabled = current < photos.lastIndex,
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.mosque_photo_next))
                        }
                    }
                }
                Text(stringResource(R.string.mosque_photo_credit, photo.author))
                Text(photo.license.title, style = MaterialTheme.typography.labelMedium)
                if (showSource) {
                    SelectionContainer {
                        Text(
                            "${photo.sourceUrl}\n${photo.license.url}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text(stringResource(R.string.close)) }
        },
        dismissButton = {
            TextButton(onClick = { showSource = !showSource }) { Text(stringResource(R.string.mosque_photo_source)) }
        },
    )
}
