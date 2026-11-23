package uz.turgunboyevjurabek.muslimapp.feature.domain.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal const val nearbyMosqueRadiusKm = 20
private const val earthRadiusMeters = 6_371_000.0

internal data class MosqueCoordinates(val latitude: Double, val longitude: Double) {
    fun distanceTo(other: MosqueCoordinates): Double {
        val latitude1 = Math.toRadians(latitude)
        val latitude2 = Math.toRadians(other.latitude)
        val halfLongitudeDelta = Math.toRadians(other.longitude - longitude) / 2
        val haversine = sin((latitude2 - latitude1) / 2).let { it * it } +
            cos(latitude1) * cos(latitude2) * sin(halfLongitudeDelta).let { it * it }
        return 2 * earthRadiusMeters * asin(sqrt(haversine.coerceIn(0.0, 1.0)))
    }
}

internal data class MosquePlace(
    val id: String,
    val coordinates: MosqueCoordinates,
    val name: String?,
    val address: String? = null,
    val names: Map<String, String>? = null,
    val aliases: List<String>? = null,
    val locality: String? = null,
    val localityApproximate: Boolean = false,
) {
    fun label(language: String): String? = names?.get(language) ?: name
}

internal data class NearbyMosque(val place: MosquePlace, val distanceMeters: Double)

internal fun nearbyMosques(origin: MosqueCoordinates, places: List<MosquePlace>): List<NearbyMosque> =
    places.asSequence()
        .map { NearbyMosque(it, origin.distanceTo(it.coordinates)) }
        .filter { it.distanceMeters <= nearbyMosqueRadiusKm * 1_000 }
        .sortedBy(NearbyMosque::distanceMeters)
        .take(50)
        .toList()
