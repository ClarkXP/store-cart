package cl.clarkxp.store.data.repository

import cl.clarkxp.store.data.local.dao.CartDao
import cl.clarkxp.store.data.mapper.toCartEntity
import cl.clarkxp.store.data.mapper.toDomain
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

    // Operación atómica para agregar un producto nuevo al carrito (INSERT o INCREMENT)
    override suspend fun addToCart(product: Product) {
        dao.atomicAddToCart(product.toCartEntity())
    }

    // Operación atómica para incrementar la cantidad de un producto existente
    override suspend fun increaseQuantity(productId: Int) {
        dao.incrementQuantity(productId)
    }

    // Operación atómica para decrementar o eliminar si llega a 0
    override suspend fun decreaseQuantity(productId: Int) {
        dao.atomicDecreaseQuantity(productId)
    }

    override suspend fun clearCart() {
        dao.clearCart()
    }
}