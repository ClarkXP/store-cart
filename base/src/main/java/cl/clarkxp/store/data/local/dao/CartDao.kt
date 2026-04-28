package cl.clarkxp.store.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.clarkxp.store.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    // Devuelve todos los productos.
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    // Para el badge rojo del toolbar
    @Query("SELECT SUM(quantity) FROM cart_items")
    fun getTotalItemsCount(): Flow<Int?>

    // Si ya existe el producto, lo reemplaza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    @Query("SELECT * FROM cart_items WHERE id = :id")
    suspend fun getCartItemById(id: Int): CartItemEntity?

    // Vaciar carro
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}