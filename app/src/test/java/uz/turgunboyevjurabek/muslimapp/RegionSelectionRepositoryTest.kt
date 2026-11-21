package uz.turgunboyevjurabek.muslimapp

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.DataStoreRegionSelectionRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionSelection
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import java.io.File

class RegionSelectionRepositoryTest {
    @get:Rule val folder = TemporaryFolder()
    private val regionKey = stringPreferencesKey("select_region")
    private val gpsKey = booleanPreferencesKey("select_region_from_gps")

    @Test
    fun existingLegacySelectionAndGpsFlagSurviveReopen() = runBlocking {
        val file = File(folder.root, "region.preferences_pb")
        val legacyJob = Job()
        try {
            open(file, legacyJob).edit {
                it[regionKey] = "Qo'qon"
                it[gpsKey] = true
            }
        } finally {
            legacyJob.cancelAndJoin()
        }

        val qoqon = Regions.supported.single { it.slug == "qoqon-shahri" }
        val readJob = Job()
        try {
            val store = open(file, readJob)
            val repository = DataStoreRegionSelectionRepository(store)
            assertEquals(RegionSelection(qoqon, fromGps = true), repository.selection.first())
            assertEquals("Qo'qon", store.data.first()[regionKey])

            // A manual choice of the same region switches off the GPS badge.
            repository.save(RegionSelection(qoqon, fromGps = false))
            val saved = store.data.first()
            assertEquals("qoqon-shahri", saved[regionKey])
            assertEquals(false, saved[gpsKey])
        } finally {
            readJob.cancelAndJoin()
        }

        val manualJob = Job()
        val andijon = Regions.supported.single { it.slug == "andijon-shahri" }
        try {
            val repository = DataStoreRegionSelectionRepository(open(file, manualJob))
            assertEquals(RegionSelection(qoqon, fromGps = false), repository.selection.first())
            repository.save(RegionSelection(andijon, fromGps = true))
        } finally {
            manualJob.cancelAndJoin()
        }

        val gpsJob = Job()
        try {
            val repository = DataStoreRegionSelectionRepository(open(file, gpsJob))
            assertEquals(RegionSelection(andijon, fromGps = true), repository.selection.first())
        } finally {
            gpsJob.cancelAndJoin()
        }
    }

    @Test
    fun emptyPreferencesAndLegacyChoicesWithoutGpsFlagKeepTheirDefaults() = runBlocking {
        val job = Job()
        try {
            val store = open(File(folder.root, "defaults.preferences_pb"), job)
            val repository = DataStoreRegionSelectionRepository(store)
            assertEquals("fargona-shahri", repository.selection.first().region.slug)
            assertEquals(false, repository.selection.first().fromGps)

            store.edit { it[regionKey] = "Toshkent" }
            val choice = repository.selection.first()
            assertEquals("toshkent-shahri", choice.region.slug)
            assertEquals(false, choice.fromGps)
        } finally {
            job.cancelAndJoin()
        }
    }

    private fun open(file: File, job: Job) =
        PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
}
