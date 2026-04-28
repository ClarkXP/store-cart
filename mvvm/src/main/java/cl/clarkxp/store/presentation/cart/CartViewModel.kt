package cl.clarkxp.store.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.domain.usecase.CartState
import cl.clarkxp.store.domain.usecase.ClearCartUseCase
import cl.clarkxp.store.domain.usecase.GetCartUseCase
import cl.clarkxp.store.domain.usecase.UpdateCartQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    getCartUseCase: GetCartUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val clearCartUseCase: ClearCartUseCase
) : ViewModel() {

    val state: StateFlow<CartState> = getCartUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CartState()
        )

    fun increaseQuantity(item: CartItem) {
        viewModelScope.launch {
            updateCartQuantityUseCase(item, item.quantity + 1)
        }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            updateCartQuantityUseCase(item, item.quantity - 1)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            clearCartUseCase()
        }
    }
}