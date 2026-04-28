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
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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
    private val _rawProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())
    private val _selectedCategory = MutableStateFlow("Todos")
    private val _selectedProductForDetail = MutableStateFlow<ProductUiModel?>(null)
    private val _categories = MutableStateFlow<List<String>>(listOf("Todos"))

    // --- 2. CANALES (Efectos e Intents) ---
    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    private val intentChannel = Channel<HomeIntent>(Channel.UNLIMITED)

    // --- 3. ESTADO UI MVI ---
    val state: StateFlow<HomeState> = combine(
        _rawProducts,
        getCartUseCase(),
        _selectedCategory,
        _categories,
        _selectedProductForDetail
    ) { productsRes, cartState, category, categoriesList, selectedProduct ->

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
        processIntents()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { result ->
                if (result is Resource.Success) {
                    val apiCategories = result.data ?: emptyList()
                    val formattedCategories = apiCategories.map { it.capitalizeWords() }
                    _categories.value = listOf("Todos") + formattedCategories
                }
            }
        }
    }

    // --- 4. PROCESADOR DE INTENCIONES (Input Único Secuencial) ---
    private fun processIntents() {
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    is HomeIntent.ChangeCategory -> {
                        _selectedCategory.value = intent.category
                        loadProducts(intent.category)
                    }
                    is HomeIntent.IncreaseQuantity -> {
                        // Al procesar secuencialmente con atomicidad en BD, prevenimos condiciones de carrera
                        addToCartUseCase(intent.product)
                    }
                    is HomeIntent.DecreaseQuantity -> {
                        decreaseQuantityUseCase(intent.productId)
                    }
                    is HomeIntent.OnProductClick -> {
                        val selectedProduct = state.value.products.find { it.product.id == intent.productId }
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
        }
    }

    fun onIntent(intent: HomeIntent) {
        // Enviamos al canal en lugar de procesar directamente
        val result = intentChannel.trySend(intent)
        if (result.isFailure) {
            // Manejo opcional si el canal se cierra/falla
        }
    }

    private fun loadProducts(category: String) {
        val flow = if (category == "Todos") getProductsUseCase() else getProductsByCategoryUseCase(category)

        viewModelScope.launch {
            _rawProducts.value = Resource.Loading() 
            flow.collect { result ->
                _rawProducts.value = result
            }
        }
    }

    private fun sendEffect(effect: HomeEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}