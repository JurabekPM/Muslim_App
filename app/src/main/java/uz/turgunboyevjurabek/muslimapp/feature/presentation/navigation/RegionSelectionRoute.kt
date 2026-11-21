package uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import uz.turgunboyevjurabek.muslimapp.R
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.RegionLocationResult
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.RegionGpsState
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.SelectRegionViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.toGpsState
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.RegionSelectionScreen

@Composable
internal fun RegionSelectionRoute(
    viewModel: SelectRegionViewModel,
    lookupLocation: suspend () -> RegionLocationResult,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var gpsState by remember { mutableStateOf<RegionGpsState>(RegionGpsState.Idle) }
    var requestingPermission by rememberSaveable { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (requestingPermission) {
            requestingPermission = false
            gpsState = if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                RegionGpsState.Finding
            } else {
                RegionGpsState.Failed(R.string.location_permission)
            }
        }
    }
    val finding = gpsState == RegionGpsState.Finding
    LaunchedEffect(finding) {
        if (finding) {
            val result = lookupLocation()
            if (gpsState == RegionGpsState.Finding) gpsState = result.toGpsState()
        }
    }
    fun cancelLocation() {
        requestingPermission = false
        gpsState = RegionGpsState.Idle
    }

    RegionSelectionScreen(
        selectRegionViewModel = viewModel,
        findingLocation = finding || requestingPermission,
        gpsState = gpsState,
        onLocate = {
            gpsState = RegionGpsState.Idle
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                gpsState = RegionGpsState.Finding
            } else {
                requestingPermission = true
                permission.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                )
            }
        },
        onCancelLocation = ::cancelLocation,
        onClose = {
            cancelLocation()
            onClose()
        },
    )
}
