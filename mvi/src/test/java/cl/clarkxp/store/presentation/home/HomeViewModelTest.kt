package cl.clarkxp.store.presentation.home

import cl.clarkxp.store.core.utils.MainDispatcherRule
import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.usecase.AddToCartUseCase
import cl.clarkxp.store.domain.usecase.CartState
import cl.clarkxp.store.domain.usecase.DecreaseQuantityUseCase
import cl.clarkxp.store.domain.usecase.GetCartItemCountUseCase
import cl.clarkxp.store.domain.usecase.GetCartUseCase
import cl.clarkxp.store.domain.usecase.GetCategoriesUseCase
import cl.clarkxp.store.domain.usecase.GetProductsByCategoryUseCase
import cl.clarkxp.store.domain.usecase.GetProductsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCategoriesUseCase: GetCategoriesUseCase = mockk()
    private val getProductsUseCase: GetProductsUseCase = mockk()
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase = mockk()
    private val getCartUseCase: GetCartUseCase = mockk()
    private val addToCartUseCase: AddToCartUseCase = mockk(relaxed = true)
    private val getCartItemCountUseCase: GetCartItemCountUseCase = mockk()
    private val decreaseQuantityUseCase: DecreaseQuantityUseCase = mockk(relaxed = true)

    @Test
    fun `loadProducts actualiza state con exito cuando UseCase retorna datos`() =
        runTest {
            // GIVEN
            val mockProduct = Product(1, "TV", 100.0, "desc", "cat", "img", 5.0, 10)
            val mockCartItems = listOf(CartItem(1, "TV", 100.0, "img", 2))

            coEvery { getCategoriesUseCase() } returns flowOf(Resource.Success(listOf("cat")))
            coEvery { getProductsUseCase() } returns flowOf(Resource.Success(listOf(mockProduct)))
            coEvery { getCartUseCase() } returns flowOf(CartState(items = mockCartItems))
            coEvery { getCartItemCountUseCase() } returns flowOf(2)

            val viewModel = HomeViewModel(
                getCategoriesUseCase,
                getProductsUseCase,
                getProductsByCategoryUseCase,
                getCartUseCase,
                getCartItemCountUseCase,
                addToCartUseCase,
                decreaseQuantityUseCase
            )

            // Iniciamos una colección en background para despertar al WhileSubscribed
            val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.state.collect()
            }

            // WHEN (Ya ocurrió en el init)

            // THEN
            val state = viewModel.state.value

            assertFalse("El estado no deberia estar cargando", state.isLoading)
            assertEquals("Deberia haber 1 producto", 1, state.products.size)
            
            val uiModel = state.products[0]
            assertEquals("La cantidad en carrito deberia ser 2", 2, uiModel.quantity)
            assertEquals("TV", uiModel.product.title)

            collectJob.cancel()
        }

    @Test
    fun `loadProducts emite Error en state cuando UseCase falla`() = runTest {
        // GIVEN
        val errorMessage = "Error de red"
        coEvery { getCategoriesUseCase() } returns flowOf(Resource.Success(listOf("cat")))
        coEvery { getProductsUseCase() } returns flowOf(Resource.Error(errorMessage))
        coEvery { getCartUseCase() } returns flowOf(CartState())
        coEvery { getCartItemCountUseCase() } returns flowOf(0)

        val viewModel = HomeViewModel(
            getCategoriesUseCase,
            getProductsUseCase,
            getProductsByCategoryUseCase,
            getCartUseCase,
            getCartItemCountUseCase,
            addToCartUseCase,
            decreaseQuantityUseCase
        )

        // Iniciamos una colección en background para despertar al WhileSubscribed
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect()
        }

        // THEN
        val state = viewModel.state.value
        assertNotNull("El estado debería tener un mensaje de error", state.error)
        assertEquals("El mensaje de error debería ser correcto", errorMessage, state.error)

        collectJob.cancel()
    }
}