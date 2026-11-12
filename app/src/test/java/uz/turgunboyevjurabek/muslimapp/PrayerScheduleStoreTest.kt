package uz.turgunboyevjurabek.muslimapp

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import uz.turgunboyevjurabek.muslimapp.feature.data.network.PrayerScheduleStore
import uz.turgunboyevjurabek.muslimapp.feature.data.network.SavedSchedule
import uz.turgunboyevjurabek.muslimapp.feature.domein.madels.birHaftalik.Birhaftalik
import uz.turgunboyevjurabek.muslimapp.feature.domein.madels.birHaftalik.BirhaftalikItem
import uz.turgunboyevjurabek.muslimapp.feature.domein.madels.birOylk.BirOylik
import uz.turgunboyevjurabek.muslimapp.feature.domein.madels.birOylk.BirOylikItem
import uz.turgunboyevjurabek.muslimapp.feature.domein.madels.bugungilik.Bugungi
import java.io.File

class PrayerScheduleStoreTest {
    @get:Rule val folder = TemporaryFolder()

    @Test
    fun reopeningRecoversAllSchedulesAndUsesLatestWhenRequestedPeriodIsMissing() = runBlocking {
        val file = File(folder.root, "prayer.preferences_pb")
        val first = Job()
        try {
            val store = open(file, first)
            store.write("daily:qoqon", SavedSchedule(Bugungi(date = "11.11.2026"), "2026-11-11", 1L))
            store.write("daily:qoqon", SavedSchedule(Bugungi(date = "12.11.2026"), "2026-11-12", 2L))
            val week = Birhaftalik().apply { add(BirhaftalikItem(date = "12.11.2026")) }
            store.write("weekly:qoqon", SavedSchedule(week, "2026-11-12", 2L))
            val month = BirOylik().apply { add(BirOylikItem(date = "12.11.2026", day = 12, month = 11)) }
            store.write("monthly:qoqon", SavedSchedule(month, "2026-11", 2L))
        } finally {
            first.cancelAndJoin()
        }

        val second = Job()
        try {
            val store = open(file, second)
            val oldDay = store.read("daily:qoqon", "2026-11-11", Bugungi::class.java)
            val fallback = store.read("daily:qoqon", "2026-11-13", Bugungi::class.java)
            assertEquals("11.11.2026", oldDay?.data?.date)
            assertEquals("2026-11-12", fallback?.period)
            assertEquals("12.11.2026", fallback?.data?.date)
            assertEquals(2L, fallback?.savedAt)
            assertEquals("12.11.2026", store.read("weekly:qoqon", "2026-11-12", Birhaftalik::class.java)?.data?.single()?.date)
            assertEquals(12, store.read("monthly:qoqon", "2026-12", BirOylik::class.java)?.data?.single()?.day)
            assertNull(store.read("daily:andijon", "2026-11-12", Bugungi::class.java))
        } finally {
            second.cancelAndJoin()
        }
    }

    private fun open(file: File, job: Job) = PrayerScheduleStore(
        PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file },
    )
}
