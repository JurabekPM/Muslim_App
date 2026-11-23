package uz.turgunboyevjurabek.muslimapp.feature.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.DataStoreMosqueBrowseRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.MosqueBrowseRepository

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MosqueBrowseModule {
    @Binds
    abstract fun bindMosqueBrowseRepository(repository: DataStoreMosqueBrowseRepository): MosqueBrowseRepository
}
