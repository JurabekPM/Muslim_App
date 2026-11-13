package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import java.io.IOException

data class RegionSelection(val region: Regions, val fromGps: Boolean)

class SelectRegionViewModel(
    private val dataStoreRegion: DataStore<Preferences>,
) : ViewModel() {
    private val regionKey = stringPreferencesKey("select_region")
    private val fromGpsKey = booleanPreferencesKey("select_region_from_gps")

    val selection: StateFlow<RegionSelection?> = dataStoreRegion.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            RegionSelection(Regions.resolve(preferences[regionKey]), preferences[fromGpsKey] == true)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    suspend fun saveRegion(selection: RegionSelection) {
        dataStoreRegion.edit { preferences ->
            preferences[regionKey] = selection.region.slug
            preferences[fromGpsKey] = selection.fromGps
        }
    }
}
