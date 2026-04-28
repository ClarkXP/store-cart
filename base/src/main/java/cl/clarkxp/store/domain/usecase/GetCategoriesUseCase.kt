package cl.clarkxp.store.domain.usecase

import cl.clarkxp.store.domain.repository.ProductRepository
import javax.inject.Inject

//Trae las categorias para poblar el navigation drawer
class GetCategoriesUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke() = repository.getCategories()
}