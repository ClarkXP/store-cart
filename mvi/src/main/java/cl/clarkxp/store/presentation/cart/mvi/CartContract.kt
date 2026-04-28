package cl.clarkxp.store.presentation.cart.mvi

import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.presentation.mvi.UiEffect
import cl.clarkxp.store.presentation.mvi.UiIntent
import cl.clarkxp.store.presentation.mvi.UiState

data class CartState(
    val isLoading: Boolean = true,
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val showClearDialog: Boolean = false // Controla la visibilidad del diálogo de borrar
) : UiState

sealed class CartIntent : UiIntent {
    data class IncreaseQuantity(val item: CartItem) : CartIntent()
    data class DecreaseQuantity(val item: CartItem) : CartIntent()

    // Acciones del botón de borrar todo
    data object OnClearCartClick : CartIntent()
    data object OnConfirmClearCart : CartIntent()
    data object OnDismissClearDialog : CartIntent()

    data object OnCheckoutClick : CartIntent()
    data object OnBackClick : CartIntent()
}

sealed class CartEffect : UiEffect {
    data object NavigateBack : CartEffect()
    data class ShowSnackbar(val message: String) : CartEffect()
}