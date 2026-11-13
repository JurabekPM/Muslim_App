package uz.turgunboyevjurabek.muslimapp.feature.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class MyLocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
) {
    private var requestToken: CancellationTokenSource? = null
    var onLocationReceived: ((Location?) -> Unit)? = null

    fun getCurrentLocation() {
        if (requestToken != null) return
        val token = CancellationTokenSource()
        requestToken = token
        requestLocation(token) { location ->
            if (requestToken === token) {
                requestToken = null
                onLocationReceived?.invoke(location)
            }
        }
    }

    suspend fun awaitCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val token = CancellationTokenSource()
        continuation.invokeOnCancellation { token.cancel() }
        requestLocation(token) { location ->
            if (continuation.isActive) continuation.resume(location)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation(token: CancellationTokenSource, onComplete: (Location?) -> Unit) {
        val precise = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (!precise && !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            onComplete(null)
            return
        }

        val request = CurrentLocationRequest.Builder()
            .setPriority(if (precise) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(30_000L)
            .setDurationMillis(15_000L)
            .build()

        try {
            fusedLocationClient.getCurrentLocation(request, token.token).addOnCompleteListener { task ->
                onComplete(if (task.isSuccessful) task.result else null)
            }
        } catch (_: SecurityException) {
            onComplete(null)
        }
    }

    fun cancelRequest() {
        requestToken?.cancel()
        requestToken = null
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
