package cl.clarkxp.store.presentation.home.model

import cl.clarkxp.store.domain.model.Product

data class ProductUiModel(
    val product: Product,
    val quantity: Int = 0
)
