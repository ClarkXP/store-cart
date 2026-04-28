package cl.clarkxp.store.data.mapper

import cl.clarkxp.store.data.local.entity.CartItemEntity
import cl.clarkxp.store.data.remote.dto.ProductDto
import cl.clarkxp.store.data.remote.dto.RatingDto
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest {

    @Test
    fun `ProductDto toDomain mapea correctamente todos los campos`() {
        // GIVEN: El DTO de un Product simulado de la API
        val dto = ProductDto(
            id = 1,
            title = "Test Title",
            price = 999.0,
            description = "Desc",
            category = "Cat",
            image = "url",
            rating = RatingDto(rate = 4.5, count = 10)
        )

        // WHEN: Lo convertimos
        val domain = dto.toDomain()

        // THEN: Verificamos que nada se perdió en la traducción
        assertEquals(dto.id, domain.id)
        assertEquals(dto.title, domain.title)
        assertEquals(dto.description, domain.description)
        assertEquals(dto.category, domain.category)
        assertEquals(dto.image, domain.image)
        assertEquals(dto.price, domain.price, 0.0)
        assertEquals(dto.rating.rate, domain.rating, 0.0)
        assertEquals(dto.rating.count, domain.ratingCount)
    }

    @Test
    fun `CartItemEntity toDomain mantiene la integridad de los datos`() {
        // GIVEN
        val entity = CartItemEntity(1, "Title", 100.0, "img", 5)

        // WHEN
        val domain = entity.toDomain()

        // THEN
        assertEquals(1, domain.id)
        assertEquals(5, domain.quantity)
        assertEquals(100.0, domain.price, 0.0)
    }
}