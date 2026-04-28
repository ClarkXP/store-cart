package cl.clarkxp.store.presentation.home

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cl.clarkxp.store.data.local.AppDatabase
import cl.clarkxp.store.data.local.dao.CartDao
import cl.clarkxp.store.data.local.entity.CartItemEntity
import cl.clarkxp.store.data.repository.CartRepositoryImpl
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.domain.usecase.AddToCartUseCase
import cl.clarkxp.store.domain.usecase.DecreaseQuantityUseCase
import cl.clarkxp.store.domain.usecase.GetCartItemCountUseCase
import cl.clarkxp.store.domain.usecase.GetCartUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * # Test de Integración: MVI Intent → Domain → Repository → Room DB
 *
 * ## Propósito
 * Valida el flujo completo desde que se simula la acción de un Intent MVI
 * (IncreaseQuantity / DecreaseQuantity) hasta que el dato persiste en Room,
 * y que la reactividad del Flow funciona de punta a punta.
 *
 * ## Estrategia
 * Usamos `runBlocking` en lugar de `runTest` para evitar la incompatibilidad
 * conocida entre Room `@Transaction` (que internamente usa `runBlocking` con
 * un `TransactionExecutor`) y el `TestCoroutineScheduler` de `kotlinx-coroutines-test`.
 *
 * La capa de red/API **no participa** en este test: verificamos exclusivamente
 * la cadena UseCase → Repository → DAO → SQLite → Flow reactivo.
 *
 * ## Componentes reales (no mockeados)
 * - Room In-Memory Database
 * - CartDao (generado por Room)
 * - CartRepositoryImpl
 * - AddToCartUseCase / DecreaseQuantityUseCase / GetCartUseCase / GetCartItemCountUseCase
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MviToRoomIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var cartDao: CartDao
    private lateinit var cartRepository: CartRepositoryImpl

    // UseCases reales con repositorio y base de datos reales
    private lateinit var addToCartUseCase: AddToCartUseCase
    private lateinit var decreaseQuantityUseCase: DecreaseQuantityUseCase
    private lateinit var getCartUseCase: GetCartUseCase
    private lateinit var getCartItemCountUseCase: GetCartItemCountUseCase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        cartDao = database.cartDao()
        cartRepository = CartRepositoryImpl(cartDao)

        addToCartUseCase = AddToCartUseCase(cartRepository)
        decreaseQuantityUseCase = DecreaseQuantityUseCase(cartRepository)
        getCartUseCase = GetCartUseCase(cartRepository)
        getCartItemCountUseCase = GetCartItemCountUseCase(cartRepository)
    }

    @After
    fun teardown() {
        database.close()
    }

    // ─────────────────────────────────────────────────────────────
    // Test 1: Flujo atómico de incremento — simula IncreaseQuantity
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `Intent IncreaseQuantity x3 persiste atomicamente en Room y el Flow reactivo refleja cantidad 3`() = runBlocking {
        val product = Product(1, "TV 4K", 500.0, "Desc", "Electrónica", "img", 4.5, 100)

        // GIVEN: El carrito inicia vacío
        val initialCount = getCartItemCountUseCase().first()
        assertEquals("Carrito debe iniciar vacío", 0, initialCount)

        // WHEN: Simulamos 3 intents rápidos de IncreaseQuantity (lo que haría el ViewModel.onIntent)
        addToCartUseCase(product) // Intent 1: inserta con quantity=1
        addToCartUseCase(product) // Intent 2: incrementa a quantity=2
        addToCartUseCase(product) // Intent 3: incrementa a quantity=3

        // THEN 1: Verificamos directamente en la DB que la operación atómica fue correcta
        val dbQuantity = cartDao.getQuantityById(product.id)
        assertEquals("Room debe registrar cantidad 3 atómicamente", 3, dbQuantity)

        // THEN 2: Verificamos que el Flow reactivo de Room emite el estado correcto
        val cartState = getCartUseCase().first()
        assertEquals("El Flow debe contener exactamente 1 producto", 1, cartState.items.size)
        assertEquals("El item del carrito debe tener quantity=3", 3, cartState.items[0].quantity)
        assertEquals("El total debe ser precio * cantidad", 1500.0, cartState.totalAmount, 0.01)

        // THEN 3: Verificamos el conteo total a través del Flow de conteo
        val totalCount = getCartItemCountUseCase().first()
        assertEquals("El badge del carrito debe mostrar 3", 3, totalCount)
    }

    // ─────────────────────────────────────────────────────────────
    // Test 2: Flujo de decremento — simula DecreaseQuantity
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `Intent DecreaseQuantity reduce cantidad y elimina el item cuando llega a 0`() = runBlocking {
        val product = Product(2, "Laptop Pro", 1200.0, "Desc", "Electrónica", "img", 4.8, 50)

        // GIVEN: Agregamos 2 unidades al carrito
        addToCartUseCase(product)
        addToCartUseCase(product)
        assertEquals("Precondición: debe haber 2 en DB", 2, cartDao.getQuantityById(product.id))

        // WHEN: Decrementamos una vez (simula Intent DecreaseQuantity)
        decreaseQuantityUseCase(product.id)

        // THEN: Debe quedar 1
        val afterFirst = cartDao.getQuantityById(product.id)
        assertEquals("Tras primer decremento debe quedar 1", 1, afterFirst)

        // Verificamos la reactividad del Flow
        val cartState = getCartUseCase().first()
        assertEquals("Flow debe reflejar quantity=1", 1, cartState.items[0].quantity)

        // WHEN: Decrementamos otra vez (quantity llega a 0, debe eliminarse)
        decreaseQuantityUseCase(product.id)

        // THEN: El item debe desaparecer completamente de la DB
        val afterSecond = cartDao.getQuantityById(product.id)
        assertNull("Cuando quantity llega a 0, el item debe eliminarse de Room", afterSecond)

        // El Flow reactivo debe reflejar carrito vacío
        val emptyCart = getCartUseCase().first()
        assertEquals("Flow del carrito debe estar vacío", 0, emptyCart.items.size)
        assertEquals("El total debe ser 0", 0.0, emptyCart.totalAmount, 0.01)
    }

    // ─────────────────────────────────────────────────────────────
    // Test 3: Atomicidad bajo concurrencia simulada
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `Multiples incrementos y decrementos secuenciales mantienen consistencia atomica`() = runBlocking {
        val product = Product(3, "Monitor UW", 800.0, "Desc", "Electrónica", "img", 4.2, 75)

        // Simulamos una secuencia rápida de intents mixtos
        addToCartUseCase(product)    // +1 → qty=1
        addToCartUseCase(product)    // +1 → qty=2
        addToCartUseCase(product)    // +1 → qty=3
        decreaseQuantityUseCase(product.id)  // -1 → qty=2
        addToCartUseCase(product)    // +1 → qty=3
        decreaseQuantityUseCase(product.id)  // -1 → qty=2
        decreaseQuantityUseCase(product.id)  // -1 → qty=1

        // THEN: La DB debe reflejar el resultado final correcto
        val finalQty = cartDao.getQuantityById(product.id)
        assertEquals("Tras secuencia mixta, quantity debe ser 1", 1, finalQty)

        // El Flow reactivo debe concordar con la DB
        val cartState = getCartUseCase().first()
        assertEquals("Flow debe mostrar 1 item", 1, cartState.items.size)
        assertEquals("Flow quantity debe ser 1", 1, cartState.items[0].quantity)
        assertEquals("Total = 800 * 1", 800.0, cartState.totalAmount, 0.01)
    }

    // ─────────────────────────────────────────────────────────────
    // Test 4: Múltiples productos independientes
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `Agregar multiples productos distintos al carrito mantiene integridad por ID`() = runBlocking {
        val productA = Product(10, "Teclado", 50.0, "Desc", "Accesorios", "img", 4.0, 200)
        val productB = Product(20, "Mouse", 30.0, "Desc", "Accesorios", "img", 4.3, 150)

        // Agregamos diferentes cantidades de cada producto
        addToCartUseCase(productA)  // A: qty=1
        addToCartUseCase(productA)  // A: qty=2
        addToCartUseCase(productB)  // B: qty=1

        // Verificamos en DB
        assertEquals("Producto A debe tener qty=2", 2, cartDao.getQuantityById(productA.id))
        assertEquals("Producto B debe tener qty=1", 1, cartDao.getQuantityById(productB.id))

        // Verificamos el Flow reactivo del carrito completo
        val cartState = getCartUseCase().first()
        assertEquals("Debe haber 2 productos distintos", 2, cartState.items.size)

        val itemA = cartState.items.find { it.id == productA.id }
        val itemB = cartState.items.find { it.id == productB.id }

        assertNotNull("Item A debe existir en el Flow", itemA)
        assertNotNull("Item B debe existir en el Flow", itemB)
        assertEquals(2, itemA!!.quantity)
        assertEquals(1, itemB!!.quantity)

        // Total: (50*2) + (30*1) = 130
        assertEquals("Total debe ser 130.0", 130.0, cartState.totalAmount, 0.01)

        // Badge total: 2 + 1 = 3
        val totalCount = getCartItemCountUseCase().first()
        assertEquals("Badge total debe ser 3", 3, totalCount)
    }
}
