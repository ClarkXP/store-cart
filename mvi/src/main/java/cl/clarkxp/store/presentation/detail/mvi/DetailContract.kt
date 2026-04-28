package cl.clarkxp.store.presentation.detail.mvi

import cl.clarkxp.store.presentation.home.model.ProductUiModel
import cl.clarkxp.store.presentation.mvi.UiEffect
import cl.clarkxp.store.presentation.mvi.UiIntent
import cl.clarkxp.store.presentation.mvi.UiState
import cl.clarkxp.store.domain.model.Product

// 1. ESTADO: ¿Qué mostramos?
data class DetailState(
    val isLoading: Boolean = true,
    val uiModel: ProductUiModel? = null, // Producto + Cantidad
    val error: String? = null
) : UiState

// 2. INTENCIONES: ¿Qué hace el usuario?
sealed class DetailIntent : UiIntent {
    data class LoadProduct(val productId: Int) : DetailIntent()
    data class IncreaseQuantity(val product: Product) : DetailIntent()
    data class DecreaseQuantity(val productId: Int) : DetailIntent()
    data object OnBackClick : DetailIntent()
}

// 3. EFECTOS: Navegación
sealed class DetailEffect : UiEffect {
    data object NavigateBack : DetailEffect()
}