package cl.clarkxp.store.domain.repository

import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<Resource<List<Product>>>
    fun getProductById(id: Int): Flow<Resource<Product>>
    fun getProductsByCategory(category: String): Flow<Resource<List<Product>>>
    fun getCategories(): Flow<Resource<List<String>>>
}