package cl.clarkxp.store.presentation.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.clarkxp.store.core.extensions.capitalizeWords
import cl.clarkxp.store.mvi.R
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.presentation.detail.DetailContent
import cl.clarkxp.store.presentation.detail.mvi.DetailIntent
import cl.clarkxp.store.presentation.detail.mvi.DetailState
import cl.clarkxp.store.presentation.home.model.ProductUiModel
import cl.clarkxp.store.presentation.home.mvi.HomeEffect
import cl.clarkxp.store.presentation.home.mvi.HomeIntent
import cl.clarkxp.store.presentation.home.mvi.HomeState
import cl.clarkxp.store.presentation.home.ui.FeaturedProductItem
import cl.clarkxp.store.presentation.home.ui.ProductItem
import cl.clarkxp.store.presentation.ui.theme.StoreTheme
import kotlinx.coroutines.launch

/**
 * COMPONENTE STATEFUL: Conecta con el ViewModel y maneja Efectos.
 */
@Composable
fun HomeScreen(
    onNavigate: (HomeEffect) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Manejo de Efectos de una sola vez (Navegación, Errores)
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect -> onNavigate(effect) }
    }

    // Delegamos la UI al componente Stateless
    HomeContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

/**
 * COMPONENTE STATELESS: Solo recibe datos y emite eventos. Ideal para Preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // 1. ESTADO DE ANIMACIÓN (UI Logic)
    // Calculamos el radio del blur basándonos en si hay un producto seleccionado en el estado
    val isSheetOpen = state.selectedProductForDetail != null
    val blurRadius by animateDpAsState(
        targetValue = if (isSheetOpen) 15.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "BlurAnimation"
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                onCategoryClick = { category ->
                    // Lógica para enviar el intent correcto ("Todos" vs "electronics")
                    val query = if (category == "Todos") "Todos" else category.lowercase()
                    onIntent(HomeIntent.ChangeCategory(query))
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .blur(radius = blurRadius),
            topBar = {
                HomeTopBar(
                    cartCount = state.cartCount,
                    selectedCategory = state.selectedCategory,
                    scrollBehavior = scrollBehavior,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onCartClick = { onIntent(HomeIntent.OnCartClick) }
                )
            }
        ) { paddingValues ->

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ProductGrid(
                    products = state.products,
                    contentPadding = paddingValues,
                    onIntent = onIntent
                )
            }
        }
    }
    if (state.selectedProductForDetail != null) {

        ModalBottomSheet(
            onDismissRequest = { onIntent(HomeIntent.DismissDetail) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
            scrimColor = Color.Black.copy(alpha = 0.2f)
        ) {
            // ¡REUTILIZAMOS TU PANTALLA DE DETALLE AQUÍ!
            // Adaptamos el HomeState al DetailState que espera el componente

            DetailContent(
                state = DetailState(
                    isLoading = false,
                    uiModel = state.selectedProductForDetail
                ),
                onIntent = { detailIntent ->
                    // Aquí puenteamos los intents del Detalle hacia el ViewModel del Home
                    when (detailIntent) {
                        is DetailIntent.IncreaseQuantity ->
                            onIntent(HomeIntent.IncreaseQuantity(detailIntent.product))

                        is DetailIntent.DecreaseQuantity ->
                            onIntent(HomeIntent.DecreaseQuantity(detailIntent.productId))

                        is DetailIntent.OnBackClick ->
                            onIntent(HomeIntent.DismissDetail)

                        else -> {}
                    }
                }
            )

        }
    }

}

// --- SUB-COMPONENTES PARA LIMPIEZA Y REUTILIZACIÓN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    cartCount: Int,
    selectedCategory: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val appName = stringResource(R.string.app_name)
    val title = if (selectedCategory == "Todos") appName else selectedCategory.capitalizeWords()

    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menú")
            }
        },
        actions = {

            BadgedBox(
                badge = {
                    if (cartCount > 0) {
                        Badge(modifier = Modifier.offset(x = (-10).dp, y = 8.dp)) {
                            Text(
                                text = cartCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                },
                modifier = Modifier.padding(end = 4.dp)// Un poco de margen extra a la derecha
            ) {
                // El botón queda adentro
                IconButton(onClick = onCartClick) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Carrito"
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun HomeDrawerContent(
    categories: List<String>,
    selectedCategory: String,
    onCategoryClick: (String) -> Unit
) {
    ModalDrawerSheet {
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Categorías",
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(16.dp))

        categories.forEach { category ->
            NavigationDrawerItem(
                label = { Text(category, style = MaterialTheme.typography.bodyLarge) },
                selected = selectedCategory.equals(category, ignoreCase = true),
                onClick = { onCategoryClick(category) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.medium
            )
        }
    }
}

@Composable
fun ProductGrid(
    products: List<ProductUiModel>,
    contentPadding: PaddingValues,
    onIntent: (HomeIntent) -> Unit
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("No se encontraron productos", color = MaterialTheme.colorScheme.outline)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. HEADER (Producto Destacado - Ocupa 2 columnas)
            item(span = { GridItemSpan(2) }) {
                FeaturedProductItem(
                    uiModel = products.first(),
                    onIntent = onIntent
                )
            }

            // 2. LISTA RESTANTE (Excluyendo el primero)
            if (products.size > 1) {
                items(products.drop(1)) { productUi ->
                    ProductItem(
                        uiModel = productUi,
                        onIntent = onIntent
                    )
                }
            }

            // Espacio final para que el último item no quede pegado al borde
            item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// --- PREVIEW ---

@Preview(showSystemUi = true)
@Composable
fun PreviewHomeContent() {
    val mockProduct = Product(1, "TV Samsung 55", 350000.0, "Desc", "Electronics", "", 4.5, 100)
    val mockUi = ProductUiModel(mockProduct, 0)

    val mockState = HomeState(
        isLoading = false,
        cartCount = 12,
        products = listOf(
            mockUi.copy(product = mockProduct.copy(title = "Producto Destacado")),
            mockUi,
            mockUi.copy(quantity = 2),
            mockUi
        ),
        categories = listOf("Todos", "Electronics", "Jewelery")
    )

    StoreTheme {
        HomeContent(
            state = mockState,
            onIntent = {}
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewHomeContentWithDetail() {
    val mockProduct = Product(1, "TV Samsung 55", 350000.0, "Desc", "Electronics", "", 4.5, 100)
    val mockUi = ProductUiModel(mockProduct, 0)

    val mockState = HomeState(
        isLoading = false,
        cartCount = 3,
        products = listOf(
            mockUi.copy(product = mockProduct.copy(title = "Producto Destacado")),
            mockUi,
            mockUi.copy(quantity = 2),
            mockUi
        ),
        categories = listOf("Todos", "Electronics", "Jewelery"),
        selectedProductForDetail = mockUi
    )

    StoreTheme {
        HomeContent(
            state = mockState,
            onIntent = {}
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewHomeContentEmpty() {
    val mockState = HomeState(
        isLoading = false,
        cartCount = 12,
        products = emptyList(),
        categories = listOf("Todos", "Electronics", "Jewelery")
    )

    StoreTheme {
        HomeContent(
            state = mockState,
            onIntent = {}
        )
    }
}