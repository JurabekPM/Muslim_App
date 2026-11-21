package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

data class DhikrSession(val count: Int, val finishedAtMillis: Long)

data class TasbihState(val count: Int, val history: List<DhikrSession>)

class CounterViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {
    private val counterKey = intPreferencesKey("counter")
    private val historyKey = stringPreferencesKey("dhikr_history")

    val state: StateFlow<TasbihState?> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            TasbihState(
                count = preferences[counterKey] ?: 0,
                history = decodeHistory(preferences[historyKey]),
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun incrementCounter() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val count = preferences[counterKey] ?: 0
                if (count < Int.MAX_VALUE) preferences[counterKey] = count + 1
            }
        }
    }

    fun decrementCounter() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val count = preferences[counterKey] ?: 0
                if (count > 0) preferences[counterKey] = count - 1
            }
        }
    }

    fun clearCounter() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val count = preferences[counterKey] ?: 0
                if (count <= 0) return@edit

                val session = DhikrSession(count, System.currentTimeMillis())
                val history = listOf(session) + decodeHistory(preferences[historyKey])
                preferences[historyKey] = history.take(20).joinToString("\n") {
                    "${it.finishedAtMillis}:${it.count}"
                }
                preferences.remove(counterKey)
            }
        }
    }

    private fun decodeHistory(value: String?): List<DhikrSession> =
        value.orEmpty().lineSequence().mapNotNull { line ->
            val timestamp = line.substringBefore(':').toLongOrNull() ?: return@mapNotNull null
            val count = line.substringAfter(':', "").toIntOrNull() ?: return@mapNotNull null
            if (timestamp > 0 && count > 0) DhikrSession(count, timestamp) else null
        }.toList()
}
