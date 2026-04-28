package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class CartState(
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0
)

//Obtiene el listado completo de CartItems y su monto total
class GetCartUseCase @Inject constructor(
    private val repository: CartRepository
) {
    operator fun invoke(): Flow<CartState> {
        return repository.getCartItems().map { items ->
            // Calculamos el total sumando (precio * cantidad) de cada item
            val total = items.sumOf { it.price * it.quantity }
            CartState(items, total)
        }
    }
}