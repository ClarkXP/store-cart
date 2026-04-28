package cl.clarkxp.store.domain.model

data class Product(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String,
    val rating: Double,
    val ratingCount: Int
)

fun List<Product>.applyFeaturedSorting(): List<Product> {
    if (this.isEmpty()) return this

    // destacado = MAX(rate * count)
    val featuredProduct = this.maxByOrNull { it.rating * it.ratingCount }

    return if (featuredProduct != null) {
        // Crear nueva lista: [Destacado, ...Resto]
        val sortedList = ArrayList<Product>()
        sortedList.add(featuredProduct)
        sortedList.addAll(this.filter { it.id != featuredProduct.id })
        sortedList
    } else {
        this
    }
}
