package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import uz.turgunboyevjurabek.muslimapp.R
import androidx.compose.ui.res.stringResource
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun PrayerScheduleRows(
    bomdod: String?,
    quyosh: String?,
    peshin: String?,
    asr: String?,
    shom: String?,
    xufton: String?,
    rowHeight: Dp = 47.dp,
) {
    val rows = listOf(
        stringResource(R.string.fajr) to bomdod,
        stringResource(R.string.sunrise) to quyosh,
        stringResource(R.string.dhuhr) to peshin,
        stringResource(R.string.asr) to asr,
        stringResource(R.string.maghrib) to shom,
        stringResource(R.string.isha) to xufton,
    )
    val colors = MaterialTheme.colorScheme
    Column {
        rows.forEachIndexed { index, (label, time) ->
            if (index > 0) {
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = colors.outlineVariant)
            }
            Surface(
                color = colors.surfaceContainerLow,
                contentColor = colors.onSurface,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = rowHeight)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    AnimatedValueText(time ?: "—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
