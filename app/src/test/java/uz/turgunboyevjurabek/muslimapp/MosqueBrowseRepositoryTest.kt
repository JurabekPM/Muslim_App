package uz.turgunboyevjurabek.muslimapp

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.DataStoreMosqueBrowseRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueArea
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueBrowsePreferences
import java.io.File

class MosqueBrowseRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun regionMapAndManualCitySurviveReopeningTheStore() = runBlocking {
        val file = File(folder.root, "mosque_browse.preferences_pb")
        val saved = MosqueBrowsePreferences(MosqueArea.Andijon, showMap = true, cityId = "node/123")
        val writer = Job()
        try {
            val repository = DataStoreMosqueBrowseRepository(open(file, writer))
            assertEquals(MosqueBrowsePreferences(), repository.load())
            repository.save(saved)
        } finally {
            writer.cancelAndJoin()
        }

        val reader = Job()
        try {
            val repository = DataStoreMosqueBrowseRepository(open(file, reader))
            assertEquals(saved, repository.load())
            val list = saved.copy(area = MosqueArea.Nearby, showMap = false, cityId = null)
            repository.save(list)
            assertEquals(list, repository.load())
        } finally {
            reader.cancelAndJoin()
        }
    }

    @Test
    fun unknownAreaFallsBackToNearbyAndRetainsMapChoice() = runBlocking {
        val ioJob = Job()
        try {
            val store = open(File(folder.root, "mosque_browse.preferences_pb"), ioJob)
            store.edit { saved ->
                saved[stringPreferencesKey("area")] = "obsolete-area"
                saved[booleanPreferencesKey("show_map")] = true
            }
            val repository = DataStoreMosqueBrowseRepository(store)
            assertEquals(MosqueBrowsePreferences(showMap = true), repository.load())
        } finally {
            ioJob.cancelAndJoin()
        }
    }

    private fun open(file: File, job: Job) =
        PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
}
