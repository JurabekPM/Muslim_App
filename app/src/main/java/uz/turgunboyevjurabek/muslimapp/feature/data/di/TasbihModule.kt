package uz.turgunboyevjurabek.muslimapp.feature.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import uz.turgunboyevjurabek.muslimapp.feature.data.repository.DataStoreTasbihRepository
import uz.turgunboyevjurabek.muslimapp.feature.domain.repository.TasbihRepository

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TasbihModule {
    @Binds
    abstract fun bindTasbihRepository(repository: DataStoreTasbihRepository): TasbihRepository
}
