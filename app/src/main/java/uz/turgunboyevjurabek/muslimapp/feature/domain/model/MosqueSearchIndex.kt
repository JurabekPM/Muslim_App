package uz.turgunboyevjurabek.muslimapp.feature.domain.model

internal class MosqueSearchIndex(private val mosques: List<MosquePlace>, cities: List<MosquePlace>) {
    private data class Entry(val place: MosquePlace, val names: Set<String>, val location: String) {
        fun rank(phrase: String, terms: List<String>): Int? = when {
            phrase in names -> 0
            names.any { it.startsWith(phrase) } -> 1
            names.any { terms.all(it::contains) } -> 2
            terms.all { term -> term in location || names.any { term in it } } -> 3
            else -> null
        }
    }

    private val entries = mosques.map { place ->
        val location = normalizePlaceSearch(listOfNotNull(place.address, place.locality).joinToString(" "))
        Entry(place, place.searchNames(), location)
    }
    private val citiesByName = cities
        .flatMap { city -> city.searchNames().map { it to city } }
        .groupBy({ it.first }, { it.second })

    fun search(query: String): List<MosquePlace> {
        val phrase = normalizePlaceSearch(query)
        if (phrase.length < 2) return emptyList()
        val terms = phrase.split(' ')
        val matches = entries.asSequence()
            .mapNotNull { entry -> entry.rank(phrase, terms)?.let { it to entry } }
            .sortedWith(compareBy({ it.first }, { it.second.names.firstOrNull().orEmpty() }, { it.second.location }))
            .map { it.second.place }
        val nearMatchingCities = citiesByName[phrase].orEmpty().asSequence()
            .flatMap { city -> nearbyMosques(city.coordinates, mosques).asSequence() }
            .map(NearbyMosque::place)
        return (matches + nearMatchingCities)
            .distinctBy(MosquePlace::id)
            .take(100)
            .toList()
    }
}

private fun MosquePlace.searchNames(): Set<String> =
    (listOfNotNull(name) + names.orEmpty().values + aliases.orEmpty())
        .map(::normalizePlaceSearch)
        .filter(String::isNotEmpty)
        .toSet()
