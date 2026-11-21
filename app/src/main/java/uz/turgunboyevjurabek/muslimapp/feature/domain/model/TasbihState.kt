package uz.turgunboyevjurabek.muslimapp.feature.domain.model

data class DhikrSession(val count: Int, val finishedAtMillis: Long)

enum class TasbihFeedbackOption {
    TapSound,
    TapVibration,
    CompletionSound,
    CompletionVibration;

    companion object {
        val defaults = setOf(TapVibration, CompletionVibration)
    }
}

data class TasbihState(
    val count: Int,
    val history: List<DhikrSession>,
    val target: Int? = null,
    val feedback: Set<TasbihFeedbackOption> = TasbihFeedbackOption.defaults,
)
