package uz.turgunboyevjurabek.muslimapp.feature.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.DataStoreRegionSelectionRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.RegionSelectionRepository

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RegionSelectionModule {
    @Binds
    abstract fun bindRegionSelectionRepository(repository: DataStoreRegionSelectionRepository): RegionSelectionRepository
}
