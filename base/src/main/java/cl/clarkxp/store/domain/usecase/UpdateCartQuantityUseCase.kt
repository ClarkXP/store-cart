package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.domain.repository.CartRepository
import javax.inject.Inject

//actualiza la cantidad de un producto en el carrito, si es 0 lo elimina del carrito
class UpdateCartQuantityUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(item: CartItem, newQuantity: Int) {
        if (newQuantity <= 0) {
            repository.removeFromCart(item)
        } else {
            repository.updateQuantity(item, newQuantity)
        }
    }
}