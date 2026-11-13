package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class CounterViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {
    private val counterKey = intPreferencesKey("counter")
    val counter: StateFlow<Int?> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { it[counterKey] ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun incrementCounter() {
        viewModelScope.launch {
            dataStore.edit { preferences -> preferences[counterKey] = (preferences[counterKey] ?: 0) + 1 }
        }
    }

    fun clearCounter() {
        viewModelScope.launch {
            dataStore.edit { preferences -> preferences.remove(counterKey) }
        }
    }
}
