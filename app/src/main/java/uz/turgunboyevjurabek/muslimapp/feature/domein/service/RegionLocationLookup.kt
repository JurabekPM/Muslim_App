package uz.turgunboyevjurabek.muslimapp.feature.domein.service

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.location.LocationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import uz.turgunboyevjurabek.muslimapp.feature.domein.madels.RegionAddressMatcher
import uz.turgunboyevjurabek.muslimapp.feature.domein.madels.Regions
import uz.turgunboyevjurabek.qiblafinderexample.service.MyLocationManager
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

sealed interface RegionLocationResult {
    data class Found(val region: Regions) : RegionLocationResult
    data object LocationOff : RegionLocationResult
    data object NoFix : RegionLocationResult
    data object AddressUnavailable : RegionLocationResult
    data object NoMatch : RegionLocationResult
}

class RegionLocationLookup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val myLocationManager: MyLocationManager,
) {
    suspend fun find(): RegionLocationResult {
        val systemLocation = context.getSystemService(LocationManager::class.java)
        if (systemLocation == null || !LocationManagerCompat.isLocationEnabled(systemLocation)) {
            return RegionLocationResult.LocationOff
        }

        val location = withTimeoutOrNull(17_000) { myLocationManager.awaitCurrentLocation() }
            ?: return RegionLocationResult.NoFix
        if (!Geocoder.isPresent()) return RegionLocationResult.AddressUnavailable

        val addresses = try {
            withTimeoutOrNull(8_000) { getAddresses(location) }
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
        if (addresses.isNullOrEmpty()) return RegionLocationResult.AddressUnavailable

        val region = addresses.firstNotNullOfOrNull { address ->
            RegionAddressMatcher.match(
                countryCode = address.countryCode,
                locality = address.locality,
                subAdminArea = address.subAdminArea,
                subLocality = address.subLocality,
                adminArea = address.adminArea,
            )
        }
        return if (region == null) RegionLocationResult.NoMatch else RegionLocationResult.Found(region)
    }

    @Suppress("DEPRECATION")
    private suspend fun getAddresses(location: Location): List<Address>? {
        val geocoder = Geocoder(context, Locale.forLanguageTag("uz-Latn-UZ"))
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    3,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) continuation.resume(addresses)
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    },
                )
            }
        } else {
            runInterruptible(Dispatchers.IO) {
                geocoder.getFromLocation(location.latitude, location.longitude, 3)
            }
        }
    }
}
