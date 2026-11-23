package uz.turgunboyevjurabek.muslimapp.feature.data.local

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import uz.turgunboyevjurabek.muslimapp.feature.domain.model.MosquePlace
import javax.inject.Inject
import javax.inject.Singleton

internal data class MosqueCatalogData(val mosques: List<MosquePlace>, val cities: List<MosquePlace>)

@Singleton
class MosqueCatalog @Inject constructor(@ApplicationContext private val context: Context) {
    private val mutex = Mutex()
    private var data: MosqueCatalogData? = null

    internal suspend fun load(): MosqueCatalogData = mutex.withLock {
        data ?: withContext(Dispatchers.IO) {
            context.assets.open("uzbekistan_mosques.json").bufferedReader().use { reader ->
                Gson().fromJson(reader, MosqueCatalogData::class.java)
            }
        }.also { data = it }
    }
}
