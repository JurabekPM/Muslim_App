@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class
)

package uz.turgunboyevjurabek.muslimapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.StateListAnimator
import android.content.DialogInterface
import android.view.View
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.hardware.GeomagneticField
import android.os.Build
import android.os.Bundle
import android.provider.Settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import uz.turgunboyevjurabek.muslimapp.core.MyApplication
import uz.turgunboyevjurabek.muslimapp.core.utils.calculateQiblaDirection
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.UIutils.screenFadeInMillis
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.UIutils.screenFadeOutMillis
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.Screens.DayOf30Screen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.Screens.Dayof7Screen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.Screens.MainScreen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.Screens.QiblaScreen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.Screens.TasbexScreen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.SheetDialogUI
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.DataStorePreferencesViewModel.CounterViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.PrayerScheduleViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.DataStorePreferencesViewModel.SelectRegionViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ViewModel.Qibla.MainViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme
import uz.turgunboyevjurabek.qiblafinderexample.service.CompassSensorManager
import uz.turgunboyevjurabek.qiblafinderexample.service.MyLocationManager
import uz.turgunboyevjurabek.muslimapp.feature.domein.service.PermissionsManager
import uz.turgunboyevjurabek.muslimapp.feature.domein.service.RegionLocationLookup
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.MyTopBar
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.myBottomNavigationBar
import uz.turgunboyevjurabek.muslimapp.feature.presentation.View.components.screenNameForRoute
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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "AutoboxingStateCreation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataStore = (application as MyApplication).dataStore
        val dataStoreRegion = (application as MyApplication).dataStoreRegion

        val mainViewModel by viewModels<MainViewModel>()
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
            MuslimAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val chromeBackdrop = rememberHazeState()
                    var isSheetOpen by rememberSaveable {
                        mutableStateOf(false)
                    }

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize(),
                        topBar = {
                            val entry by navController.currentBackStackEntryAsState()
                            val screenName = screenNameForRoute(entry?.destination?.route)
                            val connection by scheduleViewModel.connection.collectAsStateWithLifecycle()
                            MyTopBar(
                                modifier = Modifier,
                                navController = navController,
                                screenName = screenName,
                                backdrop = chromeBackdrop,
                                connection = connection,
                                refreshing = scheduleViewModel.isRefreshing(screenName),
                                onSheetClick = {
                                    isSheetOpen = true
                                }
                            )
                        },
                        bottomBar = {
                            myBottomNavigationBar(
                                navController = navController,
                                backdrop = chromeBackdrop,
                            )
                        }
                    ) { innerPadding ->
                        val direction = LocalLayoutDirection.current
                        val dockPadding = innerPadding.calculateBottomPadding()
                        val topPadding = innerPadding.calculateTopPadding()
                        Column(
                            modifier = Modifier
                                .padding(
                                    start = innerPadding.calculateStartPadding(direction),
                                    end = innerPadding.calculateEndPadding(direction),
                                )
                        ) {
                            if (isSheetOpen) {
                                SheetDialogUI(
                                    selectRegionViewModel = regionViewModel,
                                    lookupLocation = regionLocationLookup::find,
                                    onDismiss = { isSheetOpen = false },
                                )
                            }
                            /**
                             * For navigation between screens
                             */
                            NavHost(
                                navController = navController,
                                startDestination = "MainScreen",
                                enterTransition = { fadeIn(tween(screenFadeInMillis, delayMillis = screenFadeOutMillis)) },
                                exitTransition = { fadeOut(tween(screenFadeOutMillis)) },
                                popEnterTransition = { fadeIn(tween(screenFadeInMillis, delayMillis = screenFadeOutMillis)) },
                                popExitTransition = { fadeOut(tween(screenFadeOutMillis)) },
                                modifier = Modifier.fillMaxSize()
                                    .hazeSource(chromeBackdrop)
                                    .background(MaterialTheme.colorScheme.background),
                            ) {
                                composable("MainScreen") {
                                    MainScreen(
                                        selectRegionViewModel = regionViewModel,
                                        schedule = scheduleViewModel,
                                        bottomPadding = dockPadding,
                                        topPadding = topPadding,
                                    )
                                }
                                composable("TasbexScreen") {
                                    Box(Modifier.padding(top = topPadding, bottom = dockPadding)) {
                                        TasbexScreen(
                                            navController = navController,
                                            viewModel = counterViewModel
                                        )
                                    }
                                }
                                composable("Dayof7Screen") {
                                    Dayof7Screen(
                                        selectRegionViewModel = regionViewModel,
                                        schedule = scheduleViewModel,
                                        bottomPadding = dockPadding,
                                        topPadding = topPadding,
                                    )
                                }
                                composable("Dayof30Screen") {
                                    DayOf30Screen(
                                        selectRegionViewModel = regionViewModel,
                                        schedule = scheduleViewModel,
                                        bottomPadding = dockPadding,
                                        topPadding = topPadding,
                                    )
                                }
                                composable("QiblaScreen") { backStackEntry ->
                                    var compassAvailable by remember { mutableStateOf(true) }
                                    DisposableEffect(backStackEntry.lifecycle) {
                                        myLocationManager.onLocationReceived = { location ->
                                            if (location == null) {
                                                mainViewModel.updateQiblaDirection(null)
                                            } else {
                                                val bearing = calculateQiblaDirection(location.latitude, location.longitude)
                                                val field = GeomagneticField(
                                                    location.latitude.toFloat(),
                                                    location.longitude.toFloat(),
                                                    location.altitude.toFloat(),
                                                    System.currentTimeMillis(),
                                                )
                                                compassSensorManager.setMagneticReference(
                                                    field.fieldStrength / 1000f,
                                                    field.inclination,
                                                )
                                                mainViewModel.updateQiblaDirection(bearing.toFloat(), field.declination)
                                            }
                                        }
                                        compassSensorManager.onDirectionChanged = mainViewModel::updateCurrentDirection
                                        permissionsManager.onPermissionGranted = myLocationManager::getCurrentLocation
                                        val observer = LifecycleEventObserver { _, event ->
                                            when (event) {
                                                Lifecycle.Event.ON_RESUME -> {
                                                    mainViewModel.beginLocationLookup()
                                                    compassAvailable = compassSensorManager.registerListeners {
                                                        windowManager.defaultDisplay.rotation
                                                    }
                                                    myLocationManager.getCurrentLocation()
                                                }
                                                Lifecycle.Event.ON_PAUSE -> {
                                                    compassSensorManager.unregisterListeners()
                                                    myLocationManager.cancelRequest()
                                                    mainViewModel.updateCurrentDirection()
                                                }
                                                else -> Unit
                                            }
                                        }
                                        backStackEntry.lifecycle.addObserver(observer)
                                        permissionsManager.checkAndRequestLocationPermission()
                                        onDispose {
                                            backStackEntry.lifecycle.removeObserver(observer)
                                            compassSensorManager.unregisterListeners()
                                            compassSensorManager.onDirectionChanged = null
                                            myLocationManager.cancelRequest()
                                            myLocationManager.onLocationReceived = null
                                            permissionsManager.onPermissionGranted = null
                                            mainViewModel.updateCurrentDirection()
                                        }
                                    }
                                    QiblaScreen(
                                        modifier = Modifier.padding(bottom = dockPadding),
                                        qiblaState = mainViewModel.qiblaState,
                                        compassAvailable = compassAvailable,
                                        topPadding = topPadding,
                                    )
                                }


                            }
                        }

                    }
                }
            }
        }

    }

    // Lokatsiya xizmatlari o'chirilgan bo'lsa, foydalanuvchiga uni yoqishni so'rash uchun dialog
    private fun showLocationDialog() {
        val dialog = AlertDialog.Builder(this).apply {
            setTitle("Location Services Disabled")
            setMessage("Please enable location services to continue using the app.")
            setCancelable(false)
            setPositiveButton("Enable") { _, _ ->
                // Lokatsiya sozlamalarini ochish
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
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
