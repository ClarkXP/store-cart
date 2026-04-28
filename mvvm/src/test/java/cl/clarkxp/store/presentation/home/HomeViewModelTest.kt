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
import cl.clarkxp.store.domain.usecase.GetProductsByCategoryUseCase
import cl.clarkxp.store.domain.usecase.GetProductsUseCase
import cl.clarkxp.store.domain.usecase.UpdateCartQuantityUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getProductsUseCase: GetProductsUseCase = mockk()
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase = mockk()
    private val getCartUseCase: GetCartUseCase = mockk()
    private val addToCartUseCase: AddToCartUseCase = mockk(relaxed = true)
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase = mockk(relaxed = true)
    private val getCartItemCountUseCase: GetCartItemCountUseCase = mockk()
    private val decreaseQuantityUseCase: DecreaseQuantityUseCase = mockk(relaxed = true)

    @Test
    fun `loadProducts actualiza uiState con exito cuando UseCase retorna datos`() =
        runTest {
            // GIVEN
            //Product con id 1 y un CartItem que referencia al mismo producto con quantity 2
            val mockProduct = Product(1, "TV", 100.0, "desc", "cat", "img", 5.0, 10)
            val mockCartItems = listOf(CartItem(1, "TV", 100.0, "img", 2))

            coEvery { getProductsUseCase() } returns flowOf(Resource.Success(listOf(mockProduct)))
            coEvery { getCartUseCase() } returns flowOf(CartState(items = mockCartItems))
            coEvery { getCartItemCountUseCase() } returns flowOf(2)

            val viewModel = HomeViewModel(
                getProductsUseCase,
                getProductsByCategoryUseCase,
                getCartItemCountUseCase,
                addToCartUseCase,
                getCartUseCase,
                decreaseQuantityUseCase
            )

            // Iniciamos una colección en background para despertar al WhileSubscribed
            val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect()
            }

            // WHEN (Ya ocurrió en el init, pero ahora el flow está activo)

            // THEN
            val state = viewModel.uiState.value

            // Si falla, imprime qué estado llegó
            if (state !is Resource.Success) {
                println("Estado recibido: $state")
            }

            assertTrue("El estado debería ser Success pero es $state", state is Resource.Success)

            val data = (state as Resource.Success).data!!
            assertEquals(1, data.size)
            // Se prueba que uiState haya combinado correctamente los flow de product y cart
            val uiModel = data[0]
            assertEquals(2, uiModel.quantity)

            collectJob.cancel()
        }

    @Test
    fun `loadProducts emite Error cuando UseCase falla`() = runTest {
        // GIVEN
        val errorMessage = "Error de red"
        coEvery { getProductsUseCase() } returns flowOf(Resource.Error(errorMessage))
        coEvery { getCartUseCase() } returns flowOf(CartState())
        coEvery { getCartItemCountUseCase() } returns flowOf(0)

        val viewModel = HomeViewModel(
            getProductsUseCase,
            getProductsByCategoryUseCase,
            getCartItemCountUseCase,
            addToCartUseCase,
            getCartUseCase,
            decreaseQuantityUseCase
        )

        // Iniciamos una colección en background para despertar al WhileSubscribed
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        // THEN
        val state = viewModel.uiState.value
        assertTrue("El estado debería ser Error pero es $state", state is Resource.Error)
        assertEquals(errorMessage, (state as Resource.Error).message)

        collectJob.cancel()
    }
}