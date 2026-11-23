package uz.turgunboyevjurabek.muslimapp.feature.domain.model

internal enum class MosqueArea(val regionCode: String? = null, val regionSlug: String? = null) {
    Uzbekistan,
    Nearby,
    Andijon("UZ-AN", "andijon-viloyati"),
    Buxoro("UZ-BU", "buxoro-viloyati"),
    Fargona("UZ-FA", "fargona-viloyati"),
    Jizzax("UZ-JI", "jizzax-viloyati"),
    Namangan("UZ-NG", "namangan-viloyati"),
    Navoiy("UZ-NW", "navoiy-viloyati"),
    Qashqadaryo("UZ-QA", "qashqadaryo-viloyati"),
    Karakalpakstan("UZ-QR", "qoraqalpogiston"),
    Samarqand("UZ-SA", "samarqand-viloyati"),
    Sirdaryo("UZ-SI", "sirdaryo-viloyati"),
    Surxondaryo("UZ-SU", "surxondaryo-viloyati"),
    ToshkentCity("UZ-TK", "toshkent-shahri"),
    ToshkentRegion("UZ-TO", "toshkent-viloyati"),
    Xorazm("UZ-XO", "xorazm-viloyati");

    fun selectMosques(catalog: List<MosquePlace>, nearby: List<MosquePlace>): List<MosquePlace> = when (this) {
        Uzbekistan -> catalog
        Nearby -> nearby
        else -> catalog.filter { it.regionCode == regionCode }
    }
}
