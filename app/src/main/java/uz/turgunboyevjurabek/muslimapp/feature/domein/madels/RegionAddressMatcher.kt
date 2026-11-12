package uz.turgunboyevjurabek.muslimapp.feature.domein.madels

import java.util.Locale

internal object RegionAddressMatcher {
    private val aliases = mapOf(
        "tashkent" to "toshkent",
        "kokand" to "qoqon",
        "quqon" to "qoqon",
        "fergana" to "fargona",
        "ferghana" to "fargona",
        "margilan" to "margilon",
        "marghilon" to "margilon",
        "andijan" to "andijon",
        "samarkand" to "samarqand",
        "bukhara" to "buxoro",
        "karshi" to "qarshi",
        "urgench" to "urganch",
        "khiva" to "xiva",
        "jizzakh" to "jizzax",
        "navoi" to "navoiy",
        "termez" to "termiz",
        "uzbekistan" to "ozbekiston",
        "rishtan" to "rishton",
        "kuvasay" to "quvasoy",
        "kuva" to "quva",
        "olmalik" to "olmaliq",
        "almalyk" to "olmaliq",
        "chirchik" to "chirchiq",
        "chilanzar" to "chilonzor",
        "chilanzor" to "chilonzor",
        "yashnabad" to "yashnobod",
        "mirabad" to "mirobod",
        "yunusabad" to "yunusobod",
        "almazar" to "olmazor",
        "shaykhantakhur" to "shayxontohur",
    )
    private val qualifiers = mapOf(
        "shahri" to RegionKind.City, "shahar" to RegionKind.City, "city" to RegionKind.City,
        "gorod" to RegionKind.City,
        "tumani" to RegionKind.District, "tuman" to RegionKind.District, "district" to RegionKind.District,
        "rayon" to RegionKind.District,
        "viloyati" to RegionKind.Province, "region" to RegionKind.Province,
        "province" to RegionKind.Province, "oblast" to RegionKind.Province, "resp" to RegionKind.Province,
    )
    private val cities by lazy { index(RegionKind.City) }
    private val districts by lazy { index(RegionKind.District) }

    fun match(
        countryCode: String?,
        locality: String?,
        subAdminArea: String?,
        subLocality: String?,
        adminArea: String?,
    ): Regions? {
        if (!"UZ".equals(countryCode, ignoreCase = true)) return null

        val city = find(locality, RegionKind.City)
        val administrativeCity = find(adminArea, RegionKind.City, requireQualifier = true)
        if (city?.slug == "toshkent-shahri" || administrativeCity?.slug == "toshkent-shahri") {
            return sequenceOf(subLocality, subAdminArea)
                .mapNotNull { find(it, RegionKind.District) }
                .firstOrNull { it.isTashkentCityDistrict }
                ?: cities["toshkent"]
        }

        return city
            ?: find(subAdminArea, RegionKind.District)
            ?: find(subLocality, RegionKind.District)
            ?: find(subAdminArea, RegionKind.City, requireQualifier = true)
    }

    private fun find(value: String?, defaultType: RegionKind, requireQualifier: Boolean = false): Regions? {
        val (key, type) = parse(value ?: return null)
        if (requireQualifier && type != defaultType) return null
        return when (type ?: defaultType) {
            RegionKind.City -> cities[key]
            RegionKind.District -> districts[key]
            RegionKind.Province -> null
        }
    }

    private fun index(type: RegionKind): Map<String, Regions> = Regions.supported.mapNotNull { region ->
        if (type == region.kind) parse(region.nameRegion).first to region else null
    }.toMap()

    private fun parse(value: String): Pair<String, RegionKind?> {
        val words = normalize(value).split(' ').filter { it.isNotEmpty() }
        val first = words.firstOrNull()
        val last = words.lastOrNull()
        val prefix = qualifiers[first]
        val suffix = qualifiers[last]
        val name = when {
            suffix != null -> words.dropLast(1)
            prefix != null -> words.drop(1).dropWhile { it == "of" }
            else -> words
        }.joinToString("")
        return (aliases[name] ?: name) to (suffix ?: prefix)
    }

    private fun normalize(value: String): String = buildString {
        value.lowercase(Locale.ROOT).forEach { char ->
            append(cyrillic[char] ?: when {
                char in 'a'..'z' || char in '0'..'9' -> char.toString()
                char in apostrophes -> ""
                else -> " "
            })
        }
    }

    private const val apostrophes = "'‘’ʻʼ`´"
    private val cyrillic = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e",
        'ё' to "yo", 'ж' to "j", 'з' to "z", 'и' to "i", 'й' to "y", 'к' to "k",
        'л' to "l", 'м' to "m", 'н' to "n", 'о' to "o", 'п' to "p", 'р' to "r",
        'с' to "s", 'т' to "t", 'у' to "u", 'ф' to "f", 'х' to "x", 'ц' to "ts",
        'ч' to "ch", 'ш' to "sh", 'щ' to "sh", 'ъ' to "", 'ы' to "i", 'ь' to "",
        'э' to "e", 'ю' to "yu", 'я' to "ya", 'ў' to "o", 'қ' to "q", 'ғ' to "g", 'ҳ' to "h",
    )
}
