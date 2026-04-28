package cl.clarkxp.store.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.clarkxp.store.core.extensions.capitalizeWords
import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.usecase.AddToCartUseCase
import cl.clarkxp.store.domain.usecase.DecreaseQuantityUseCase
import cl.clarkxp.store.domain.usecase.GetCartItemCountUseCase
import cl.clarkxp.store.domain.usecase.GetCartUseCase
import cl.clarkxp.store.domain.usecase.GetCategoriesUseCase
import cl.clarkxp.store.domain.usecase.GetProductsByCategoryUseCase
import cl.clarkxp.store.domain.usecase.GetProductsUseCase
import cl.clarkxp.store.presentation.home.model.ProductUiModel
import cl.clarkxp.store.presentation.home.mvi.HomeEffect
import cl.clarkxp.store.presentation.home.mvi.HomeIntent
import cl.clarkxp.store.presentation.home.mvi.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val getCartItemCountUseCase: GetCartItemCountUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val decreaseQuantityUseCase: DecreaseQuantityUseCase
) : ViewModel() {

    // --- 1. ESTADO INTERNO ---
    // Mantenemos los flujos raw para combinarlos
    private val _rawProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())
    private val _selectedCategory = MutableStateFlow("Todos")
    private val _selectedProductForDetail = MutableStateFlow<ProductUiModel?>(null)

    // Iniciamos con "Todos" para que siempre exista esa opción
    private val _categories = MutableStateFlow<List<String>>(listOf("Todos"))

    // --- 2. CANAL DE EFECTOS (Eventos de una sola vez) ---
    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    // --- 3. ESTADO UI MVI (La joya de la corona) ---
    // Combinamos: Productos API + Carrito DB + Categoría Seleccionada + Contador
    val state: StateFlow<HomeState> = combine(
        _rawProducts,
        getCartUseCase(),
        _selectedCategory,
        //getCartItemCountUseCase(),
        _categories,
        _selectedProductForDetail
    ) { productsRes, cartState, category, categoriesList, selectedProduct ->

        // Transformamos todo esto en un único HomeState
        val currentUiModels = when (productsRes) {
            is Resource.Success -> {
                val cartMap = cartState.items.associate { it.id to it.quantity }
                productsRes.data?.map { product ->
                    ProductUiModel(product, cartMap[product.id] ?: 0)
                } ?: emptyList()
            }

            else -> emptyList()
        }

        val updatedSelectedProduct = if (selectedProduct != null) {
            currentUiModels.find { it.product.id == selectedProduct.product.id } ?: selectedProduct
        } else {
            null
        }
        val count = cartState.items.sumOf { it.quantity }

        HomeState(
            isLoading = productsRes is Resource.Loading,
            products = currentUiModels,
            cartCount = count,
            selectedCategory = category,
            categories = categoriesList,
            selectedProductForDetail = updatedSelectedProduct,
            error = if (productsRes is Resource.Error) productsRes.message else null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState()
    )

    init {
        loadCategories()
        loadProducts("Todos")
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { result ->
                if (result is Resource.Success) {
                    // Agregamos "Todos" al principio de la lista que viene de la API
                    val apiCategories = result.data ?: emptyList()
                    // Capitalizamos la primera letra de cada categoría para que se vea bien
                    val formattedCategories = apiCategories.map { it.capitalizeWords() }

                    _categories.value = listOf("Todos") + formattedCategories
                }
                // Opcional: Manejar error de categorías (aunque no debería bloquear la app)
            }
        }
    }

    // --- 4. PROCESADOR DE INTENCIONES (Input Único) ---
    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ChangeCategory -> {
                _selectedCategory.value = intent.category
                loadProducts(intent.category)
            }

            is HomeIntent.IncreaseQuantity -> {
                viewModelScope.launch { addToCartUseCase(intent.product) }
            }

            is HomeIntent.DecreaseQuantity -> {
                viewModelScope.launch { decreaseQuantityUseCase(intent.productId) }
            }

            is HomeIntent.OnProductClick -> {
                //sendEffect(HomeEffect.NavigateToDetail(intent.productId))
                val selectedProduct =
                    state.value.products.find { it.product.id == intent.productId }
                _selectedProductForDetail.value = selectedProduct
            }

            is HomeIntent.DismissDetail -> {
                _selectedProductForDetail.value = null
            }

            is HomeIntent.OnCartClick -> {
                sendEffect(HomeEffect.NavigateToCart)
            }
        }
    }

    // Lógica privada (igual que antes, pero alimentando _rawProducts)
    private fun loadProducts(category: String) {
        val flow =
            if (category == "Todos") getProductsUseCase() else getProductsByCategoryUseCase(category)

        viewModelScope.launch {
            _rawProducts.value = Resource.Loading() // Emitir loading inmediato al cambiar
            flow.collect { result ->
                _rawProducts.value = result
            }
        }
    }

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}