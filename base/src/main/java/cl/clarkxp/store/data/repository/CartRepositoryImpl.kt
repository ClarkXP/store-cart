package cl.clarkxp.store.data.repository

import cl.clarkxp.store.data.local.dao.CartDao
import cl.clarkxp.store.data.mapper.toCartEntity
import cl.clarkxp.store.data.mapper.toDomain
import cl.clarkxp.store.data.mapper.toEntity
import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val dao: CartDao
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> {
        return dao.getAllCartItems().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCartItemCount(): Flow<Int> {
        return dao.getTotalItemsCount().map { it ?: 0 }
    }

    // Usamos el método atómico del DAO para evitar condiciones de carrera (read-modify-write)
    override suspend fun addToCart(product: Product) {
        dao.atomicAddToCart(product.toCartEntity(quantity = 1))
    }

    override suspend fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity > 0) {
            val entity = cartItem.toEntity().copy(quantity = newQuantity)
            dao.insertCartItem(entity)
        }
    }

    override suspend fun removeFromCart(cartItem: CartItem) {
        dao.deleteCartItem(cartItem.toEntity())
    }

    // Usamos el método atómico del DAO para evitar condiciones de carrera (read-modify-write)
    override suspend fun decreaseQuantity(productId: Int) {
        dao.atomicDecreaseQuantity(productId)
    }

    override suspend fun clearCart() {
        dao.clearCart()
    }
}