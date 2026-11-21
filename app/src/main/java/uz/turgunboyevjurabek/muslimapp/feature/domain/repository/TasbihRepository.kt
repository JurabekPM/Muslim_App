package uz.turgunboyevjurabek.muslimapp.feature.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.TasbihFeedbackOption
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.TasbihState

interface TasbihRepository {
    val state: Flow<TasbihState>

    /** Increments the count and returns the target when it is reached for the first time. */
    suspend fun increment(): Int?
    suspend fun decrement()
    suspend fun reset()
    suspend fun setTarget(target: Int?)
    suspend fun setFeedback(option: TasbihFeedbackOption, enabled: Boolean)
}
