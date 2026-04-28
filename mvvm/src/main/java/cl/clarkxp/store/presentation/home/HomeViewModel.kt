package cl.clarkxp.store.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.usecase.AddToCartUseCase
import cl.clarkxp.store.domain.usecase.DecreaseQuantityUseCase
import cl.clarkxp.store.domain.usecase.GetCartItemCountUseCase
import cl.clarkxp.store.domain.usecase.GetCartUseCase
import cl.clarkxp.store.domain.usecase.GetProductsByCategoryUseCase
import cl.clarkxp.store.domain.usecase.GetProductsUseCase
import cl.clarkxp.store.presentation.home.model.ProductUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getProductsByCategoryUseCase: GetProductsByCategoryUseCase,
    private val getCartItemCountUseCase: GetCartItemCountUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val decreaseQuantityUseCase: DecreaseQuantityUseCase
) : ViewModel() {

    private val _productsState = MutableStateFlow<Resource<List<Product>>>(Resource.Loading())

    //    val state: StateFlow<Resource<List<Product>>> = _state
    val uiState: StateFlow<Resource<List<ProductUiModel>>> = combine(
        _productsState,
        getCartUseCase()
    ) { productsResource, cartState ->
        when (productsResource) {
            is Resource.Success -> {
                // mapa para asociar id con quantity actual del carrito
                val cartMap = cartState.items.associate { it.id to it.quantity }

                // Mapeamos Product a ProductUiModel enlazando quantity desde cartMap
                val uiModels = productsResource.data?.map { product ->
                    ProductUiModel(
                        product = product,
                        quantity = cartMap[product.id] ?: 0 // Si no está en el map es 0
                    )
                } ?: emptyList()

                Resource.Success(uiModels)
            }

            is Resource.Loading -> Resource.Loading()
            is Resource.Error -> Resource.Error(productsResource.message ?: "Error")
        }
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading()
    )


    val cartCount: StateFlow<Int> = getCartItemCountUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        loadProducts()
    }

    fun loadProducts(category: String? = null) {
        val flow = if (category == null || category == "Todos") {
            getProductsUseCase()
        } else {
            getProductsByCategoryUseCase(category)
        }

        flow.onEach { result ->
            _productsState.value = result
        }.launchIn(viewModelScope)
    }

    @Deprecated("Use increaseQuantity function instead")
    fun addToCart(product: Product) {
        viewModelScope.launch {
            addToCartUseCase(product)
        }
    }

    fun increaseQuantity(product: Product) {
        viewModelScope.launch { addToCartUseCase(product) }
    }

    fun decreaseQuantity(product: Product) {
        viewModelScope.launch { decreaseQuantityUseCase(product.id) }
    }
}