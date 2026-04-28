package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//Trae el detalle de un producto por su id
class GetProductDetailUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(id: Int): Flow<Resource<Product>> {
        return repository.getProductById(id)
    }
}