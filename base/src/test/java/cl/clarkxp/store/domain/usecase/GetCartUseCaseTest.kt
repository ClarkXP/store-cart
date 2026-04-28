package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCartUseCaseTest {

    private val repository: CartRepository = mockk()
    private val useCase = GetCartUseCase(repository)

    @Test
    fun `invoke calcula correctamente el monto total sumando (precio * cantidad)`() = runTest {
        // GIVEN: Un carrito con 2 productos distintos
        // Item 1: $10.000 x 2 unidades = $20.000
        val item1 = CartItem(id = 1, title = "A", price = 10000.0, image = "", quantity = 2)

        // Item 2: $5.000 x 3 unidades = $15.000
        val item2 = CartItem(id = 2, title = "B", price = 5000.0, image = "", quantity = 3)

        val cartItems = listOf(item1, item2)

        // Entrenamos al mock
        coEvery { repository.getCartItems() } returns flowOf(cartItems)

        // WHEN: Ejecutamos
        val results = useCase().toList()
        val cartState = results.first()

        // THEN: Verificamos la suma
        // Total esperado: 20.000 + 15.000 = 35.000
        assertEquals(35000.0, cartState.totalAmount, 0.0)
        assertEquals(2, cartState.items.size)
    }

    @Test
    fun `invoke devuelve total 0 cuando el carro esta vacio`() = runTest {
        // GIVEN: Lista vacía
        coEvery { repository.getCartItems() } returns flowOf(emptyList())

        // WHEN
        val result = useCase().toList().first()

        // THEN
        assertEquals(0.0, result.totalAmount, 0.0)
    }
}