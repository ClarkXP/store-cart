package cl.clarkxp.store.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cl.clarkxp.store.data.local.dao.CartDao
import cl.clarkxp.store.data.local.entity.CartItemEntity

@Database(entities = [CartItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}