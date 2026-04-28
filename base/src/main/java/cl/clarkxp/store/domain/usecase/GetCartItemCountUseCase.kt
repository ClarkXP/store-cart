package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.domain.repository.CartRepository
import javax.inject.Inject

//Obtiene la suma total de items * quantity en el carrito
class GetCartItemCountUseCase @Inject constructor(
    private val repository: CartRepository
) {
    operator fun invoke() = repository.getCartItemCount()
}