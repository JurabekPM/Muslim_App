package uz.turgunboyevjurabek.muslimapp.feature.presentation.state.preferences

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosqueBrowsePreferences
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.MosqueBrowseRepository
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
internal class MosqueBrowseViewModel @Inject constructor(
    private val repository: MosqueBrowseRepository,
) : ViewModel() {
    private val currentPreferences = MutableStateFlow<MosqueBrowsePreferences?>(null)
    val preferences = currentPreferences.asStateFlow()
    // Queue complete snapshots so rapid changes preserve both the area and the map choice.
    private val saves = Channel<MosqueBrowsePreferences>(Channel.CONFLATED)

    init {
        viewModelScope.launch {
            currentPreferences.value = try {
                repository.load()
            } catch (error: IOException) {
                Log.w("MosqueBrowse", "Could not load preferences", error)
                MosqueBrowsePreferences()
            }
            for (selection in saves) {
                try {
                    repository.save(selection)
                } catch (error: IOException) {
                    Log.w("MosqueBrowse", "Could not save preferences", error)
                }
            }
        }
    }

    fun update(change: (MosqueBrowsePreferences) -> MosqueBrowsePreferences) {
        val previous = currentPreferences.value ?: return
        val next = change(previous)
        if (next == previous) return
        currentPreferences.value = next
        saves.trySend(next)
    }
}
