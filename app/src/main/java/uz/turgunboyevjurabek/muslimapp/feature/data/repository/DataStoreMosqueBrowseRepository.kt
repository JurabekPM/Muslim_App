package uz.turgunboyevjurabek.muslimapp.feature.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueArea
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueBrowsePreferences
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.MosqueBrowseRepository
import javax.inject.Inject
import javax.inject.Singleton

private val Context.mosqueBrowseStore by preferencesDataStore(name = "mosque_browse")

@Singleton
internal class DataStoreMosqueBrowseRepository(
    private val store: DataStore<Preferences>,
) : MosqueBrowseRepository {
    @Inject constructor(@ApplicationContext context: Context) : this(context.mosqueBrowseStore)

    override suspend fun load(): MosqueBrowsePreferences {
        val preferences = store.data.first()
        val savedArea = preferences[areaKey]
        return MosqueBrowsePreferences(
            area = MosqueArea.entries.firstOrNull { it.storageKey == savedArea } ?: MosqueArea.Nearby,
            showMap = preferences[showMapKey] == true,
            cityId = preferences[cityIdKey],
        )
    }

    override suspend fun save(preferences: MosqueBrowsePreferences) {
        store.edit { saved ->
            saved[areaKey] = preferences.area.storageKey
            saved[showMapKey] = preferences.showMap
            val cityId = preferences.cityId
            if (cityId == null) saved.remove(cityIdKey) else saved[cityIdKey] = cityId
        }
    }

    private companion object {
        val areaKey = stringPreferencesKey("area")
        val showMapKey = booleanPreferencesKey("show_map")
        val cityIdKey = stringPreferencesKey("city_id")
    }
}

private val MosqueArea.storageKey: String
    get() = when (this) {
        MosqueArea.Uzbekistan -> "uzbekistan"
        MosqueArea.Nearby -> "nearby"
        else -> checkNotNull(regionSlug)
    }
