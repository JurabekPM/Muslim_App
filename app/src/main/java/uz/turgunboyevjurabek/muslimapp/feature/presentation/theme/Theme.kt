package uz.turgunboyevjurabek.muslimapp.feature.presentation.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val InkNavy = Color(0xFF273241)
private val Saffron = Color(0xFFF2D27C)
internal val ScheduleCardColor = Color(0xFF24354D)
internal val ScheduleCardContent = Color(0xFFEDF1F4)

private val DarkColorScheme = darkColorScheme(
    primary = Saffron,
    onPrimary = Color(0xFF2B210A),
    primaryContainer = Color(0xFF414535),
    onPrimaryContainer = Saffron,
    secondary = Color(0xFFB8CADB),
    onSecondary = Color(0xFF213143),
    secondaryContainer = Saffron,
    onSecondaryContainer = Color(0xFF2B210A),
    tertiary = Color(0xFFF3CD69),
    onTertiary = Color(0xFF302305),
    tertiaryContainer = Color(0xFF50442A),
    onTertiaryContainer = Color(0xFFFBE3A2),
    background = Color(0xFF101820),
    onBackground = Color(0xFFEDF1F4),
    surface = Color(0xFF101820),
    onSurface = Color(0xFFEDF1F4),
    onSurfaceVariant = Color(0xFFBDC9D6),
    surfaceVariant = Color(0xFF334353),
    surfaceContainerLow = Color(0xFF1C2935),
    surfaceContainer = Color(0xFF22313E),
    surfaceContainerHigh = Color(0xFF283947),
    surfaceContainerHighest = Color(0xFF304351),
    outline = Color(0xFF95A5B4),
    outlineVariant = Color(0xFF405261),
)

private val LightColorScheme = lightColorScheme(
    primary = InkNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF4E3AB),
    onPrimaryContainer = Color(0xFF4B3610),
    secondary = Color(0xFF526274),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4E3AB),
    onSecondaryContainer = Color(0xFF4B3610),
    tertiary = Color(0xFF795815),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5E7BC),
    onTertiaryContainer = Color(0xFF4B3610),
    background = Color(0xFFF8F8F5),
    onBackground = Color(0xFF273241),
    surface = Color(0xFFF8F8F5),
    onSurface = Color(0xFF273241),
    onSurfaceVariant = Color(0xFF485463),
    surfaceVariant = Color(0xFFE6E9E8),
    surfaceContainerLow = Color(0xFFEEEFEA),
    surfaceContainer = Color(0xFFEAEEE9),
    surfaceContainerHigh = Color(0xFFE6EAE7),
    surfaceContainerHighest = Color(0xFFDDE3E2),
    outline = Color(0xFF78848B),
    outlineVariant = Color(0xFFC9D1D1),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuslimAppTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AndroidColor.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = { CompositionLocalProvider(LocalRippleConfiguration provides null, content = content) },
    )
}
