package uz.turgunboyevjurabek.muslimapp

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.core.utils.Status
import uz.turgunboyevjurabek.muslimapp.feature.data.local.SavedSchedule
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.ScheduleCache
import java.io.IOException

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
        ) {
            requests++
            response.await()
        }
        val period = "2026-11-12"
        cache.load(period)
        yield()
        assertEquals(0, requests)
        assertEquals("saved", cache[period].result.data)
        assertEquals("2026-11-11", cache[period].snapshot?.period)

        online = true
        cache.load(period, force = true)
        yield()
        assertEquals(1, requests)
        assertTrue(cache[period].refreshing)
        assertEquals("saved", cache[period].result.data)
        response.complete("fresh")
        yield()
        assertEquals("fresh", cache[period].result.data)
        assertEquals(period, cache[period].snapshot?.period)
    }

    @Test
    fun reopeningUsesLoadedDataAndRefreshKeepsItVisible() = runBlocking {
        var requests = 0
        var response = CompletableDeferred<String>()
        val cache = ScheduleCache(this) { _: String -> requests++; response.await() }
        cache.load("region")
        cache.load("region", force = true)
        yield()
        assertEquals(1, requests)
        assertEquals(Status.LOADING, cache["region"].result.status)

        response.complete("old")
        yield()
        cache.load("region")
        yield()
        assertEquals(1, requests)
        assertEquals("old", cache["region"].result.data)

        response = CompletableDeferred()
        cache.load("region", force = true)
        yield()
        assertTrue(cache["region"].refreshing)
        assertEquals("old", cache["region"].result.data)
        response.completeExceptionally(IOException())
        yield()
        assertFalse(cache["region"].refreshing)
        assertEquals("old", cache["region"].result.data)
        assertTrue(cache["region"].refreshError != null)
        assertNull(cache["other region"].result.data)
    }
}
