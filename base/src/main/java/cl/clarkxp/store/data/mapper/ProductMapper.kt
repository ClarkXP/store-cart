package cl.clarkxp.store.data.mapper

import cl.clarkxp.store.data.remote.dto.ProductDto
import cl.clarkxp.store.domain.model.Product

fun ProductDto.toDomain(): Product{
    return Product(
        id = id,
        title = title,
        price = price,
        description = description,
        category = category,
        image = image,
        rating = rating.rate,
        ratingCount = rating.count
    )
}