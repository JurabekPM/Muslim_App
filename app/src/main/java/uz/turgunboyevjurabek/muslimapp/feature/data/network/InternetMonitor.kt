package uz.turgunboyevjurabek.muslimapp.feature.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

enum class InternetConnection { Connecting, Offline, Online }

class InternetMonitor @Inject constructor(@ApplicationContext context: Context) {
    private val manager = context.getSystemService(ConnectivityManager::class.java)

    fun observe() = callbackFlow {
        val connectivity = manager
        if (connectivity == null) {
            trySend(InternetConnection.Offline)
            close()
            return@callbackFlow
        }

        val initialNetwork = connectivity.activeNetwork
        trySend(connectivity.getNetworkCapabilities(initialNetwork).toConnection())
        val callback = object : ConnectivityManager.NetworkCallback() {
            private var currentNetwork: Network? = initialNetwork

            override fun onAvailable(network: Network) {
                if (currentNetwork != network) trySend(InternetConnection.Connecting)
                currentNetwork = network
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                if (currentNetwork == network) trySend(capabilities.toConnection())
            }

            override fun onLost(network: Network) {
                if (currentNetwork == network) {
                    currentNetwork = null
                    trySend(InternetConnection.Offline)
                }
            }
        }
        connectivity.registerDefaultNetworkCallback(callback)
        awaitClose { connectivity.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun NetworkCapabilities?.toConnection(): InternetConnection =
        if (this != null && hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) InternetConnection.Online else InternetConnection.Offline
}
