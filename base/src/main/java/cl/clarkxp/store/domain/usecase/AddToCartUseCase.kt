package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.repository.CartRepository
import javax.inject.Inject

// Cuando se agrega un Product al carrito (CartItem)
class AddToCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    suspend operator fun invoke(product: Product) {
        repository.addToCart(product)
    }
}