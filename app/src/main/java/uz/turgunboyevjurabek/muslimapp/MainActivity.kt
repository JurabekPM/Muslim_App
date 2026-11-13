package uz.turgunboyevjurabek.muslimapp

import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import android.app.AlertDialog
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.StateListAnimator
import android.content.DialogInterface
import android.view.View
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import uz.turgunboyevjurabek.muslimapp.core.MyApplication
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.CounterViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.PrayerScheduleViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.SelectRegionViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla.QiblaViewModel
import uz.turgunboyevjurabek.muslimapp.feature.data.location.CompassSensorManager
import uz.turgunboyevjurabek.muslimapp.feature.data.location.MyLocationManager
import uz.turgunboyevjurabek.muslimapp.feature.data.location.PermissionsManager
import uz.turgunboyevjurabek.muslimapp.feature.data.location.RegionLocationLookup
import uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation.MuslimApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var myLocationManager: MyLocationManager

    @Inject
    lateinit var regionLocationLookup: RegionLocationLookup

    @Inject
    lateinit var compassSensorManager: CompassSensorManager

    private lateinit var permissionsManager: PermissionsManager

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguage.load(newBase).localize(newBase))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataStore = (application as MyApplication).dataStore
        val dataStoreRegion = (application as MyApplication).dataStoreRegion

        val qiblaViewModel by viewModels<QiblaViewModel>()
        val scheduleViewModel by viewModels<PrayerScheduleViewModel>()
        val counterViewModel by viewModels<CounterViewModel> {
            viewModelFactory { initializer { CounterViewModel(dataStore) } }
        }
        val regionViewModel by viewModels<SelectRegionViewModel> {
            viewModelFactory { initializer { SelectRegionViewModel(dataStoreRegion) } }
        }

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        // Lokatsiya yoqilganligini tekshirish
        if (!isGpsEnabled && !isNetworkEnabled) {
            // Agar lokatsiya xizmatlari o'chirilgan bo'lsa, foydalanuvchidan yoqishni so'rash
            showLocationDialog()
        }

        permissionsManager = PermissionsManager(this)

        enableEdgeToEdge()
        setContent {
            MuslimApp(
                scheduleViewModel = scheduleViewModel,
                regionViewModel = regionViewModel,
                counterViewModel = counterViewModel,
                qiblaViewModel = qiblaViewModel,
                regionLocationLookup = regionLocationLookup,
                locationManager = myLocationManager,
                compassSensorManager = compassSensorManager,
                permissionsManager = permissionsManager,
                displayRotation = ::displayRotation,
                language = AppLanguage.load(this),
                onLanguageChange = { selected ->
                    if (selected != AppLanguage.load(this)) {
                        selected.save(this)
                        recreate()
                    }
                },
            )
        }

    }

    @Suppress("DEPRECATION")
    private fun displayRotation(): Int = windowManager.defaultDisplay.rotation

    // Lokatsiya xizmatlari o'chirilgan bo'lsa, foydalanuvchiga uni yoqishni so'rash uchun dialog
    private fun showLocationDialog() {
        val dialog = AlertDialog.Builder(this).apply {
            setTitle(R.string.location_services_off)
            setMessage(R.string.location_services_hint)
            setCancelable(false)
            setPositiveButton(R.string.enable) { _, _ ->
                // Lokatsiya sozlamalarini ochish
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        }.show()
        for (action in listOf(DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE)) {
            val button = dialog.getButton(action)
            fun scaleTo(scale: Float, durationMillis: Long) = ObjectAnimator.ofPropertyValuesHolder(
                button,
                PropertyValuesHolder.ofFloat(View.SCALE_X, scale),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, scale),
            ).setDuration(durationMillis)
            button.stateListAnimator = StateListAnimator().apply {
                addState(intArrayOf(android.R.attr.state_pressed), scaleTo(0.94f, 85L))
                addState(intArrayOf(), scaleTo(1f, 160L))
            }
        }
    }
}
