package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.AppThemeMode
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale

@Composable
internal fun MenuScreen(
    theme: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    topPadding: Dp,
    bottomPadding: Dp,
) {
    var showLanguage by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showPrivacy by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = topPadding + 12.dp, bottom = bottomPadding + 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = RoundedCornerShape(22.dp),
        ) {
            Column(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    stringResource(R.string.theme),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(
                    Modifier.fillMaxWidth().selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        ThemeOption(mode, theme == mode, Modifier.weight(1f)) { onThemeChange(mode) }
                    }
                }
            }
        }
        MenuRow(
            title = stringResource(R.string.language),
            value = language.label,
            onClick = { showLanguage = true },
        )
        MenuRow(
            title = stringResource(R.string.about_app),
            icon = Icons.Default.Info,
            onClick = { showAbout = true },
        )
    }

    if (showLanguage) {
        AlertDialog(
            onDismissRequest = { showLanguage = false },
            title = { Text(stringResource(R.string.language)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                    AppLanguage.entries.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).selectable(
                                selected = option == language,
                                role = Role.RadioButton,
                                onClick = {
                                    showLanguage = false
                                    onLanguageChange(option)
                                },
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            RadioButton(selected = option == language, onClick = null)
                            Text(option.label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguage = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }
    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            title = { Text(stringResource(R.string.privacy_policy)) },
            text = {
                Text(
                    text = stringResource(R.string.privacy_policy_summary),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacy = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }
    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text(stringResource(R.string.app_name)) },
            text = { Text(stringResource(R.string.about_app_description)) },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }
}

@Composable
private fun MenuRow(title: String, value: String? = null, icon: ImageVector? = null, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier.fillMaxWidth().pressScale(rememberPressScale(interaction)),
        shape = RoundedCornerShape(18.dp),
        color = colors.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.heightIn(min = 54.dp).padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (icon == null) {
                Icon(painterResource(R.drawable.ic_menu_language), null, Modifier.size(20.dp), tint = colors.tertiary)
            } else {
                Icon(icon, null, Modifier.size(20.dp), tint = colors.tertiary)
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (value != null) {
                    Text(value, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(20.dp), tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun ThemeOption(mode: AppThemeMode, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(12.dp)
    val fill by animateColorAsState(
        if (selected) colors.secondaryContainer else colors.surfaceContainer,
        label = "Theme selection",
    )
    val outline by animateColorAsState(
        if (selected) colors.tertiary else colors.outlineVariant,
        label = "Theme outline",
    )
    Surface(
        modifier = modifier.clip(shape)
            .selectable(selected, interaction, null, role = Role.RadioButton, onClick = onClick)
            .pressScale(rememberPressScale(interaction)),
        shape = shape,
        color = fill,
        border = BorderStroke(1.dp, outline),
    ) {
        Column(
            Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(Modifier.fillMaxWidth().height(48.dp)) {
                ThemePreview(mode, Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))
                if (selected) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(3.dp),
                        shape = CircleShape,
                        color = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer,
                    ) {
                        Icon(painterResource(R.drawable.ic_menu_check), null, Modifier.padding(2.dp).size(12.dp))
                    }
                }
            }
            Text(
                stringResource(mode.label),
                modifier = Modifier.padding(bottom = 3.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) colors.onSecondaryContainer else colors.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ThemePreview(mode: AppThemeMode, modifier: Modifier) {
    Canvas(modifier) {
        when (mode) {
            AppThemeMode.Light -> drawLandscape(night = false, celestialX = 0.54f)
            AppThemeMode.Dark -> drawLandscape(night = true, celestialX = 0.60f)
            AppThemeMode.System -> {
                drawLandscape(night = false, celestialX = 0.26f)
                val nightSide = Path().apply {
                    moveTo(size.width * 0.61f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height)
                    lineTo(size.width * 0.43f, size.height)
                    close()
                }
                clipPath(nightSide) { drawLandscape(night = true, celestialX = 0.73f) }
            }
        }
    }
}

private fun DrawScope.drawLandscape(night: Boolean, celestialX: Float) {
    val skyTop = if (night) Color(0xFF111D32) else Color(0xFFFCE8C8)
    val skyBottom = if (night) Color(0xFF2D4864) else Color(0xFFF8B56F)
    drawRect(Brush.verticalGradient(listOf(skyTop, skyBottom)))
    val center = Offset(size.width * celestialX, size.height * 0.37f)
    val radius = size.minDimension * 0.15f
    drawCircle(if (night) Color(0xFFFFE3B3) else Color(0xFFFFF3BD), radius, center)
    if (night) {
        drawCircle(
            Brush.verticalGradient(listOf(skyTop, skyBottom)),
            radius * 0.87f,
            center + Offset(radius * 0.47f, -radius * 0.30f),
        )
        drawCircle(Color(0xFFFFE3B3), 1.dp.toPx(), Offset(size.width * 0.89f, size.height * 0.55f))
        drawCircle(Color(0xFFFFE3B3), 0.65.dp.toPx(), Offset(size.width * 0.53f, size.height * 0.24f))
    }
    val hills = Path().apply {
        moveTo(0f, size.height * 0.78f)
        quadraticTo(size.width * 0.27f, size.height * 0.46f, size.width * 0.55f, size.height * 0.79f)
        quadraticTo(size.width * 0.83f, size.height * 0.92f, size.width, size.height * 0.66f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(hills, if (night) Color(0xFF14253C) else Color(0xFFC98054))
}
