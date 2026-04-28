package cl.clarkxp.store.data.repository

import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.data.mapper.toDomain
import cl.clarkxp.store.data.remote.api.FakeStoreApi
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: FakeStoreApi
) : ProductRepository {

    override fun getProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getProducts()
            val products = response.map { it.toDomain() }
            emit(Resource.Success(products))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión: Revisa tu internet"))
        } catch (e: Exception) {
            emit(Resource.Error("Error inesperado: ${e.localizedMessage}"))
        }
    }

    override fun getProductById(id: Int): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getProductById(id)
            emit(Resource.Success(response.toDomain()))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión"))
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage}"))
        }
    }

    override fun getProductsByCategory(category: String): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getProductsByCategory(category)
            emit(Resource.Success(response.map { it.toDomain() }))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión"))
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage}"))
        }
    }

    override fun getCategories(): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading())
        try {
            val response = api.getCategories() // Llamada a la API
            emit(Resource.Success(response))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión"))
        } catch (e: Exception) {
            emit(Resource.Error("Error: ${e.localizedMessage}"))
        }
    }
}