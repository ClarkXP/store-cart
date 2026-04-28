package cl.clarkxp.store.di

import android.content.Context
import androidx.room.Room
import cl.clarkxp.store.data.local.AppDatabase
import cl.clarkxp.store.data.local.dao.CartDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    //Provee una instancia de la base de datos
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "store_cart_db"
        ).build()
    }

    //Provee una instancia del DAO con las operaciones de la base de datos
    @Provides
    fun provideCartDao(database: AppDatabase): CartDao {
        return database.cartDao()
    }
}