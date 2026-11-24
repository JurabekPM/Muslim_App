package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import uz.turgunboyevjurabek.muslimapp.R

private val dockButtonSize = 48.dp
private val dockGap = 8.dp

@Composable
internal fun MosqueSearchDock(
    glass: MosqueGlass,
    showMap: Boolean,
    searching: Boolean,
    searchAvailable: Boolean,
    searchText: TextFieldState,
    requestFocus: Boolean,
    onFocusRequested: () -> Unit,
    onSearchOpen: () -> Unit,
    onSearchClose: () -> Unit,
    onViewChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        if (searching) 1f else 0f, tween(320, easing = FastOutSlowInEasing),
        label = "Mosque search expansion",
    )
    val toggleInteraction = remember { MutableInteractionSource() }
    val searchInteraction = remember { MutableInteractionSource() }
    val toggleScale = rememberPressScale(toggleInteraction, bounce = true)
    val searchScale = rememberPressScale(searchInteraction, bounce = true)
    BoxWithConstraints(modifier.widthIn(max = 460.dp).fillMaxWidth()) {
        val expandedWidth = maxWidth - dockGap - dockButtonSize
        val travel = (expandedWidth - dockButtonSize).coerceAtLeast(0.dp) * progress
        val canShowTabs = !searching
        Row(horizontalArrangement = Arrangement.spacedBy(dockGap)) {
            MosqueGlassPill(
                glass,
                Modifier.size(expandedWidth - travel, dockButtonSize).mosqueLiquidPress(toggleScale),
                interactionSource = toggleInteraction,
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = canShowTabs,
                    modifier = Modifier.matchParentSize(),
                    enter = fadeIn(tween(160, delayMillis = 115)),
                    exit = fadeOut(tween(130)),
                ) {
                    MosqueViewTabs(showMap, enabled = canShowTabs, onViewChange)
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = searching,
                    modifier = Modifier.matchParentSize(),
                    enter = fadeIn(tween(140, delayMillis = 145)),
                    exit = fadeOut(tween(95)),
                ) {
                    val actionLabel = stringResource(if (showMap) R.string.mosque_list else R.string.mosque_map)
                    Box(
                        Modifier.fillMaxSize().padding(2.dp).clip(CircleShape)
                            .background(MosqueActionColor.copy(alpha = 0.24f))
                            .testTag("mosque-search-view-toggle")
                            .clickable(
                                interactionSource = toggleInteraction, indication = null,
                                enabled = searching, role = Role.Button, onClick = { onViewChange(!showMap) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        val tint = MaterialTheme.colorScheme.tertiary
                        if (showMap) Icon(Icons.AutoMirrored.Filled.List, actionLabel, Modifier.size(21.dp), tint)
                        else Icon(painterResource(R.drawable.ic_mosque_map), actionLabel, Modifier.size(21.dp), tint)
                    }
                }
            }
            MosqueGlassPill(
                glass,
                Modifier.size(dockButtonSize + travel, dockButtonSize).mosqueLiquidPress(searchScale),
                interactionSource = searchInteraction,
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = canShowTabs,
                    modifier = Modifier.matchParentSize(),
                    enter = fadeIn(tween(140, delayMillis = 125)),
                    exit = fadeOut(tween(100)),
                ) {
                    Box(
                        Modifier.fillMaxSize().clickable(
                            interactionSource = searchInteraction, indication = null,
                            enabled = canShowTabs && searchAvailable, role = Role.Button, onClick = onSearchOpen,
                        ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Search, stringResource(R.string.mosque_search), Modifier.size(22.dp))
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = searching,
                    modifier = Modifier.matchParentSize(),
                    enter = fadeIn(tween(150, delayMillis = 155)),
                    exit = fadeOut(tween(90)),
                ) {
                    MosqueSearchInput(
                        searchText, searching, requestFocus && progress == 1f,
                        onFocusRequested, onSearchClose, searchInteraction,
                    )
                }
            }
        }
    }
}

@Composable
private fun MosqueViewTabs(showMap: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val listInteraction = remember { MutableInteractionSource() }
    val mapInteraction = remember { MutableInteractionSource() }
    val listScale = rememberPressScale(listInteraction, bounce = true)
    val mapScale = rememberPressScale(mapInteraction, bounce = true)
    val lens = remember {
        Brush.verticalGradient(
            0f to Color(0xFFFFE8AF), 0.35f to MosqueActionColor, 1f to Color(0xFFE3A849),
        )
    }
    BoxWithConstraints(Modifier.fillMaxSize().padding(3.dp)) {
        val tabWidth = maxWidth / 2
        val offset by animateDpAsState(
            if (showMap) tabWidth else 0.dp, spring(dampingRatio = 0.76f, stiffness = 450f),
            label = "Mosque view selection",
        )
        Box(
            Modifier.offset { IntOffset(offset.roundToPx(), 0) }.width(tabWidth).fillMaxHeight()
                .mosqueLiquidPress(if (showMap) mapScale else listScale)
                .graphicsLayer {
                    val travel = if (tabWidth > 0.dp) (offset / tabWidth).coerceIn(0f, 1f) else 0f
                    val stretch = 4f * travel * (1f - travel)
                    scaleX = 1f + stretch * 0.13f
                    scaleY = 1f - stretch * 0.13f
                }.background(lens, CircleShape),
        )
        Row(Modifier.fillMaxSize().selectableGroup()) {
            listOf(false, true).forEach { map ->
                val selected = showMap == map
                Box(
                    Modifier.weight(1f).fillMaxHeight().clip(CircleShape)
                        .selectable(
                            selected = selected, enabled = enabled, role = Role.Tab,
                            interactionSource = if (map) mapInteraction else listInteraction, indication = null,
                            onClick = { onChange(map) },
                        ).mosqueLiquidPress(if (map) mapScale else listScale),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(if (map) R.string.mosque_map else R.string.mosque_list),
                        color = if (selected) colors.onSecondaryContainer else colors.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun MosqueSearchInput(
    searchText: TextFieldState,
    enabled: Boolean,
    requestFocus: Boolean,
    onFocusRequested: () -> Unit,
    onClose: () -> Unit,
    interactionSource: MutableInteractionSource,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val label = stringResource(R.string.mosque_search)
    val colors = MaterialTheme.colorScheme
    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            keyboard?.show()
            onFocusRequested()
        }
    }
    Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Search, null,
            Modifier.padding(start = 12.dp, end = 8.dp).size(19.dp),
            tint = colors.onSurfaceVariant,
        )
        BasicTextField(
            state = searchText,
            enabled = enabled,
            interactionSource = interactionSource,
            modifier = Modifier.weight(1f).focusRequester(focusRequester).testTag("mosque-inline-search")
                .semantics { contentDescription = label },
            lineLimits = TextFieldLineLimits.SingleLine,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.tertiary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            onKeyboardAction = {
                focusManager.clearFocus()
                keyboard?.hide()
            },
            decorator = { innerTextField ->
                Box {
                    if (searchText.text.isEmpty()) Text(
                        label, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    innerTextField()
                }
            },
        )
        Box(
            Modifier.size(44.dp).clip(CircleShape).testTag("mosque-close-search")
                .clickable(
                    interactionSource = interactionSource, indication = null, enabled = enabled,
                    role = Role.Button, onClick = onClose,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Close, stringResource(R.string.close), Modifier.size(19.dp))
        }
    }
}
