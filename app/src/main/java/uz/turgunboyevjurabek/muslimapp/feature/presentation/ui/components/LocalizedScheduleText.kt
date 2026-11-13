package uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionKind
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.PrayerInterval

@Composable
internal fun appLocale() = LocalConfiguration.current.locales[0]

@Composable
internal fun Regions.displayName(): String {
    val name = nameRegion.removeSuffix(" shahri").removeSuffix(" tumani").removeSuffix(" viloyati")
    return when (kind) {
        RegionKind.City -> stringResource(R.string.city_name, name)
        RegionKind.District -> stringResource(R.string.district_name, name)
        RegionKind.Province -> if (slug == "qoraqalpogiston") stringResource(R.string.karakalpakstan)
        else stringResource(R.string.province_name, name)
    }
}

// Keep the cache and prayer-art lookup keyed by the provider's canonical Uzbek names.
@Composable
internal fun prayerName(name: String): String = when (name.lowercase(java.util.Locale.ROOT)) {
    "bomdod" -> stringResource(R.string.fajr)
    "quyosh" -> stringResource(R.string.sunrise)
    "peshin" -> stringResource(R.string.dhuhr)
    "asr" -> stringResource(R.string.asr)
    "shom" -> stringResource(R.string.maghrib)
    "xufton", "hufton" -> stringResource(R.string.isha)
    else -> name
}

@Composable
internal fun PrayerInterval.remainingCaption(nextName: String): String {
    val hours = minutesLeft / 60
    val minutes = minutesLeft % 60
    val remaining = when {
        hours == 0L -> stringResource(R.string.remaining_minutes, minutes)
        minutes == 0L -> stringResource(R.string.remaining_hours, hours)
        else -> stringResource(R.string.remaining_hours_minutes, hours, minutes)
    }
    return stringResource(R.string.until_prayer, prayerName(nextName), remaining)
}
