package uz.turgunboyevjurabek.muslimapp.feature.presentation.navigation

import androidx.annotation.DrawableRes

internal data class BottomNavigationItem(
    val title: String,
    @DrawableRes val outlineIcon: Int,
    @DrawableRes val filledIcon: Int,
    val route: String,
)
