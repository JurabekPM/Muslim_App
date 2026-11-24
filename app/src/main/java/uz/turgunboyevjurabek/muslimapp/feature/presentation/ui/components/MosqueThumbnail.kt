package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosquePhoto
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosquePhotos

@Composable
internal fun MosqueThumbnail(mosqueId: String, name: String, modifier: Modifier = Modifier) {
    val photo = MosquePhotos[mosqueId]
    val thumbnail = modifier.size(60.dp).clip(RoundedCornerShape(14.dp))
    if (photo == null) {
        val colors = MaterialTheme.colorScheme
        val galleryUrl = MosquePhotos.galleryUrl(mosqueId)
        val uriHandler = LocalUriHandler.current
        val galleryAction = if (galleryUrl != null) {
            Modifier.clickable(role = Role.Button, onClickLabel = stringResource(R.string.mosque_photo_source)) {
                uriHandler.openUri(galleryUrl)
            }
        } else {
            Modifier
        }
        Box(
            modifier = thumbnail.background(lerp(colors.surfaceContainerHigh, colors.tertiaryContainer, 0.28f))
                .testTag("mosque-photo-placeholder")
                .then(galleryAction),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(R.drawable.ic_mosque),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = colors.tertiary,
            )
            if (galleryUrl != null) {
                Icon(
                    painterResource(R.drawable.ic_mosque_gallery),
                    contentDescription = stringResource(R.string.mosque_photo_source),
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(20.dp)
                        .background(colors.tertiaryContainer, CircleShape)
                        .padding(4.dp),
                    tint = colors.onTertiaryContainer,
                )
            }
        }
    } else {
        var showCredit by remember(mosqueId) { mutableStateOf(false) }
        val photoSource = stringResource(R.string.mosque_photo_source)
        Image(
            painterResource(photo.thumbnail),
            contentDescription = stringResource(R.string.mosque_photo_description, name),
            modifier = thumbnail.testTag("mosque-photo").clickable(
                role = Role.Button,
                onClickLabel = photoSource,
                onClick = { showCredit = true },
            ),
            contentScale = ContentScale.Crop,
        )
        if (showCredit) MosquePhotoCredit(name, photo, onClose = { showCredit = false })
    }
}

@Composable
private fun MosquePhotoCredit(name: String, photo: MosquePhoto, onClose: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painterResource(photo.thumbnail),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 152.dp),
                    contentScale = ContentScale.Fit,
                )
                Text(stringResource(R.string.mosque_photo_credit, photo.author))
                TextButton(onClick = { uriHandler.openUri(photo.license.url) }) {
                    Text(photo.license.title)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { uriHandler.openUri(photo.sourceUrl) }) {
                Text(stringResource(R.string.mosque_photo_source))
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text(stringResource(R.string.close)) }
        },
    )
}
