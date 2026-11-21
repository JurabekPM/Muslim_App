package uz.turgunboyevjurabek.muslimapp.core

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication:Application(){
    val dataStoreRegion: DataStore<Preferences> by preferencesDataStore(name = "region")
}