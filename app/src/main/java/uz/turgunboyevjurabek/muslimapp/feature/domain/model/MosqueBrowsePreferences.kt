package uz.turgunboyevjurabek.muslimapp.feature.domain.model

internal data class MosqueBrowsePreferences(
    val area: MosqueArea = MosqueArea.Uzbekistan,
    val showMap: Boolean = false,
    val cityId: String? = null,
)
