package uz.turgunboyevjurabek.muslimapp.feature.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionSelection
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.RegionSelectionRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.regionSelectionStore by preferencesDataStore(name = "region")

@Singleton
internal class DataStoreRegionSelectionRepository(
    private val store: DataStore<Preferences>,
) : RegionSelectionRepository {
    @Inject constructor(@ApplicationContext context: Context) : this(context.regionSelectionStore)

    override val selection: Flow<RegionSelection> = store.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            RegionSelection(Regions.resolve(preferences[regionKey]), preferences[fromGpsKey] == true)
        }

    override suspend fun save(selection: RegionSelection) {
        store.edit { preferences ->
            preferences[regionKey] = selection.region.slug
            preferences[fromGpsKey] = selection.fromGps
        }
    }

    private companion object {
        val regionKey = stringPreferencesKey("select_region")
        val fromGpsKey = booleanPreferencesKey("select_region_from_gps")
    }
}
