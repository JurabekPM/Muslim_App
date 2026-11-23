package uz.turgunboyevjurabek.muslimapp.feature.domain.repository

import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueBrowsePreferences

internal interface MosqueBrowseRepository {
    suspend fun load(): MosqueBrowsePreferences
    suspend fun save(preferences: MosqueBrowsePreferences)
}
