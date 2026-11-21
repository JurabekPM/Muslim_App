package uz.turgunboyevjurabek.muslimapp

import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.ScheduleCache
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.SavedSchedule
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.ScheduleFailure

class ScheduleCacheTest {
    @Test
    fun offlineOpenUsesStoredScheduleAndOnlineRefreshRetainsIt() = runBlocking {
        var online = false
        var requests = 0
        val response = CompletableDeferred<String>()
        val cache = ScheduleCache(
            scope = this,
            periodOf = { key: String -> key },
            online = { online },
            restore = { SavedSchedule("saved", "2026-11-11", 1L) },
            persist = { _, _ -> },
        ) {
            requests++
            response.await()
        }
        val period = "2026-11-12"
        cache.load(period)
        yield()
        assertEquals(0, requests)
        assertEquals("saved", cache.states.value[period]?.snapshot?.data)
        assertEquals("2026-11-11", cache.states.value[period]?.snapshot?.period)

        online = true
        cache.load(period, force = true)
        yield()
        assertEquals(1, requests)
        assertEquals(true, cache.states.value[period]?.refreshing)
        assertEquals("saved", cache.states.value[period]?.snapshot?.data)
        response.complete("fresh")
        yield()
        assertEquals("fresh", cache.states.value[period]?.snapshot?.data)
        assertEquals(period, cache.states.value[period]?.snapshot?.period)
    }

    @Test
    fun reopeningUsesLoadedDataAndRefreshKeepsItVisible() = runBlocking {
        var requests = 0
        var response = CompletableDeferred<String>()
        val cache = ScheduleCache(
            scope = this,
            periodOf = { key: String -> key },
            online = { true },
            restore = { null },
            persist = { _, _ -> },
        ) {
            requests++
            response.await()
        }
        cache.load("region")
        cache.load("region", force = true)
        yield()
        assertEquals(1, requests)
        assertEquals(true, cache.states.value["region"]?.requesting)

        response.complete("old")
        yield()
        cache.load("region")
        yield()
        assertEquals(1, requests)
        assertEquals("old", cache.states.value["region"]?.snapshot?.data)

        response = CompletableDeferred()
        cache.load("region", force = true)
        yield()
        assertEquals(true, cache.states.value["region"]?.refreshing)
        assertEquals("old", cache.states.value["region"]?.snapshot?.data)
        response.completeExceptionally(IOException())
        yield()
        assertEquals(false, cache.states.value["region"]?.refreshing)
        assertEquals("old", cache.states.value["region"]?.snapshot?.data)
        assertEquals(ScheduleFailure.Network, cache.states.value["region"]?.failure)
        assertNull(cache.states.value["other region"]?.snapshot?.data)
    }
}
