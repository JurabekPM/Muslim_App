package uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation

import uz.turgunboyevjurabek.muslimapp.core.utils.AppLanguage
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.screens.MenuScreen
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.AppThemeMode
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.CounterViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.MosqueBrowseViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.PrayerScheduleViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences.SelectRegionViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.state.qibla.QiblaViewModel
import uz.turgunboyevjurabek.muslimapp.feature.presentation.theme.MuslimAppTheme
import uz.turgunboyevjurabek.muslimapp.feature.data.location.QiblaSensors
import uz.turgunboyevjurabek.muslimapp.feature.data.location.RegionLocationLookup
import uz.turgunboyevjurabek.muslimapp.feature.data.local.MosqueCatalog
import uz.turgunboyevjurabek.muslimapp.feature.data.network.InternetConnection
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MyTopBar
import uz.turgunboyevjurabek.muslimapp.feature.presentation.ui.components.MyBottomNavigationBar

@Composable
internal fun MuslimApp(
    scheduleViewModel: PrayerScheduleViewModel,
    regionViewModel: SelectRegionViewModel,
    counterViewModel: CounterViewModel,
    qiblaViewModel: QiblaViewModel,
    regionLocationLookup: RegionLocationLookup,
    mosqueCatalog: MosqueCatalog,
    mosqueBrowseViewModel: MosqueBrowseViewModel,
    qiblaSensors: QiblaSensors,
    permissionsManager: PermissionsManager,
    displayRotation: () -> Int,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
) {
    val context = LocalContext.current
    var theme by remember(context) { mutableStateOf(AppThemeMode.load(context)) }
    val darkTheme = when (theme) {
        AppThemeMode.System -> isSystemInDarkTheme()
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    MuslimAppTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            val chromeBackdrop = rememberHazeState()
            val entry by navController.currentBackStackEntryAsState()
            val hasOwnBars = when (entry?.destination?.route) {
                AppRoute.REGIONS, AppRoute.MOSQUES -> true
                else -> false
            }

            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                topBar = {
                    if (!hasOwnBars) {
                        val route = entry?.destination?.route
                        val selection by regionViewModel.selection.collectAsStateWithLifecycle()
                        val connection by scheduleViewModel.connection.collectAsStateWithLifecycle()
                        val refreshing = scheduleViewModel.refreshing(route)?.collectAsStateWithLifecycle()
                        MyTopBar(
                            modifier = Modifier,
                            route = route,
                            region = selection?.region,
                            backdrop = chromeBackdrop,
                            connection = connection,
                            refreshing = refreshing?.value == true,
                            onRegionClick = {
                                navController.navigate(AppRoute.REGIONS) { launchSingleTop = true }
                            },
                            onBackClick = { navController.popBackStack() },
                        )
                    }
                },
                bottomBar = {
                    if (!hasOwnBars) MyBottomNavigationBar(
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
                        predictivePopEnterTransition = { fadeIn(tween(screenFadeInMillis)) },
                        predictivePopExitTransition = { fadeOut(tween(screenFadeOutMillis)) },
                        modifier = Modifier.fillMaxSize()
                            .hazeSource(chromeBackdrop)
                            .background(MaterialTheme.colorScheme.background),
                    ) {
                        composable(AppRoute.REGIONS) {
                            RegionSelectionRoute(
                                viewModel = regionViewModel,
                                lookupLocation = regionLocationLookup::find,
                                onClose = { navController.popBackStack() },
                            )
                        }
                        composable(AppRoute.HOME) {
                            MainScreen(
                                selectRegionViewModel = regionViewModel,
                                schedule = scheduleViewModel,
                                bottomPadding = dockPadding,
                                topPadding = topPadding,
                                onNearbyMosques = {
                                    mosqueBrowseViewModel.openNearby()
                                    navController.navigate(AppRoute.MOSQUES) { launchSingleTop = true }
                                },
                                onQibla = {
                                    navController.navigate(AppRoute.QIBLA) { launchSingleTop = true }
                                },
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
                        composable(AppRoute.MENU) {
                            MenuScreen(
                                theme = theme,
                                onThemeChange = {
                                    if (theme != it) {
                                        it.save(context)
                                        theme = it
                                    }
                                },
                                language = language,
                                onLanguageChange = onLanguageChange,
                                topPadding = topPadding,
                                bottomPadding = dockPadding,
                            )
                        }
                        composable(AppRoute.MOSQUES) {
                            val connection by scheduleViewModel.connection.collectAsStateWithLifecycle()
                            val preferences by mosqueBrowseViewModel.preferences.collectAsStateWithLifecycle()
                            preferences?.let { selection ->
                                NearbyMosquesRoute(
                                    loadCatalog = mosqueCatalog::load,
                                    findDeviceLocation = mosqueBrowseViewModel::findDeviceLocation,
                                    language = language,
                                    darkTheme = darkTheme,
                                    canLoadMap = connection == InternetConnection.Online,
                                    preferences = selection,
                                    onPreferencesChange = mosqueBrowseViewModel::update,
                                    onClose = { navController.popBackStack() },
                                )
                            }
                        }
                        composable(AppRoute.QIBLA) { backStackEntry ->
                            QiblaRoute(
                                lifecycle = backStackEntry.lifecycle,
                                viewModel = qiblaViewModel,
                                sensors = qiblaSensors,
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
