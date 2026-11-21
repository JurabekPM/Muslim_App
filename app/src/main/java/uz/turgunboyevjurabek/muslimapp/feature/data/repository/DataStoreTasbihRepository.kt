package uz.turgunboyevjurabek.muslimapp.feature.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.TasbihFeedbackOption
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
                val target = preferences[targetKey]?.takeIf { it > 0 }
                val feedback = feedbackKeys.keys.filterTo(mutableSetOf()) { option ->
                    preferences[feedbackKeys.getValue(option)] ?: (option in TasbihFeedbackOption.defaults)
                }
                emit(TasbihState(preferences[counterKey] ?: 0, history, target, feedback))
            }
    }

    override suspend fun increment(): Int? {
        var reachedTarget: Int? = null
        store.edit { preferences ->
            val count = preferences[counterKey] ?: 0
            if (count == Int.MAX_VALUE) return@edit

            val next = count + 1
            preferences[counterKey] = next
            if (next == preferences[targetKey] && preferences[targetReachedKey] != true) {
                preferences[targetReachedKey] = true
                reachedTarget = next
            }
        }
        return reachedTarget
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
            preferences.remove(targetReachedKey)
        }
    }

    override suspend fun setTarget(target: Int?) {
        require(target == null || target > 0)
        store.edit { preferences ->
            if (target == preferences[targetKey]) return@edit
            if (target == null) {
                preferences.remove(targetKey)
                preferences.remove(targetReachedKey)
            } else {
                preferences[targetKey] = target
                preferences[targetReachedKey] = (preferences[counterKey] ?: 0) >= target
            }
        }
    }

    override suspend fun setFeedback(option: TasbihFeedbackOption, enabled: Boolean) {
        store.edit { it[feedbackKeys.getValue(option)] = enabled }
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
        val targetKey = intPreferencesKey("tasbih_target")
        val targetReachedKey = booleanPreferencesKey("tasbih_target_reached")
        val feedbackKeys = mapOf(
            TasbihFeedbackOption.TapSound to booleanPreferencesKey("tasbih_tap_sound"),
            TasbihFeedbackOption.TapVibration to booleanPreferencesKey("tasbih_tap_vibration"),
            TasbihFeedbackOption.CompletionSound to booleanPreferencesKey("tasbih_completion_sound"),
            TasbihFeedbackOption.CompletionVibration to booleanPreferencesKey("tasbih_completion_vibration"),
        )
    }
}
