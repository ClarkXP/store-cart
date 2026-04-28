package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.domain.repository.CartRepository
import javax.inject.Inject

// Incrementa atómicamente la cantidad de un producto existente en el carrito
class IncreaseQuantityUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(productId: Int) {
        repository.increaseQuantity(productId)
    }
}
