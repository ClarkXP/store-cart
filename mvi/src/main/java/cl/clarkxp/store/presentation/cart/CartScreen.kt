package cl.clarkxp.store.presentation.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.clarkxp.store.core.extensions.toUSD
import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.presentation.cart.mvi.CartEffect
import cl.clarkxp.store.presentation.cart.mvi.CartIntent
import cl.clarkxp.store.presentation.cart.mvi.CartState
import cl.clarkxp.store.presentation.cart.ui.CartItemRow
import cl.clarkxp.store.presentation.ui.theme.storeTheme

// --- COMPONENTE STATEFUL ---
@Composable
fun CartScreen(
    onBack: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CartEffect.NavigateBack -> onBack()
                is CartEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CartContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}

// --- COMPONENTE STATELESS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartContent(
    state: CartState,
    snackbarHostState: SnackbarHostState,
    onIntent: (CartIntent) -> Unit
) {
    // Diálogo de confirmación (parte de la UI pura si el estado lo dicta)
    if (state.showClearDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(CartIntent.OnDismissClearDialog) },
            title = { Text("Vaciar Carrito") },
            text = { Text("¿Estás seguro que deseas eliminar todos los productos?") },
            confirmButton = {
                TextButton(onClick = { onIntent(CartIntent.OnConfirmClearCart) }) { Text("Vaciar") }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(CartIntent.OnDismissClearDialog) }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito") },
                navigationIcon = {
                    IconButton(onClick = { onIntent(CartIntent.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { onIntent(CartIntent.OnClearCartClick) }) {
                        Icon(Icons.Outlined.Delete, "Vaciar Carro")
                    }
                }
            )
        },
        bottomBar = {
            if (state.items.isNotEmpty()) {
                Surface(shadowElevation = 16.dp, tonalElevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total:", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = state.totalAmount.toUSD(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onIntent(CartIntent.OnCheckoutClick) },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) { Text("Pagar Ahora") }
                    }
                }
            }
        }
    ) { padding ->
        if (state.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Tu carrito está vacío", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.items, key = { it.id }) { item ->
                    CartItemRow(item = item, onIntent = onIntent)
                }
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showSystemUi = true)
@Composable
fun PreviewCartContent() {
    val items = listOf(
        CartItem(1, "TV Samsung", 300000.0, "", 1),
        CartItem(2, "Cable HDMI", 5000.0, "", 2)
    )
    val state = CartState(
        isLoading = false,
        items = items,
        totalAmount = 310000.0,
        showClearDialog = false
    )

    storeTheme {
        CartContent(
            state = state,
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {}
        )
    }
}

@Preview(showSystemUi = true, name = "Empty Cart")
@Composable
fun PreviewEmptyCart() {
    storeTheme {
        CartContent(
            state = CartState(),
            snackbarHostState = remember { SnackbarHostState() },
            onIntent = {}
        )
    }
}