package uz.turgunboyevjurabek.muslimapp.feature.domain.model

sealed interface RegionLocationResult {
    data class Found(val region: Regions) : RegionLocationResult
    data object LocationOff : RegionLocationResult
    data object NoFix : RegionLocationResult
    data object AddressUnavailable : RegionLocationResult
    data object NoMatch : RegionLocationResult
}
