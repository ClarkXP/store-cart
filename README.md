# Store Architecture Lab

Un laboratorio de arquitectura Android que implementa una misma aplicación de e-commerce (catálogo de productos + carrito de compras) bajo dos patrones de presentación distintos — **MVI con Jetpack Compose** y **MVVM con XML** — compartiendo una capa de datos y dominio común.

El proyecto consume la [Fake Store API](https://fakestoreapi.com/) y persiste el carrito localmente con Room.

---

## Estructura del Proyecto

```
store-architecture-lab/
├── :base          ← Data + Domain compartido (Android Library)
├── :mvi           ← UI declarativa con Compose + MVI (Application)
└── :mvvm          ← UI tradicional con XML + MVVM (Application)
```

### `:base` — Núcleo compartido

Contiene la lógica de negocio y acceso a datos que ambos módulos de UI consumen como dependencia de proyecto.

| Capa | Contenido |
|------|-----------|
| **Domain** | Modelos puros (`Product`, `CartItem`), interfaces de repositorio (`CartRepository`, `ProductRepository`) y UseCases (`AddToCartUseCase`, `DecreaseQuantityUseCase`, `GetCartUseCase`, etc.) |
| **Data** | `CartRepositoryImpl` y `ProductRepositoryImpl`, Room (`AppDatabase`, `CartDao`, `CartItemEntity`), Retrofit (`FakeStoreApi`, `ProductDto`), Mappers |
| **DI** | Módulos Hilt (`DatabaseModule`, `NetworkModule`, `RepositoryModule`) |
| **Core** | Utilidades (`Resource<T>` sealed class, extensiones de String/Number, constantes) |

### `:mvi` — Presentación declarativa

- **UI:** Jetpack Compose con Material3
- **Patrón:** MVI (Model-View-Intent) con flujo de datos unidireccional estricto
- **Estado:** Un único `data class HomeState` inmutable por pantalla, expuesto como `StateFlow`
- **Intents:** Sealed class `HomeIntent` procesada secuencialmente vía `Channel<HomeIntent>`
- **Efectos:** Sealed class `HomeEffect` para navegación y eventos one-shot
- **Pantallas:** Home (catálogo con filtro por categoría), Detail, Cart
- **Carga de imágenes:** Coil Compose

### `:mvvm` — Presentación tradicional

- **UI:** XML Layouts con ViewBinding
- **Patrón:** MVVM clásico con múltiples `StateFlow` independientes
- **Navegación:** Android Navigation Component con SafeArgs
- **Componentes:** Fragments (`HomeFragment`, `CartFragment`, `ProductDetailFragment`) + Adapters con RecyclerView
- **Carga de imágenes:** Coil

---

## Stack Tecnológico

| Categoría | Tecnología |
|-----------|------------|
| Lenguaje | Kotlin |
| UI Declarativa | Jetpack Compose + Material3 |
| UI Tradicional | XML + ViewBinding + Material Components |
| DI | Hilt |
| Persistencia Local | Room |
| API REST | Retrofit + OkHttp + Gson |
| Asincronía | Coroutines + Flow (`StateFlow`, `SharedFlow`) |
| Imágenes | Coil |
| Testing | JUnit 4 + MockK + Kotlinx Coroutines Test + Robolectric + Room Testing |
| Gestión de dependencias | Gradle Version Catalog (`libs.versions.toml`) |

---

## Decisiones Arquitectónicas

### Persistencia Atómica en Room

El `CartDao` implementa operaciones atómicas para evitar condiciones de carrera en el patrón clásico *read-modify-write*:

```kotlin
// En lugar de: leer → sumar en memoria → guardar (propenso a race conditions)
// Usamos UPDATE directo en SQLite:
@Query("UPDATE cart_items SET quantity = quantity + 1 WHERE id = :productId")
suspend fun incrementQuantity(productId: Int): Int

// Envuelto en @Transaction para atomicidad completa:
@Transaction
suspend fun atomicAddToCart(item: CartItemEntity) {
    val rowsUpdated = incrementQuantity(item.id)
    if (rowsUpdated == 0) insertCartItem(item)  // Primera vez → INSERT
}
```

### Procesamiento Secuencial de Intents (MVI)

En el módulo `:mvi`, los Intents del usuario se encolan en un `Channel<HomeIntent>(UNLIMITED)` y se procesan uno a uno mediante `consumeAsFlow().collect { }` dentro del `viewModelScope`. Esto garantiza orden FIFO y previene que múltiples taps rápidos al botón "+" generen estados intermedios inconsistentes.

### Estado Combinado Reactivo

El `HomeViewModel` del módulo MVI combina 5 flujos independientes (`_rawProducts`, `getCartUseCase()`, `_selectedCategory`, `_categories`, `_selectedProductForDetail`) en un único `StateFlow<HomeState>` mediante `combine(...).stateIn(...)`. Cualquier cambio en la base de datos (Room emite un nuevo `Flow`) se propaga automáticamente hasta la UI.

---

## Trade-offs: MVI vs MVVM

| Aspecto | MVI (Compose) | MVVM (XML) |
|---------|---------------|------------|
| Estado | Objeto único inmutable (`HomeState`) | Múltiples `StateFlow` independientes |
| Flujo de datos | Estrictamente unidireccional | Puede volverse bidireccional |
| Consistencia | Imposible tener estados contradictorios | Posible (ej: loading + error simultáneo) |
| Depuración | Determinista — cada Intent produce un Estado | Difícil rastrear qué Flow cambió primero |
| Boilerplate | Requiere `Intent`, `State` y `Effect` por pantalla | Más directo, menos clases |
| Re-renders | Un cambio en cualquier propiedad notifica a toda la UI | Cada Flow actualiza solo su observador |
| Curva de aprendizaje | Mayor (Compose + MVI + Channels) | Menor (estándar de la industria) |

---

## Testing

El proyecto incluye tests distribuidos según la responsabilidad de cada módulo:

### `:base` — Tests de Dominio
- **`GetCartUseCaseTest`** — Valida que el cálculo del monto total (`precio × cantidad`) sea correcto y que un carrito vacío retorne `0`.
- **`GetProductsUseCaseTest`** — Verifica la transformación de datos de la API.
- **`MapperTest`** — Asegura la correcta conversión entre entidades, DTOs y modelos de dominio.

### `:mvi` — Tests de Presentación e Integración
- **`HomeViewModelTest`** — Tests unitarios del ViewModel MVI con UseCases mockeados. Valida que el `StateFlow<HomeState>` combine correctamente productos y carrito, y que los estados de error se propaguen.
- **`MviToRoomIntegrationTest`** — Test de integración que valida el flujo completo: `UseCase → Repository → DAO (@Transaction) → SQLite → Flow reactivo`. Usa **Robolectric** con base de datos **Room in-memory** y `runBlocking` (para compatibilidad con las transacciones de Room). Incluye 4 escenarios:
  1. Incremento atómico ×3 con verificación en DB y Flow
  2. Decremento con eliminación automática al llegar a 0
  3. Secuencia mixta de incrementos y decrementos
  4. Múltiples productos independientes con integridad por ID

### `:mvvm` — Tests de Presentación
- **`HomeViewModelTest`** — Tests unitarios del ViewModel MVVM. Valida la carga exitosa de productos con cantidades del carrito y el manejo de errores de red.

### Ejecutar los tests
```bash
# Todos los tests unitarios
./gradlew test

# Solo tests del módulo MVI
./gradlew :mvi:testDebugUnitTest

# Solo tests del módulo base
./gradlew :base:testDebugUnitTest
```

> **Nota:** Requiere JDK 17. Android Studio usa su JDK embebido, pero si ejecutas desde terminal, asegúrate de que `JAVA_HOME` apunte a una versión compatible.

---

## Setup

1. Clonar el repositorio
2. Abrir con **Android Studio** (Ladybug o superior recomendado)
3. Sincronizar Gradle (`File → Sync Project with Gradle Files`)
4. Seleccionar la Run Configuration deseada:
   - **`mvi`** → Lanza la versión Compose
   - **`mvvm`** → Lanza la versión XML
5. Ejecutar en emulador o dispositivo con **API 26+**

---

## Dependencias centralizadas

Todas las versiones y librerías están definidas en [`gradle/libs.versions.toml`](gradle/libs.versions.toml) para facilitar el mantenimiento y evitar duplicación de versiones entre módulos.
