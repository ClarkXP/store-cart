package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetProductsUseCaseTest {

    private val repository: ProductRepository = mockk()

    private val useCase = GetProductsUseCase(repository)

    @Test
    fun `invoke producto con mayor (rate * count) debe ser el primero`() = runTest {
        // GIVEN: Una lista de productos desordenada
        // Producto A: Score = 3.0 * 10 = 30
        val productLow = createMockProduct(id = 1, rate = 3.0, count = 10)

        // Producto B: Score = 5.0 * 100 = 500 (El destacado)
        val productHigh = createMockProduct(id = 2, rate = 5.0, count = 100)

        // Producto C: Score = 4.0 * 50 = 200
        val productMid = createMockProduct(id = 3, rate = 4.0, count = 50)

        val unsortedList = listOf(productLow, productHigh, productMid)

        // Entrenamos al mock: Cuando pidan productos, devuelve esta lista
        coEvery { repository.getProducts() } returns flowOf(Resource.Success(unsortedList))

        // WHEN: Ejecutamos el caso de uso
        val flowResults = useCase().toList()
        val resultResource = flowResults.first()

        // THEN: Verificamos que el resultado sea exitoso y el orden sea correcto
        assertTrue(resultResource is Resource.Success)
        val sortedList = (resultResource as Resource.Success).data!!

        // Verificación Clave: El primer elemento debe ser el de ID 2 (El de mayor score)
        assertEquals("El primer producto debería ser el de mayor score", 2, sortedList[0].id)

        // Verificación Secundaria: El tamaño de la lista se mantiene
        assertEquals(3, sortedList.size)
    }

    // Helper para crear productos rápido y no ensuciar el test
    private fun createMockProduct(id: Int, rate: Double, count: Int): Product {
        return Product(
            id = id,
            title = "Test Product $id",
            price = 1000.0,
            description = "Desc",
            category = "Cat",
            image = "img",
            rating = rate,
            ratingCount = count
        )
    }


}