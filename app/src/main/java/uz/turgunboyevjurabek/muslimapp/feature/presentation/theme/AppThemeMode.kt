package uz.turgunboyevjurabek.muslimapp.feature.presentation.theme

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.edit
import uz.turgunboyevjurabek.muslimapp.R

private const val preferencesName = "appearance"
private const val themeKey = "theme"

internal enum class AppThemeMode(val key: String, @StringRes val label: Int) {
    System("system", R.string.theme_system),
    Light("light", R.string.theme_light),
    Dark("dark", R.string.theme_dark);

    fun save(context: Context) {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit {
            putString(themeKey, key)
        }
    }

    companion object {
        fun load(context: Context): AppThemeMode {
            val key = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
                .getString(themeKey, null)
            return entries.firstOrNull { it.key == key } ?: System
        }
    }
}
