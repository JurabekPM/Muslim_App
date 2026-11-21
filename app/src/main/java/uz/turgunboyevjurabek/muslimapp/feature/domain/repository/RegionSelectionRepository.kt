package uz.turgunboyevjurabek.muslimapp.feature.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionSelection

interface RegionSelectionRepository {
    val selection: Flow<RegionSelection>
    suspend fun save(selection: RegionSelection)
}
