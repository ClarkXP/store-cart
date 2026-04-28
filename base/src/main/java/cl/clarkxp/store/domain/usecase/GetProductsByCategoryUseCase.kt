package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.model.applyFeaturedSorting
import cl.clarkxp.store.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

//trae el listado de productos por categoria desde la API
class GetProductsByCategoryUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(category: String): Flow<Resource<List<Product>>> {
        return repository.getProductsByCategory(category).map { resource ->
            if (resource is Resource.Success) {
                //aplica el orden de los productos para poner al destacado al inicio
                val sortedData = resource.data?.applyFeaturedSorting() ?: emptyList()
                Resource.Success(sortedData)
            } else {
                resource
            }
        }
    }
}