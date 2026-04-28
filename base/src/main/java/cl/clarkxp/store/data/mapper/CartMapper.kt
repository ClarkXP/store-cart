package cl.clarkxp.store.data.mapper

import cl.clarkxp.store.data.local.entity.CartItemEntity
import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.domain.model.Product

//para leer items desde la DB
fun CartItemEntity.toDomain(): CartItem {
    return CartItem(
        id = id,
        title = title,
        price = price,
        image = image,
        quantity = quantity
    )
}

//para guardar items en la DB
fun CartItem.toEntity(): CartItemEntity {
    return CartItemEntity(
        id = id,
        title = title,
        price = price,
        image = image,
        quantity = quantity
    )
}

// Helper para convertir un Producto (del Home) cuando se agrega al carrito
fun Product.toCartEntity(quantity: Int = 1): CartItemEntity {
    return CartItemEntity(
        id = id,
        title = title,
        price = price,
        image = image,
        quantity = quantity
    )
}

fun Product.toCartItem(quantity: Int): CartItem{
    return CartItem(
        id = id,
        title = title,
        price = price,
        image = image,
        quantity = quantity
    )
}