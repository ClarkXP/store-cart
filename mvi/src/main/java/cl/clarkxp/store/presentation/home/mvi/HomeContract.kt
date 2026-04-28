package cl.clarkxp.store.presentation.home.mvi

import cl.clarkxp.store.presentation.home.model.ProductUiModel
import cl.clarkxp.store.presentation.mvi.UiEffect
import cl.clarkxp.store.presentation.mvi.UiIntent
import cl.clarkxp.store.presentation.mvi.UiState
import cl.clarkxp.store.domain.model.Product

// 1. EL ESTADO ÚNICO
data class HomeState(
    val isLoading: Boolean = true, // Carga inicial
    val products: List<ProductUiModel> = emptyList(), // Lista combinada (API + DB)
    val cartCount: Int = 0, // Badge
    val selectedCategory: String = "Todos",
    val categories: List<String> = listOf("Todos"),
    val selectedProductForDetail: ProductUiModel? = null,
    val error: String? = null,
) : UiState

// 2. LAS INTENCIONES (Acciones del usuario)
sealed class HomeIntent : UiIntent {
    // Carga inicial o cambio de filtro
    data class ChangeCategory(val category: String) : HomeIntent()

    // Acciones de carrito (Reemplazan a tus antiguos métodos sueltos)
    data class IncreaseQuantity(val product: Product) : HomeIntent()
    data class DecreaseQuantity(val productId: Int) : HomeIntent()

    // Navegación
    data class OnProductClick(val productId: Int) : HomeIntent()
    data object DismissDetail : HomeIntent()
    data object OnCartClick : HomeIntent()
}

// 3. LOS EFECTOS (Navegación y Errores)
sealed class HomeEffect : UiEffect {
    data class NavigateToDetail(val productId: Int) : HomeEffect()
    data object NavigateToCart : HomeEffect()
    data class ShowError(val message: String) : HomeEffect()
}