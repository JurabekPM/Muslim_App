package uz.turgunboyevjurabek.muslimapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueCoordinates
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosquePlace
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.nearbyMosques

class NearbyMosqueTest {
    private val origin = MosqueCoordinates(41.0, 69.0)

    @Test
    fun sortsByDistanceAndExcludesPlacesOutsideTheSearchRadius() {
        val results = nearbyMosques(
            origin,
            listOf(mosque("outside", 41.20), mosque("far", 41.15), mosque("nearest", 41.01)),
        )

        assertEquals(listOf("nearest", "far"), results.map { it.place.id })
        assertEquals(1_112.0, results.first().distanceMeters, 5.0)
    }

    @Test
    fun returnsAtMostFiftyOfTheClosestMosques() {
        val places = (1..60).reversed().map { mosque(it.toString(), 41.0 + it * 0.001) }

        val results = nearbyMosques(origin, places)

        assertEquals(50, results.size)
        assertEquals("1", results.first().place.id)
        assertEquals("50", results.last().place.id)
        assertTrue(results.zipWithNext().all { (first, next) -> first.distanceMeters < next.distanceMeters })
    }

    private fun mosque(id: String, latitude: Double) = MosquePlace(id, MosqueCoordinates(latitude, 69.0), id)
}
