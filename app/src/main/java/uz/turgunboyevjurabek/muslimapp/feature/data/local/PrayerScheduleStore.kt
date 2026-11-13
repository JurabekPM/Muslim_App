package uz.turgunboyevjurabek.muslimapp.feature.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prayerScheduleStore by preferencesDataStore("prayer_schedule_cache_v1")

internal data class SavedSchedule<T>(val data: T, val period: String, val savedAt: Long)

@Singleton
class PrayerScheduleStore internal constructor(private val store: DataStore<Preferences>) {
    @Inject constructor(@ApplicationContext context: Context) : this(context.prayerScheduleStore)
    private val gson = Gson()
    private val listType = object : TypeToken<List<StoredSchedule>>() {}.type

    internal suspend fun <T : Any> read(
        bucket: String,
        period: String,
        type: Class<T>,
    ): SavedSchedule<T>? = withContext(Dispatchers.IO) {
        val entries = try {
            decode(store.data.first()[stringPreferencesKey(bucket)])
        } catch (_: IOException) {
            emptyList()
        }
        val ordered = entries.sortedWith(
            compareByDescending<StoredSchedule> { it.period == period }.thenByDescending { it.savedAt },
        )
        ordered.firstNotNullOfOrNull { entry ->
            try {
                gson.fromJson(entry.data, type)?.let { SavedSchedule(it, entry.period, entry.savedAt) }
            } catch (_: JsonParseException) {
                null
            }
        }
    }

    internal suspend fun <T : Any> write(bucket: String, schedule: SavedSchedule<T>) {
        withContext(Dispatchers.IO) {
            val key = stringPreferencesKey(bucket)
            val entry = StoredSchedule(schedule.period, schedule.savedAt, gson.toJsonTree(schedule.data))
            store.edit { preferences ->
                val history = decode(preferences[key]).filterNot { it.period == entry.period }
                preferences[key] = gson.toJson(listOf(entry) + history.take(5), listType)
            }
        }
    }

    private fun decode(json: String?): List<StoredSchedule> = try {
        gson.fromJson<List<StoredSchedule>>(json, listType).orEmpty()
    } catch (_: JsonParseException) {
        emptyList()
    }

    private data class StoredSchedule(val period: String, val savedAt: Long, val data: JsonElement)
}
