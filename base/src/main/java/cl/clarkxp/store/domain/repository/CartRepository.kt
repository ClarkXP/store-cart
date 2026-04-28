package cl.clarkxp.store.domain.repository

import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    fun getCartItemCount(): Flow<Int>
    suspend fun addToCart(product: Product)
    suspend fun updateQuantity(cartItem: CartItem, newQuantity: Int)
    suspend fun removeFromCart(cartItem: CartItem)
    suspend fun decreaseQuantity(productId: Int)
    suspend fun clearCart()
}