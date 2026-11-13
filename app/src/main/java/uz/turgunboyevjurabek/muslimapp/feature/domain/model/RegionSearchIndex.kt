package uz.turgunboyevjurabek.muslimapp.feature.domain.model

import java.text.Normalizer
import java.util.Locale

internal object RegionSearchIndex {
    private val locales = listOf("uz-Latn", "uz-Cyrl", "ru", "en", "kk", "ky", "tg", "tk")
        .map(Locale::forLanguageTag)
    private val spaces = Regex("\\s+")
    private val latin = Regex("\\p{IsLatin}+")
    private val diacritics = Regex("\\p{M}+")
    private val qualifiers = mapOf(
        RegionKind.City to "shahri shahar city город қаласы шаары шаҳр шаҳри şäheri",
        RegionKind.District to "tumani tuman district район района ауданы району ноҳия ноҳияи etraby",
        RegionKind.Province to "viloyati viloyat province region область области облысы облусу вилояти welaýaty",
    )
    private data class Entry(val region: Regions, val names: Set<String>, val words: Set<String>)

    private val entries by lazy {
        Regions.supported.map { region ->
            val names = buildList {
                add(region.baseName)
                add(region.slug.substringBeforeLast("-shahri").substringBeforeLast("-viloyati").substringBeforeLast("-tumani"))
                locales.forEach { add(region.localizedBaseName(it)) }
                addAll(RegionAddressMatcher.alternativeNames(region.baseName))
            }.map(::normalize).filter(String::isNotEmpty).toSet()
            val words = names.flatMap { it.split(' ') }.toSet()
            Entry(region, names, words)
        }
    }
    private val kindsByQualifier by lazy {
        qualifiers.flatMap { (kind, words) -> words.split(' ').map { normalize(it) to kind } }.toMap()
    }

    fun search(query: String, candidates: List<Regions> = Regions.supported): List<Regions> {
        val tokens = normalize(query).split(' ').filter(String::isNotEmpty)
        if (tokens.isEmpty()) return emptyList()
        val requestedKind = kindsByQualifier[tokens.last()] ?: kindsByQualifier[tokens.first()]
        val terms = if (requestedKind == null) tokens else tokens.filterNot { kindsByQualifier[it] == requestedKind }
        val allowed = candidates.toHashSet()
        val eligible = entries.filter { it.region in allowed && (requestedKind == null || it.region.kind == requestedKind) }
        if (terms.isEmpty()) return eligible.map(Entry::region)

        val phrase = terms.joinToString(" ")
        val joined = terms.joinToString("")
        val direct = eligible.mapNotNull { entry ->
            val rank = when {
                phrase in entry.names -> 0
                joined in entry.names -> 1
                entry.names.any { it.startsWith(phrase) } -> 1
                terms.all { term -> entry.words.any { it.contains(term) } } -> 2
                else -> return@mapNotNull null
            }
            entry.region to rank
        }
        if (direct.isNotEmpty()) return direct.sortedBy { it.second }.map { it.first }

        return eligible.mapNotNull { entry ->
            var score = 0
            for (term in terms) {
                val limit = when (term.length) {
                    in 0..3 -> 0
                    in 4..6 -> 1
                    else -> 2
                }
                val best = entry.words.minOf { word ->
                    when {
                        word.startsWith(term) -> 0
                        limit == 0 -> 1
                        else -> editDistance(term, word, limit)
                    }
                }
                if (best > limit) return@mapNotNull null
                score += best
            }
            entry.region to score
        }.sortedBy { it.second }.map { it.first }
    }

    private fun normalize(value: String): String {
        val folded = latin.replace(value) { match ->
            Normalizer.normalize(match.value, Normalizer.Form.NFD).replace(diacritics, "")
        }
        return RegionAddressMatcher.normalize(folded).trim().replace(spaces, " ")
    }

    // Adjacent letter swaps count as one typo.
    private fun editDistance(first: String, second: String, limit: Int): Int {
        if (kotlin.math.abs(first.length - second.length) > limit) return limit + 1
        var older = IntArray(second.length + 1)
        var previous = IntArray(second.length + 1) { it }
        var current = IntArray(second.length + 1)
        for (i in 1..first.length) {
            current[0] = i
            for (j in 1..second.length) {
                val substitution = if (first[i - 1] == second[j - 1]) 0 else 1
                current[j] = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + substitution)
                if (i > 1 && j > 1 && first[i - 1] == second[j - 2] && first[i - 2] == second[j - 1]) {
                    current[j] = minOf(current[j], older[j - 2] + 1)
                }
            }
            val reusable = older
            older = previous
            previous = current
            current = reusable
        }
        return previous[second.length]
    }
}
