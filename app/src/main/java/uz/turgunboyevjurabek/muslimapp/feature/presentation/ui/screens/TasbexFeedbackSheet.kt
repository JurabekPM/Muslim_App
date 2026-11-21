package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.TasbihFeedbackOption

private val feedbackLabels = mapOf(
    TasbihFeedbackOption.TapSound to R.string.tasbih_tap_sound,
    TasbihFeedbackOption.TapVibration to R.string.tasbih_tap_vibration,
    TasbihFeedbackOption.CompletionSound to R.string.tasbih_completion_sound,
    TasbihFeedbackOption.CompletionVibration to R.string.tasbih_completion_vibration,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TasbexFeedbackSheet(
    feedback: Set<TasbihFeedbackOption>,
    onChange: (TasbihFeedbackOption, Boolean) -> Unit,
    onDismiss: () -> Unit,
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
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                stringResource(R.string.tasbih_feedback_settings),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.titleLarge,
            )
            feedbackLabels.forEach { (option, label) ->
                val checked = option in feedback
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .toggleable(checked, role = Role.Switch, onValueChange = { onChange(option, it) })
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(label), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = checked, onCheckedChange = null)
                }
            }
            TasbexDialogButton(
                label = stringResource(R.string.close),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                onClick = {
                    scope.launch {
                        sheet.hide()
                        if (!sheet.isVisible) onDismiss()
                    }
                },
            )
        }
    }
}
