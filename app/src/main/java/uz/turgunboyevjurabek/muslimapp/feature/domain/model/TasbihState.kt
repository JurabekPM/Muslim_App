package uz.turgunboyevjurabek.muslimapp.feature.domain.model

data class DhikrSession(val count: Int, val finishedAtMillis: Long)

data class TasbihState(val count: Int, val history: List<DhikrSession>, val target: Int? = null)
