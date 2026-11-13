package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.calendar

import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.scheduleMonth
import androidx.compose.ui.res.stringResource
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.AnimatedValueText
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.PressIconButton
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
                contentDescription = stringResource(R.string.previous_month),
                modifier = Modifier.size(22.dp).rotate(-forwardRotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AnimatedValueText(
            text = scheduleMonth(yearMonth),
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
                contentDescription = stringResource(R.string.next_month),
                modifier = Modifier.size(22.dp).rotate(forwardRotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
