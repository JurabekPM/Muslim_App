package uz.turgunboyevjurabek.muslimapp

import org.junit.Assert.assertEquals
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueArea
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueCoordinates
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosquePlace

class MosqueAreaTest {
    @Test
    fun countryIncludesEveryPlaceAndRegionsUseAdministrativeCodes() {
        val city = place("city", "UZ-TK")
        val province = place("province", "UZ-TO")
        val karakalpakstan = place("nukus", "UZ-QR")
        val unassigned = place("boundary", null)
        val catalog = listOf(city, province, karakalpakstan, unassigned)

        assertEquals(catalog, MosqueArea.Uzbekistan.selectMosques(catalog, listOf(city)))
        assertEquals(listOf(city), MosqueArea.ToshkentCity.selectMosques(catalog, emptyList()))
        assertEquals(listOf(province), MosqueArea.ToshkentRegion.selectMosques(catalog, emptyList()))
        assertEquals(listOf(karakalpakstan), MosqueArea.Karakalpakstan.selectMosques(catalog, emptyList()))
        assertEquals(listOf(city), MosqueArea.Nearby.selectMosques(catalog, listOf(city)))
    }

    private fun place(id: String, regionCode: String?) =
        MosquePlace(id, MosqueCoordinates(41.0, 69.0), id, regionCode = regionCode)
}
