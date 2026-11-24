package uz.turgunboyevjurabek.muslimapp.feature.domain.model

internal data class MosqueBrowsePreferences(
    val area: MosqueArea = MosqueArea.Nearby,
    val showMap: Boolean = false,
    val cityId: String? = null,
)
