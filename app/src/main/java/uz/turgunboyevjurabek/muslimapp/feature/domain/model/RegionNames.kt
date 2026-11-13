package uz.turgunboyevjurabek.muslimapp.feature.domain.model

import java.util.Locale

internal val Regions.baseName: String
    get() = nameRegion.removeSuffix(" shahri").removeSuffix(" tumani").removeSuffix(" viloyati")

internal fun Regions.localizedBaseName(locale: Locale): String {
    if (locale.language == "uz" && locale.script != "Cyrl") return baseName
    val names = regionalNames[baseName]
    return when (locale.language) {
        "ru" -> names?.russian ?: toCyrillic(baseName, russian = true)
        "en" -> names?.english ?: baseName
        "kk" -> names?.kazakh ?: toCyrillic(baseName)
        "ky" -> names?.kyrgyz ?: toCyrillic(baseName)
        "tg" -> names?.tajik ?: toCyrillic(baseName)
        "uz" -> uzbekCyrillicSpellings[baseName] ?: toCyrillic(baseName)
        "tk" -> names?.turkmen ?: baseName
        else -> baseName
    }
}

private data class RegionalNames(
    val russian: String,
    val english: String,
    val kazakh: String? = null,
    val kyrgyz: String? = null,
    val tajik: String? = null,
    val turkmen: String? = null,
)

private val regionalNames = mapOf(
    "Qo‘qon" to RegionalNames("Коканд", "Kokand", "Қоқан", "Кокон", "Хӯқанд", "Kokan"),
    "Toshkent" to RegionalNames("Ташкент", "Tashkent", "Ташкент", "Ташкент", "Тошканд", "Daşkent"),
    "Farg‘ona" to RegionalNames("Фергана", "Fergana", "Ферғана", "Фергана", "Фарғона", "Fergana"),
    "Marg‘ilon" to RegionalNames("Маргилан", "Margilan", "Марғилан", "Маргалаң", "Марғелон"),
    "Andijon" to RegionalNames("Андижан", "Andijan", "Әндіжан", "Анжиян", "Андиҷон", "Andijan"),
    "Samarqand" to RegionalNames("Самарканд", "Samarkand", "Самарқанд", "Самарканд", "Самарқанд", "Samarkant"),
    "Buxoro" to RegionalNames("Бухара", "Bukhara", "Бұхара", "Бухара", "Бухоро", "Buhara"),
    "Namangan" to RegionalNames("Наманган", "Namangan", "Наманган", "Наманган", "Намангон"),
    "Qarshi" to RegionalNames("Карши", "Karshi", "Қаршы", "Каршы", "Қаршӣ"),
    "Urganch" to RegionalNames("Ургенч", "Urgench", "Үргеніш", "Үргөнч", "Урганҷ", "Ürgenç"),
    "Xiva" to RegionalNames("Хива", "Khiva", "Хиуа", "Хива", "Хева", "Hywa"),
    "Jizzax" to RegionalNames("Джизак", "Jizzakh", "Жызақ", "Жизак", "Ҷиззах"),
    "Navoiy" to RegionalNames("Навои", "Navoi", "Науаи", "Навои", "Навоӣ"),
    "Termiz" to RegionalNames("Термез", "Termez", "Термез", "Термез", "Тирмиз"),
    "Nukus" to RegionalNames("Нукус", "Nukus", "Нөкіс", "Нукус", "Нукус", "Nöküs"),
    "Sirdaryo" to RegionalNames("Сырдарья", "Syrdarya", "Сырдария", "Сырдарыя", "Сирдарё"),
    "Qashqadaryo" to RegionalNames("Кашкадарья", "Kashkadarya", "Қашқадария", "Кашкадарыя", "Қашқадарё"),
    "Surxondaryo" to RegionalNames("Сурхандарья", "Surkhandarya", "Сурхандария", "Сурхандарыя", "Сурхондарё"),
    "Xorazm" to RegionalNames("Хорезм", "Khorezm", "Хорезм", "Хорезм", "Хоразм", "Horezm"),
    "Rishton" to RegionalNames("Риштан", "Rishtan"),
    "Quva" to RegionalNames("Кува", "Kuva"),
    "Quvasoy" to RegionalNames("Кувасай", "Kuvasay"),
    "Olmaliq" to RegionalNames("Алмалык", "Almalyk"),
    "Chirchiq" to RegionalNames("Чирчик", "Chirchik"),
    "Chilonzor" to RegionalNames("Чиланзар", "Chilanzar"),
    "Yashnobod" to RegionalNames("Яшнабад", "Yashnabad"),
    "Mirobod" to RegionalNames("Мирабад", "Mirabad"),
    "Yunusobod" to RegionalNames("Юнусабад", "Yunusabad"),
    "Olmazor" to RegionalNames("Алмазар", "Almazar"),
    "Shayxontohur" to RegionalNames("Шайхантахур", "Shaykhantakhur"),
    "Qoraqalpog‘iston Resp." to RegionalNames(
        "Каракалпакстан", "Karakalpakstan", "Қарақалпақстан", "Каракалпакстан", "Қарақалпоқистон", "Garagalpagystan",
    ),
)

// Spellings that differ from transliteration in the provider’s Uzbek Cyrillic selector.
private val uzbekCyrillicSpellings = mapOf(
    "Boyovut" to "Бойовут",
    "Ellikqal’a" to "Элликқалъа",
    "Koson" to "Қосон",
    "Mang‘it" to "Мангит",
    "Muzrabot" to "Музрабод",
    "Qanliko‘l" to "Қанлиқўл",
    "Sayxunobod" to "Сайҳунобод",
    "Tayloq" to "Тойлоқ",
    "Tuproqqal’a" to "Тупроққалъа",
    "Urganch" to "Ўрганч",
    "Yangiyer" to "Янгийер",
    "Yangiyo‘l" to "Янгиёл",
)

private val latinLetters = ('a'..'z').zip("абсдефгҳижклмнопқрстуввхйз".toList()).toMap()
private val digraphs = mapOf(
    "o‘" to "ў", "g‘" to "ғ", "sh" to "ш", "ch" to "ч",
    "yo" to "ё", "yu" to "ю", "ya" to "я", "ye" to "е",
)

private fun toCyrillic(name: String, russian: Boolean = false): String = buildString {
    val source = name.lowercase(Locale.ROOT)
    var index = 0
    while (index < name.length) {
        val pair = source.substring(index, (index + 2).coerceAtMost(source.length))
        val digraph = digraphs[pair]
        val char = source[index]
        val letter = digraph ?: when {
            char == 'e' && (index == 0 || source[index - 1] in " -") -> "э"
            char == '‘' -> "ъ"
            else -> (latinLetters[char] ?: char).toString()
        }
        val converted = if (russian) letter.replace('ў', 'у').replace('ғ', 'г').replace('қ', 'к').replace('ҳ', 'х') else letter
        append(if (name[index].isUpperCase()) converted.uppercase(Locale.ROOT) else converted)
        index += if (digraph != null) 2 else 1
    }
}
