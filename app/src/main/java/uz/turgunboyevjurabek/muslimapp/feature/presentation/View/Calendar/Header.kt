package uz.turgunboyevjurabek.muslimapp.feature.presentation.View.Calendar

import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.UIutils.AnimatedValueText
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.PressIconButton
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.core.utils.getDisplayName
import java.time.YearMonth
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Header(
    yearMonth: YearMonth,
    onPreviousMonthButtonClicked: (YearMonth) -> Unit,
    onNextMonthButtonClicked: (YearMonth) -> Unit,
) {
    val forwardRotation = if (LocalLayoutDirection.current == LayoutDirection.Ltr) -90f else 90f
    val chevron = painterResource(R.drawable.ic_chevron_down)
    Row {
        PressIconButton(onClick = { onPreviousMonthButtonClicked(yearMonth.minusMonths(1)) }) {
            Icon(
                painter = chevron,
                contentDescription = "Oldingi oy",
                modifier = Modifier.size(22.dp).rotate(-forwardRotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedValueText(
            text = yearMonth.getDisplayName(),
            contentAlignment = Alignment.Center,
            fontSize = 15.sp,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .padding(vertical = 10.dp)
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        PressIconButton(onClick = { onNextMonthButtonClicked(yearMonth.plusMonths(1)) }) {
            Icon(
                painter = chevron,
                contentDescription = "Keyingi oy",
                modifier = Modifier.size(22.dp).rotate(forwardRotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
