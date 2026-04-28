package cl.clarkxp.store.di

import cl.clarkxp.store.data.repository.CartRepositoryImpl
import cl.clarkxp.store.data.repository.ProductRepositoryImpl
import cl.clarkxp.store.domain.repository.CartRepository
import cl.clarkxp.store.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Provee la implementación del repositorio de productos (Rest API), se puede cambiar la impl de forma agnóstica
    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    // Provee la implementación del repositorio del carro de compras (DB), se puede cambiar la impl de forma agnóstica
    @Binds
    @Singleton
    abstract fun bindCartRepository(
        impl: CartRepositoryImpl
    ): CartRepository
}