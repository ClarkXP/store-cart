package cl.clarkxp.store.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.domain.usecase.CartState
import cl.clarkxp.store.domain.usecase.ClearCartUseCase
import cl.clarkxp.store.domain.usecase.DecreaseQuantityUseCase
import cl.clarkxp.store.domain.usecase.GetCartUseCase
import cl.clarkxp.store.domain.usecase.IncreaseQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    getCartUseCase: GetCartUseCase,
    private val increaseQuantityUseCase: IncreaseQuantityUseCase,
    private val decreaseQuantityUseCase: DecreaseQuantityUseCase,
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
            // Operación atómica: UPDATE SET quantity = quantity + 1
            increaseQuantityUseCase(item.id)
        }
    }

    fun decreaseQuantity(item: CartItem) {
        viewModelScope.launch {
            // Operación atómica: decrementa o elimina si llega a 0
            decreaseQuantityUseCase(item.id)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            clearCartUseCase()
        }
    }
}