package uz.turgunboyevjurabek.muslimapp.feature.domein.service

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionsManager(private val activity: ComponentActivity) {
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    private val launcher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        if (result.values.any { it }) onPermissionGranted?.invoke()
        else onPermissionDenied?.invoke()
    }

    var onPermissionGranted: (() -> Unit)? = null
    var onPermissionDenied: (() -> Unit)? = null

    fun checkAndRequestLocationPermission() {
        if (permissions.any { ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED }) {
            onPermissionGranted?.invoke()
        } else {
            launcher.launch(permissions)
        }
    }
}
