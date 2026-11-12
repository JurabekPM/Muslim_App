package uz.turgunboyevjurabek.muslimapp.feature.data.Calendar

import android.os.Build
import androidx.annotation.RequiresApi
import uz.turgunboyevjurabek.muslimapp.core.utils.getDayOfMonthStartingFromMonday

import java.time.LocalDate
import java.time.YearMonth


class CalendarDataSource {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDates(yearMonth: YearMonth): List<CalendarUiState.Date> {
        val today = LocalDate.now()
        return yearMonth.getDayOfMonthStartingFromMonday()
            .map { date ->
                CalendarUiState.Date(
                    dayOfMonth = if (date.monthValue == yearMonth.monthValue) {
                        "${date.dayOfMonth}"
                    } else {
                        "" // Fill with empty string for days outside the current month
                    },
                    isSelected = false,
                    isToday = date == today && YearMonth.from(date) == yearMonth,

                )
            }
    }
}