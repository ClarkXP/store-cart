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

    //función para agregar productos al carrito
    override suspend fun addToCart(product: Product) {
        // Verificamos si ya existe
        val existingItem = dao.getCartItemById(product.id)

        if (existingItem != null) {
            //Si existe, sumamos +1 a la cantidad
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
            dao.insertCartItem(updatedItem)
        } else {
            // Si no existe, lo creamos con cantidad 1
            dao.insertCartItem(product.toCartEntity(quantity = 1))
        }
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

    override suspend fun decreaseQuantity(productId: Int) {
        //revisamos que el producto exista en el carro, sino salimos de la ejecución
        val currentItem = dao.getCartItemById(productId) ?: return
        val newQuantity = currentItem.quantity - 1

        if (newQuantity <= 0) {
            dao.deleteCartItem(currentItem)
        } else {
            dao.insertCartItem(currentItem.copy(quantity = newQuantity))
        }
    }

    override suspend fun clearCart() {
        dao.clearCart()
    }
}