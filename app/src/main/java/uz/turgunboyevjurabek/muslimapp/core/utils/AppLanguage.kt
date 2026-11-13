package uz.turgunboyevjurabek.muslimapp.core.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

internal enum class AppLanguage(val tag: String, val label: String) {
    Uzbek("uz-Latn-UZ", "O‘zbekcha"),
    Russian("ru", "Русский"),
    English("en", "English");

    val locale: Locale get() = Locale.forLanguageTag(tag)

    fun localize(context: Context): Context {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    fun save(context: Context) {
        context.getSharedPreferences("app_language", Context.MODE_PRIVATE)
            .edit().putString("tag", tag).apply()
    }

    companion object {
        fun load(context: Context): AppLanguage {
            val saved = context.getSharedPreferences("app_language", Context.MODE_PRIVATE).getString("tag", null)
            return entries.firstOrNull { it.tag == saved } ?: Uzbek
        }
    }
}
