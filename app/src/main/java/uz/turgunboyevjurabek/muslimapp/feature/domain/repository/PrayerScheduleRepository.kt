package uz.turgunboyevjurabek.muslimapp.feature.domain.repository

import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.weekly.Birhaftalik
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.monthly.BirOylik
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.daily.Bugungi
import java.time.YearMonth

interface PrayerScheduleRepository {
    suspend fun getDailySchedule(location: Regions): Bugungi
    suspend fun getWeeklySchedule(location: Regions): Birhaftalik
    suspend fun getMonthlySchedule(location: Regions, month: YearMonth): BirOylik
}
