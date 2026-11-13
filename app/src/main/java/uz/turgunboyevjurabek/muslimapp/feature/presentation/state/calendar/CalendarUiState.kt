

package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.calendar

import java.time.YearMonth

data class CalendarUiState(
    val yearMonth: YearMonth,
    val dates: List<Date>
) {
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