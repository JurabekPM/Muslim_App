package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi

@Composable
fun TimeListCard(todayData: Bugungi) {
    val times = todayData.times
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        PrayerScheduleRows(
            times?.tongSaharlik, times?.quyosh, times?.peshin, times?.asr,
            times?.shomIftor, times?.hufton,
            featuredPrayer = todayData.featuredPrayer,
            featuredTime = todayData.featuredTime,
            featuredIsCurrent = todayData.featuredIsCurrent,
        )
    }
}

@Composable
internal fun PrayerScheduleRows(
    bomdod: String?,
    quyosh: String?,
    peshin: String?,
    asr: String?,
    shom: String?,
    xufton: String?,
    rowHeight: Dp = 47.dp,
    featuredPrayer: String? = null,
    featuredTime: String? = null,
    featuredIsCurrent: Boolean = false,
) {
    val rows = listOf(
        "Bomdod" to bomdod,
        "Quyosh" to quyosh,
        "Peshin" to peshin,
        "Asr" to asr,
        "Shom" to shom,
        "Xufton" to xufton,
    )
    val colors = MaterialTheme.colorScheme
    Column {
        rows.forEachIndexed { index, (label, time) ->
            val featured = time != null && time == featuredTime &&
                label.equals(featuredPrayer, ignoreCase = true)
            if (index > 0) {
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = colors.outlineVariant)
            }
            Surface(
                color = if (featured) colors.primaryContainer else colors.surfaceContainerLow,
                contentColor = if (featured) colors.onPrimaryContainer else colors.onSurface,
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
                        fontWeight = if (featured) FontWeight.SemiBold else FontWeight.Normal,
                    )
                    if (featured) {
                        AnimatedValueText(
                            if (featuredIsCurrent) "Hozir" else "Keyingi",
                            modifier = Modifier.background(colors.tertiary, RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            color = colors.onTertiary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    AnimatedValueText(time ?: "—", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
