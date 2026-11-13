package uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation

import androidx.annotation.StringRes
import androidx.annotation.DrawableRes

internal data class BottomNavigationItem(
    @StringRes val title: Int,
    @DrawableRes val outlineIcon: Int,
    @DrawableRes val filledIcon: Int,
    val route: String,
)
