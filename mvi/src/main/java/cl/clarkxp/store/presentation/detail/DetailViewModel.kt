package cl.clarkxp.store.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.usecase.*
import cl.clarkxp.store.presentation.detail.mvi.DetailEffect
import cl.clarkxp.store.presentation.detail.mvi.DetailIntent
import cl.clarkxp.store.presentation.detail.mvi.DetailState
import cl.clarkxp.store.presentation.home.model.ProductUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, // Para recibir argumentos de navegación automáticamente
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val decreaseQuantityUseCase: DecreaseQuantityUseCase
) : ViewModel() {

    // Recuperamos el ID desde la ruta de navegación (definido en MainActivity como "productId")
    private val productId: Int = checkNotNull(savedStateHandle["productId"])

    // Estado interno del producto
    private val _productRes = MutableStateFlow<Resource<Product>>(Resource.Loading())

    // Canal de efectos
    private val _effect = Channel<DetailEffect>()
    val effect = _effect.receiveAsFlow()

    // ESTADO UI COMBINADO
    val state: StateFlow<DetailState> = combine(
        _productRes,
        getCartUseCase()
    ) { productRes, cartState ->
        val uiModel = if (productRes is Resource.Success && productRes.data != null) {
            val product = productRes.data!!
            val cartItem = cartState.items.find { it.id == product.id }
            ProductUiModel(product, cartItem?.quantity ?: 0)
        } else {
            null
        }

        DetailState(
            isLoading = productRes is Resource.Loading,
            uiModel = uiModel,
            error = if (productRes is Resource.Error) productRes.message else null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailState()
    )

    init {
        loadProduct(productId)
    }

    fun onIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.LoadProduct -> loadProduct(intent.productId)
            is DetailIntent.IncreaseQuantity -> {
                viewModelScope.launch { addToCartUseCase(intent.product) }
            }
            is DetailIntent.DecreaseQuantity -> {
                viewModelScope.launch { decreaseQuantityUseCase(intent.productId) }
            }
            is DetailIntent.OnBackClick -> {
                viewModelScope.launch { _effect.send(DetailEffect.NavigateBack) }
            }
        }
    }

    private fun loadProduct(id: Int) {
        getProductDetailUseCase(id).onEach {
            _productRes.value = it
        }.launchIn(viewModelScope)
    }
}