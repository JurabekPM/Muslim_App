

package uz.turgunboyevjurabek.muslimapp.feature.data.Calendar

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.YearMonth

data class CalendarUiState(
    val yearMonth: YearMonth,
    val dates: List<Date>
) {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        val Init = CalendarUiState(
            yearMonth = YearMonth.now(),
            dates = emptyList()
        )
    }
    data class Date(
        val dayOfMonth: String,
        val isSelected: Boolean,
        val isToday: Boolean,
    ) {
        companion object {
            val Empty = Date("", false, false)
        }
    }
}