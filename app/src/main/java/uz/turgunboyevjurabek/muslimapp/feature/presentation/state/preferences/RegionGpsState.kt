package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences

import androidx.annotation.StringRes
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionLocationResult
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.Regions

internal sealed interface RegionGpsState {
    data object Idle : RegionGpsState
    data object Finding : RegionGpsState
    data class Resolved(val region: Regions) : RegionGpsState
    data class Failed(@StringRes val message: Int) : RegionGpsState
}

internal fun RegionLocationResult.toGpsState(): RegionGpsState = when (this) {
    is RegionLocationResult.Found -> RegionGpsState.Resolved(region)
    RegionLocationResult.LocationOff -> RegionGpsState.Failed(R.string.location_services_hint)
    RegionLocationResult.NoFix -> RegionGpsState.Failed(R.string.location_no_fix)
    RegionLocationResult.AddressUnavailable -> RegionGpsState.Failed(R.string.location_no_address)
    RegionLocationResult.NoMatch -> RegionGpsState.Failed(R.string.location_no_match)
}
