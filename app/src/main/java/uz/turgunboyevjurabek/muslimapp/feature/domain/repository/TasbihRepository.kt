package uz.turgunboyevjurabek.muslimapp.feature.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.TasbihState

interface TasbihRepository {
    val state: Flow<TasbihState>

    suspend fun increment()
    suspend fun decrement()
    suspend fun reset()
}
