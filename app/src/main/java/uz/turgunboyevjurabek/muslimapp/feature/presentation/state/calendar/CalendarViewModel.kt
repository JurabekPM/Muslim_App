package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.calendar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.YearMonth

class CalendarViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(createState(YearMonth.now()))
    val uiState = _uiState.asStateFlow()

    fun selectMonth(month: YearMonth) {
        _uiState.value = createState(month)
    }

    private fun createState(month: YearMonth) = CalendarUiState(
        yearMonth = month,
        dates = calendarDatesFor(month),
    )
}
