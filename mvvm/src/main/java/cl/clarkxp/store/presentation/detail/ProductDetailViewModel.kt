package cl.clarkxp.store.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.usecase.AddToCartUseCase
import cl.clarkxp.store.domain.usecase.DecreaseQuantityUseCase
import cl.clarkxp.store.domain.usecase.GetCartUseCase
import cl.clarkxp.store.domain.usecase.GetProductDetailUseCase
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
class ProductDetailViewModel @Inject constructor(
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val decreaseQuantityUseCase: DecreaseQuantityUseCase
) : ViewModel() {

    private val _productRaw = MutableStateFlow<Resource<Product>>(Resource.Loading())

    val state: StateFlow<Resource<ProductUiModel>> = combine(
        _productRaw,
        getCartUseCase()
    ) { productRes, cartState ->
        when (productRes) {
            is Resource.Success -> {
                val product = productRes.data!!

                // Buscamos si este producto está en el carro
                val cartItem = cartState.items.find { it.id == product.id }
                val qty = cartItem?.quantity ?: 0

                Resource.Success(ProductUiModel(product, qty))
            }

            is Resource.Loading -> Resource.Loading()
            is Resource.Error -> Resource.Error(productRes.message ?: "Error")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Resource.Loading()
    )

    fun loadProduct(id: Int) {
        getProductDetailUseCase(id).onEach {
            _productRaw.value = it
        }.launchIn(viewModelScope)
    }



    fun increaseQuantity(product: Product) {
        viewModelScope.launch {
            addToCartUseCase(product)
        }
    }

    fun decreaseQuantity(productId: Int) {
        viewModelScope.launch {
            decreaseQuantityUseCase(productId)
        }
    }
}