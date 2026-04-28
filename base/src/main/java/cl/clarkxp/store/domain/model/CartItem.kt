package cl.clarkxp.store.domain.model

data class CartItem(
    val id: Int,
    val title: String,
    val price: Double,
    val image: String,
    val quantity: Int
)