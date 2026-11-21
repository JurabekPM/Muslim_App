package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionSelection
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.RegionSelectionRepository
import javax.inject.Inject

@HiltViewModel
class SelectRegionViewModel @Inject constructor(
    private val repository: RegionSelectionRepository,
) : ViewModel() {
    val selection = repository.selection.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    suspend fun saveRegion(selection: RegionSelection) = repository.save(selection)
}
