package uz.turgunboyevjurabek.muslimapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uz.turgunboyevjurabek.muslimapp.feature.domein.madels.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domein.madels.RegionKind
import uz.turgunboyevjurabek.muslimapp.feature.domein.madels.RegionAddressMatcher

class RegionCatalogTest {
    @Test
    fun oldSavedNamesKeepTheirPrayerScheduleSlug() {
        assertEquals("qoqon-shahri", Regions.resolve("Qo'qon").slug)
        assertEquals("toshkent-shahri", Regions.resolve("Toshkent").slug)
        assertEquals("denov", Regions.resolve("Denov").slug)
        assertEquals("fargona-shahri", Regions.resolve(null).slug)
    }

    @Test
    fun searchRecognizesUzbekApostrophesAndSeparateLocationTypes() {
        val names = Regions.search("qoqon")
        assertEquals("qoqon-shahri", names.first().slug)
        assertEquals(names, Regions.search("Qo‘qon"))
        assertEquals(names, Regions.search("Qo'qon"))
        assertEquals("toshkent-tumani", Regions.search("toshkent tumani").single().slug)
        assertEquals("toshkent-viloyati", Regions.search("toshkent viloyati").single().slug)
        assertTrue(Regions.search("zzzzzzz").isEmpty())
    }

    @Test
    fun eachCatalogSlugRoundTripsThroughSavedSelection() {
        assertEquals(Regions.supported.size, Regions.supported.map { it.slug }.distinct().size)
        Regions.supported.forEach { region -> assertEquals(region, Regions.resolve(region.slug)) }
    }

    @Test
    fun filtersDistinguishMatchingCityDistrictAndProvinceNames() {
        assertEquals("andijon-shahri", Regions.search("Andijon", RegionKind.City).single().slug)
        assertEquals("andijon", Regions.search("Andijon", RegionKind.District).single().slug)
        assertEquals("andijon-viloyati", Regions.search("Andijon", RegionKind.Province).single().slug)
        assertEquals("chilonzor", Regions.search("Chilonzor", RegionKind.District).single().slug)
        assertEquals("angren", Regions.search("Angren", RegionKind.City).single().slug)
        assertEquals("qoraqalpogiston", Regions.search("Qoraqalpogiston", RegionKind.Province).single().slug)
        assertTrue(Regions.search("Angren", RegionKind.District).isEmpty())
    }

    @Test
    fun gpsAddressKeepsCityAndDistrictDistinct() {
        assertEquals("qoqon-shahri", RegionAddressMatcher.match("UZ", "Коканд", null, null, "Ферганская область")?.slug)
        assertEquals("fargona-shahri", RegionAddressMatcher.match("UZ", "Fergana", "Fergana District", null, null)?.slug)
        assertEquals("fargona", RegionAddressMatcher.match("UZ", "Unknown village", "Farg‘ona tumani", null, null)?.slug)
        assertEquals("toshkent-tumani", RegionAddressMatcher.match("UZ", null, "Tashkent District", null, "Tashkent Region")?.slug)
        assertEquals("chilonzor", RegionAddressMatcher.match("UZ", "Tashkent", null, "Chilanzar District", null)?.slug)
        assertEquals("ozbekiston", RegionAddressMatcher.match("UZ", null, "Uzbekistan District", null, "Fergana Region")?.slug)
    }

    @Test
    fun gpsAddressRequiresSupportedCityOrDistrictInUzbekistan() {
        assertNull(RegionAddressMatcher.match("KG", "Tashkent", null, null, null))
        assertNull(RegionAddressMatcher.match("UZ", null, null, null, "Fergana Region"))
        assertNull(RegionAddressMatcher.match("UZ", null, "Unknown District", null, "Fergana Region"))
    }
}
