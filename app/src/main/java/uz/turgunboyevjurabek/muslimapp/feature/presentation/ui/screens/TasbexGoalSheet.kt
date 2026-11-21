package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.pressScale
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.rememberPressScale

private val goalPresets = listOf(33, 99)

@Composable
internal fun TasbexGoalPicker(target: Int?, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.pressScale(rememberPressScale(interaction)),
        interactionSource = interaction,
        border = BorderStroke(1.dp, colors.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.onSurface),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(goalLabel(target), style = MaterialTheme.typography.labelLarge)
            Icon(painterResource(R.drawable.ic_chevron_down), null, Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TasbexGoalSheet(target: Int?, onDismiss: () -> Unit, onSelect: (Int?) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val sheet = rememberBottomSheetState(SheetValue.Hidden, setOf(SheetValue.Hidden, SheetValue.Expanded))
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val isCustom = target != null && target !in goalPresets
    var showCustom by rememberSaveable { mutableStateOf(isCustom) }
    var custom by rememberSaveable { mutableStateOf(if (isCustom) target.toString() else "") }
    val customTarget = custom.toIntOrNull()?.takeIf { it > 0 }

    fun select(value: Int?) {
        focusManager.clearFocus()
        onSelect(value)
        scope.launch {
            sheet.hide()
            if (!sheet.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheet,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = colors.surfaceContainer,
        contentColor = colors.onSurface,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                stringResource(R.string.tasbih_count_mode),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                goalPresets.forEach { value ->
                    GoalOption(goalLabel(value), selected = target == value, onClick = { select(value) })
                }
                GoalOption(goalLabel(null), selected = target == null, onClick = { select(null) })
                GoalOption(
                    label = stringResource(R.string.tasbih_custom_count),
                    selected = isCustom,
                    expands = true,
                    expanded = showCustom,
                    onClick = { showCustom = !showCustom },
                )
            }
            AnimatedVisibility(visible = showCustom) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = custom,
                        onValueChange = { input ->
                            if (input.length <= Int.MAX_VALUE.toString().length && input.all(Char::isDigit)) {
                                custom = input
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.tasbih_enter_count)) },
                        shape = RoundedCornerShape(14.dp),
                        isError = custom.isNotEmpty() && customTarget == null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { customTarget?.let(::select) }),
                    )
                    TasbexDialogButton(
                        label = stringResource(R.string.tasbih_save_count),
                        enabled = customTarget != null,
                        onClick = { customTarget?.let(::select) },
                    )
                }
            }
        }
    }
}

@Composable
private fun goalLabel(target: Int?): String = if (target == null) {
    stringResource(R.string.tasbih_free_count)
} else {
    pluralStringResource(R.plurals.tasbih_count_times, target, target)
}

@Composable
private fun GoalOption(
    label: String,
    selected: Boolean,
    expands: Boolean = false,
    expanded: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            .pressScale(rememberPressScale(interaction))
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colors.tertiaryContainer else colors.surfaceContainer)
            .selectable(
                selected = selected,
                indication = null,
                interactionSource = interaction,
                role = if (expands) Role.Button else Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val content = if (selected) colors.onTertiaryContainer else colors.onSurface
        Text(label, Modifier.weight(1f), color = content, style = MaterialTheme.typography.bodyLarge)
        if (selected) {
            Icon(painterResource(R.drawable.ic_menu_check), null, Modifier.size(18.dp), tint = content)
        }
        if (expands) {
            Icon(
                painterResource(R.drawable.ic_chevron_down),
                null,
                Modifier.size(18.dp).rotate(if (expanded) 180f else 0f),
                tint = content,
            )
        }
    }
}
