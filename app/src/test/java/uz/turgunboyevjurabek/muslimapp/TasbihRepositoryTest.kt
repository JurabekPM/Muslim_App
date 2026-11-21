package uz.turgunboyevjurabek.muslimapp

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.DataStoreTasbihRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.DhikrSession
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.TasbihState
import java.io.File

class TasbihRepositoryTest {
    @get:Rule val folder = TemporaryFolder()
    private val counterKey = intPreferencesKey("counter")
    private val historyKey = stringPreferencesKey("dhikr_history")

    @Test
    fun legacyPreferencesSurviveReopenAndConcurrentResetsArchiveTheCountOnce() = runBlocking {
        val file = File(folder.root, "counter.preferences_pb")
        val legacy = List(20) { index -> DhikrSession(10 + index, 1_000L - index) }
        val legacyJob = Job()
        try {
            open(file, legacyJob).edit { preferences ->
                preferences[counterKey] = 143
                preferences[historyKey] = legacy.joinToString("\n") {
                    "${it.finishedAtMillis}:${it.count}"
                } + "\ninvalid\n0:3\n100:0"
            }
        } finally {
            legacyJob.cancelAndJoin()
        }

        val updateJob = Job()
        val archived = try {
            val store = open(file, updateJob)
            val repository = DataStoreTasbihRepository(store)
            assertEquals(TasbihState(143, legacy), repository.state.first())

            List(4) { async { repository.reset() } }.awaitAll()
            val result = repository.state.first()
            assertEquals(0, result.count)
            assertEquals(20, result.history.size)
            val session = result.history.first()
            assertEquals(143, session.count)
            assertTrue(session.finishedAtMillis > 0)
            assertEquals(legacy.take(19), result.history.drop(1))

            val saved = store.data.first()
            assertEquals(0, saved[counterKey])
            assertEquals("${session.finishedAtMillis}:143", saved[historyKey]?.lineSequence()?.first())
            repository.increment()
            result.history
        } finally {
            updateJob.cancelAndJoin()
        }

        val reopenJob = Job()
        try {
            val repository = DataStoreTasbihRepository(open(file, reopenJob))
            assertEquals(TasbihState(1, archived), repository.state.first())
        } finally {
            reopenJob.cancelAndJoin()
        }
    }

    @Test
    fun rapidUpdatesKeepAllTapsAndRespectBothCountLimits() = runBlocking {
        val job = Job()
        try {
            val store = open(File(folder.root, "bounds.preferences_pb"), job)
            val repository = DataStoreTasbihRepository(store)
            repository.decrement()
            repository.reset()
            assertEquals(TasbihState(0, emptyList()), repository.state.first())

            List(30) { async { repository.increment() } }.awaitAll()
            List(12) { async { repository.decrement() } }.awaitAll()
            assertEquals(18, repository.state.first().count)

            store.edit { it[counterKey] = Int.MAX_VALUE - 1 }
            List(3) { async { repository.increment() } }.awaitAll()
            assertEquals(Int.MAX_VALUE, repository.state.first().count)
        } finally {
            job.cancelAndJoin()
        }
    }

    private fun open(file: File, job: Job) =
        PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
}
