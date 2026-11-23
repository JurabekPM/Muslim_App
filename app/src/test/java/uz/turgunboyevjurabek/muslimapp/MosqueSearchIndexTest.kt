package uz.turgunboyevjurabek.muslimapp

import org.junit.Assert.assertEquals
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueCoordinates
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosquePlace
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueSearchIndex

class MosqueSearchIndexTest {
    private val samarkand = MosquePlace(
        "sam", MosqueCoordinates(39.65, 66.98), "Faqih Abulays",
        address = "Vobkend ko‘chasi 82", names = mapOf("uz-Cyrl" to "Фақиҳ Абулайс"), locality = "Samarqand",
    )
    private val tashkent = MosquePlace(
        "tash", MosqueCoordinates(41.32, 69.24), "Xo‘ja Ahror Valiy",
        aliases = listOf("Dzhuma Mosque"), locality = "Toshkent",
    )
    private val nukus = MosquePlace(
        "nukus", MosqueCoordinates(42.46, 59.61), null, locality = "Nókis", localityApproximate = true,
    )
    private val index = MosqueSearchIndex(
        listOf(samarkand, tashkent, nukus),
        listOf(MosquePlace("city", MosqueCoordinates(42.46, 59.61), "Nókis", names = mapOf("en" to "Nukus"))),
    )

    @Test
    fun findsNamesAcrossUzbekistanRegardlessOfScriptOrApostrophe() {
        assertEquals(listOf(samarkand), index.search("Фақиҳ Абулайс"))
        assertEquals(listOf(tashkent), index.search("xoja ahror"))
        assertEquals(listOf(tashkent), index.search("Dzhuma Mosque"))
    }

    @Test
    fun findsAnUnnamedMosqueByLocalityAndSupportsAddressSearch() {
        assertEquals(listOf(nukus), index.search("Nokis"))
        assertEquals(listOf(nukus), index.search("Nukus"))
        assertEquals(listOf(samarkand), index.search("Samarqand Vobkend 82"))
    }
}
