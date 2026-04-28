package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.domain.repository.CartRepository
import javax.inject.Inject

class DecreaseQuantityUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(productId: Int) {
        repository.decreaseQuantity(productId)
    }
}