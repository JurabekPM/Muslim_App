package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.TasbihFeedbackOption
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.TasbihRepository
import javax.inject.Inject

@HiltViewModel
class CounterViewModel @Inject constructor(
    private val repository: TasbihRepository,
) : ViewModel() {
    val state = repository.state.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun incrementCounter(onCounted: (completedTarget: Int?) -> Unit) {
        viewModelScope.launch { onCounted(repository.increment()) }
    }

    fun decrementCounter() {
        viewModelScope.launch { repository.decrement() }
    }

    fun clearCounter() {
        viewModelScope.launch { repository.reset() }
    }

    fun setTarget(target: Int?) {
        viewModelScope.launch { repository.setTarget(target) }
    }

    fun setFeedback(option: TasbihFeedbackOption, enabled: Boolean) {
        viewModelScope.launch { repository.setFeedback(option, enabled) }
    }
}
