package uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation

import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.screenFadeInMillis
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.animation.screenFadeOutMillis
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.DayOf30Screen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.Dayof7Screen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.MainScreen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.TasbexScreen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.RegionSelectionScreen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.CounterViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.PrayerScheduleViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.SelectRegionViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla.QiblaViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme
import uz.turgunboyevjurabek.muslimapp.feature.data.location.CompassSensorManager
import uz.turgunboyevjurabek.muslimapp.feature.data.location.MyLocationManager
import uz.turgunboyevjurabek.muslimapp.feature.data.location.PermissionsManager
import uz.turgunboyevjurabek.muslimapp.feature.data.location.RegionLocationLookup
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MyTopBar
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.myBottomNavigationBar

@Composable
internal fun MuslimApp(
    scheduleViewModel: PrayerScheduleViewModel,
    regionViewModel: SelectRegionViewModel,
    counterViewModel: CounterViewModel,
    qiblaViewModel: QiblaViewModel,
    regionLocationLookup: RegionLocationLookup,
    locationManager: MyLocationManager,
    compassSensorManager: CompassSensorManager,
    permissionsManager: PermissionsManager,
    displayRotation: () -> Int,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    MuslimAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            val chromeBackdrop = rememberHazeState()
            val entry by navController.currentBackStackEntryAsState()
            val selectingRegion = entry?.destination?.route == AppRoute.REGIONS

            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                topBar = {
                    if (!selectingRegion) {
                        val route = entry?.destination?.route
                        val connection by scheduleViewModel.connection.collectAsStateWithLifecycle()
                        MyTopBar(
                            modifier = Modifier,
                            navController = navController,
                            route = route,
                            backdrop = chromeBackdrop,
                            connection = connection,
                            refreshing = scheduleViewModel.isRefreshing(route),
                            onRegionClick = {
                                navController.navigate(AppRoute.REGIONS) { launchSingleTop = true }
                            },
                        )
                    }
                },
                bottomBar = {
                    if (!selectingRegion) myBottomNavigationBar(
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
                    NavHost(
                        navController = navController,
                        startDestination = AppRoute.HOME,
                        enterTransition = { fadeIn(tween(screenFadeInMillis, delayMillis = screenFadeOutMillis)) },
                        exitTransition = { fadeOut(tween(screenFadeOutMillis)) },
                        popEnterTransition = { fadeIn(tween(screenFadeInMillis, delayMillis = screenFadeOutMillis)) },
                        popExitTransition = { fadeOut(tween(screenFadeOutMillis)) },
                        modifier = Modifier.fillMaxSize()
                            .hazeSource(chromeBackdrop)
                            .background(MaterialTheme.colorScheme.background),
                    ) {
                        composable(AppRoute.REGIONS) {
                            RegionSelectionScreen(
                                selectRegionViewModel = regionViewModel,
                                lookupLocation = regionLocationLookup::find,
                                language = language,
                                onLanguageChange = onLanguageChange,
                                onClose = { navController.popBackStack() },
                            )
                        }
                        composable(AppRoute.HOME) {
                            MainScreen(
                                selectRegionViewModel = regionViewModel,
                                schedule = scheduleViewModel,
                                bottomPadding = dockPadding,
                                topPadding = topPadding,
                            )
                        }
                        composable(AppRoute.TASBEX) {
                            Box(Modifier.padding(top = topPadding, bottom = dockPadding)) {
                                TasbexScreen(
                                    viewModel = counterViewModel,
                                )
                            }
                        }
                        composable(AppRoute.WEEKLY) {
                            Dayof7Screen(
                                selectRegionViewModel = regionViewModel,
                                schedule = scheduleViewModel,
                                bottomPadding = dockPadding,
                                topPadding = topPadding,
                            )
                        }
                        composable(AppRoute.MONTHLY) {
                            DayOf30Screen(
                                selectRegionViewModel = regionViewModel,
                                schedule = scheduleViewModel,
                                bottomPadding = dockPadding,
                                topPadding = topPadding,
                            )
                        }
                        composable(AppRoute.QIBLA) { backStackEntry ->
                            QiblaRoute(
                                lifecycle = backStackEntry.lifecycle,
                                viewModel = qiblaViewModel,
                                locationManager = locationManager,
                                compassSensorManager = compassSensorManager,
                                permissionsManager = permissionsManager,
                                displayRotation = displayRotation,
                                modifier = Modifier.padding(bottom = dockPadding),
                                topPadding = topPadding,
                            )
                        }
                    }
                }
            }
        }
    }
}
