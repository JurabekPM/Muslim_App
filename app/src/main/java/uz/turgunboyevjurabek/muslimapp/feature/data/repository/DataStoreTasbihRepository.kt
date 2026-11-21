package uz.turgunboyevjurabek.muslimapp.feature.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.DhikrSession
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.TasbihState
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.TasbihRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tasbihStore by preferencesDataStore(name = "counter")

@Singleton
internal class DataStoreTasbihRepository(
    private val store: DataStore<Preferences>,
) : TasbihRepository {
    @Inject constructor(@ApplicationContext context: Context) : this(context.tasbihStore)

    override val state: Flow<TasbihState> = flow {
        var lastHistoryValue: String? = null
        var history = emptyList<DhikrSession>()
        store.data
            .catch { error ->
                if (error is IOException) emit(emptyPreferences()) else throw error
            }
            .collect { preferences ->
                val historyValue = preferences[historyKey]
                if (historyValue != lastHistoryValue) {
                    history = decodeHistory(historyValue)
                    lastHistoryValue = historyValue
                }
                emit(TasbihState(preferences[counterKey] ?: 0, history))
            }
    }

    override suspend fun increment() {
        store.edit { preferences ->
            val count = preferences[counterKey] ?: 0
            if (count < Int.MAX_VALUE) preferences[counterKey] = count + 1
        }
    }

    override suspend fun decrement() {
        store.edit { preferences ->
            val count = preferences[counterKey] ?: 0
            if (count > 0) preferences[counterKey] = count - 1
        }
    }

    override suspend fun reset() {
        store.edit { preferences ->
            val count = preferences[counterKey] ?: 0
            if (count <= 0) return@edit

            val entry = "${System.currentTimeMillis()}:$count"
            val history = preferences[historyKey]
            preferences[historyKey] = if (history.isNullOrEmpty()) entry else "$entry\n$history"
            preferences[counterKey] = 0
        }
    }

    private fun decodeHistory(value: String?): List<DhikrSession> =
        value.orEmpty().lineSequence().mapNotNull { line ->
            val timestamp = line.substringBefore(':').toLongOrNull() ?: return@mapNotNull null
            val count = line.substringAfter(':', "").toIntOrNull() ?: return@mapNotNull null
            if (timestamp > 0 && count > 0) DhikrSession(count, timestamp) else null
        }.toList()

    private companion object {
        val counterKey = intPreferencesKey("counter")
        val historyKey = stringPreferencesKey("dhikr_history")
    }
}
