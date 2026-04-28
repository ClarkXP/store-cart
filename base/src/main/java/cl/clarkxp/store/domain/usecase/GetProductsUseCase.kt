package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.model.applyFeaturedSorting
import cl.clarkxp.store.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

//Trae el listado de todos los productos sin filtrar
class GetProductsUseCase @Inject constructor(private val repository: ProductRepository) {
    operator fun invoke(): Flow<Resource<List<Product>>> {
        return repository.getProducts().map { resource ->
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