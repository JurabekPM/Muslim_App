package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R

@Composable
internal fun HomeShortcutCards(
    onNearbyMosques: () -> Unit,
    onQibla: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val darkTheme = colors.background.luminance() < 0.35f
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeShortcutCard(
            title = R.string.nearby_mosques,
            subtitle = R.string.home_mosques_subtitle,
            illustration = R.drawable.home_shortcut_mosque,
            illustrationAlignment = Alignment.BottomEnd,
            background = if (darkTheme) {
                Brush.horizontalGradient(listOf(Color(0xFF614521), colors.surfaceContainer))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFF6E4C4), Color(0xFFFFF4E0)))
            },
            contentColor = if (darkTheme) colors.onSurface else colors.onTertiaryContainer,
            onClick = onNearbyMosques,
        )
        HomeShortcutCard(
            title = R.string.qibla,
            subtitle = R.string.home_qibla_subtitle,
            illustration = R.drawable.home_shortcut_qibla,
            illustrationAlignment = Alignment.CenterEnd,
            background = if (darkTheme) {
                Brush.horizontalGradient(listOf(Color(0xFF2B4657), colors.surfaceContainerLow))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFDFE5EB), Color(0xFFF0F2F2)))
            },
            contentColor = colors.onSurface,
            onClick = onQibla,
        )
    }
}

@Composable
private fun HomeShortcutCard(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    @DrawableRes illustration: Int,
    illustrationAlignment: Alignment,
    background: Brush,
    contentColor: Color,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth()
            .pressScale(rememberPressScale(interaction))
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 6.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(title),
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor,
                )
            }
            Text(
                text = stringResource(subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.88f),
            )
        }
        Image(
            painter = painterResource(illustration),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.39f).height(96.dp).padding(top = 4.dp, bottom = 4.dp),
            contentScale = ContentScale.Fit,
            alignment = illustrationAlignment,
        )
    }
}
