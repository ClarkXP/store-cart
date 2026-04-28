package cl.clarkxp.store.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import cl.clarkxp.store.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT SUM(quantity) FROM cart_items")
    fun getTotalItemsCount(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :productId")
    suspend fun deleteCartItemById(productId: Int)

    @Query("SELECT quantity FROM cart_items WHERE id = :productId")
    suspend fun getQuantityById(productId: Int): Int?

    @Query("UPDATE cart_items SET quantity = quantity + 1 WHERE id = :productId")
    suspend fun incrementQuantity(productId: Int): Int

    @Query("UPDATE cart_items SET quantity = quantity - 1 WHERE id = :productId AND quantity > 0")
    suspend fun decrementQuantity(productId: Int): Int

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Transaction
    suspend fun atomicAddToCart(item: CartItemEntity) {
        val rowsUpdated = incrementQuantity(item.id)
        if (rowsUpdated == 0) {
            insertCartItem(item)
        }
    }

    @Transaction
    suspend fun atomicDecreaseQuantity(productId: Int) {
        val currentQty = getQuantityById(productId) ?: return
        if (currentQty <= 1) {
            deleteCartItemById(productId)
        } else {
            decrementQuantity(productId)
        }
    }
}