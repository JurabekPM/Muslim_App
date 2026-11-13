package uz.turgunboyevjurabek.muslimapp.feature.domain.model

internal data class RegionGroup(val region: Regions, val children: List<Regions>)

internal object RegionHierarchy {
    // Provider order from https://namoz-vaqti.uz/hududlar/, bundled for offline browsing.
    private val childSlugsByParent = mapOf(
        "toshkent-shahri" to listOf(
            "bektemir", "chilonzor", "mirobod", "mirzo-ulugbek", "sergeli",
            "shayxontohur", "uchtepa", "olmazor", "yakkasaroy", "yangihayot",
            "yashnobod", "yunusobod",
        ),
        "toshkent-viloyati" to listOf(
            "nurafshon", "angren", "bekobod-shahri", "chirchiq", "ohangaron-shahri",
            "olmaliq", "yangiyol-shahri", "bekobod", "boka", "bostonliq",
            "chinoz", "ohangaron", "oqqorgon", "parkent", "piskent",
            "qibray", "quyichirchiq", "ortachirchiq", "yangiyol", "yuqorichirchiq",
            "zangiota", "toshkent-tumani",
        ),
        "sirdaryo-viloyati" to listOf(
            "guliston-shahri", "shirin-shahri", "yangiyer-shahri", "boyovut", "guliston",
            "mirzaobod", "oqoltin", "sardoba", "sayxunobod", "sirdaryo",
            "xovos",
        ),
        "jizzax-viloyati" to listOf(
            "jizzax-shahri", "arnasoy", "baxmal", "dostlik", "forish",
            "gallaorol", "jizzax", "mirzachol", "paxtakor", "zafarobod",
            "zarbdor", "zomin",
        ),
        "samarqand-viloyati" to listOf(
            "samarqand-shahri", "kattaqorgon-shahri", "bulungur", "ishtixon", "jomboy",
            "kattaqorgon", "narpay", "nurobod", "oqdaryo", "paxtachi",
            "payariq", "pastdargom", "qoshrabot", "samarqand", "tayloq",
            "urgut",
        ),
        "namangan-viloyati" to listOf(
            "namangan-shahri", "chortoq", "chust", "kosonsoy", "mingbuloq",
            "namangan", "norin", "pop", "toraqorgon", "uchqorgon",
            "uychi", "yangiqorgon",
        ),
        "fargona-viloyati" to listOf(
            "fargona-shahri", "qoqon-shahri", "marghilon-shahri", "quvasoy-shahri", "beshariq",
            "bogdod", "buvayda", "dangara", "fargona", "furqat",
            "ozbekiston", "oltiariq", "qoshtepa", "quva", "rishton",
            "sox", "toshloq", "uchkoprik", "yozyovon",
        ),
        "andijon-viloyati" to listOf(
            "andijon-shahri", "xonobod-shahri", "asaka-shahri", "andijon", "asaka",
            "baliqchi", "boston", "buloqboshi", "izboskan", "jalaquduq",
            "marhamat", "oltinkol", "paxtaobod", "qorgontepa", "shahrixon",
            "xojaobod",
        ),
        "buxoro-viloyati" to listOf(
            "buxoro-shahri", "kogon-shahri", "buxoro", "vobkent", "gijduvon",
            "jondor", "kogon", "olot", "peshku", "qorakol",
            "qorovulbozor", "romitan", "shofirkon",
        ),
        "navoiy-viloyati" to listOf(
            "navoiy-shahri", "zarafshon-shahri", "karmana", "qiziltepa", "xatirchi",
            "navbahor", "nurota", "konimex", "tomdi", "uchquduq",
        ),
        "qashqadaryo-viloyati" to listOf(
            "qarshi-shahri", "shahrisabz-shahri", "chiroqchi", "dehqonobod", "guzor",
            "kasbi", "kitob", "koson", "mirishkor", "muborak",
            "nishon", "qarshi", "qamashi", "shahrisabz", "yakkabog",
        ),
        "surxondaryo-viloyati" to listOf(
            "termiz-shahri", "denov-shahri", "angor", "bandixon", "boysun",
            "denov", "jarqorgon", "muzrabot", "oltinsoy", "qiziriq",
            "qumqorgon", "sariosiyo", "sherobod", "shorchi", "termiz",
            "uzun",
        ),
        "xorazm-viloyati" to listOf(
            "urganch-shahri", "xiva-shahri", "bogot", "gurlan", "qoshkopir",
            "shovot", "urganch", "xazorasp", "xiva", "yangibozor",
            "yangiariq", "xonqa", "tuproqqala",
        ),
        "qoraqalpogiston" to listOf(
            "nukus-shahri", "taxiatosh", "tortkol-shahri", "amudaryo", "beruniy",
            "bozatov", "chimboy", "ellikqala", "kegeyli", "moynoq",
            "nukus-tumani", "qanlikol", "qoraozak", "kungirot", "shumanay",
            "taxtakopir", "tortkol", "xojayli", "mangit", "kungirot-shahri",
            "chimboy-shahri", "xojayli-shahri",
        ),
    )

    val groups: List<RegionGroup> = run {
        val regionsBySlug = Regions.supported.associateBy(Regions::slug)
        childSlugsByParent.map { (parent, children) ->
            RegionGroup(regionsBySlug.getValue(parent), children.map(regionsBySlug::getValue))
        }
    }

    val groupsBySlug = groups.associateBy { it.region.slug }
}
