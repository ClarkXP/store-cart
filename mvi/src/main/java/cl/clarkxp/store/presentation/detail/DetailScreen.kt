package cl.clarkxp.store.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.clarkxp.store.core.extensions.toUSD
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.presentation.common.SimpleRatingBar
import cl.clarkxp.store.presentation.detail.mvi.DetailEffect
import cl.clarkxp.store.presentation.detail.mvi.DetailIntent
import cl.clarkxp.store.presentation.detail.mvi.DetailState
import cl.clarkxp.store.presentation.home.model.ProductUiModel
import cl.clarkxp.store.presentation.ui.theme.storeTheme
import coil.compose.AsyncImage

// --- COMPONENTE STATEFUL (Lógica) ---
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                DetailEffect.NavigateBack -> onBack()
            }
        }
    }

    DetailContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

// --- COMPONENTE STATELESS (UI Pura) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    state: DetailState,
    onIntent: (DetailIntent) -> Unit
) {
    // 1. CONTENEDOR RAÍZ: Se adapta a la altura (wrapContentHeight)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // Importante: No llenar todo el alto
            .navigationBarsPadding() // Respetar gestos del sistema abajo
    ) {

        // --- A. HEADER (Botón Atrás/Cerrar) ---
        // Lo ponemos en un Box para alinearlo a la derecha o izquierda según gusto
       /* Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Icono X o Flecha para cerrar el sheet
            *//*IconButton(
                onClick = { onIntent(DetailIntent.OnBackClick) },
                modifier = Modifier.align(Alignment.CenterEnd) // A la derecha queda mejor en Sheets
            ) {
                Icon(
                    imageVector = Icons.Default.Close, // Asegúrate de tener este icono
                    contentDescription = "Cerrar",
                    tint = Color.Gray
                )
            }*//*
            FilledTonalIconButton(
                { onIntent(DetailIntent.OnBackClick) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Rounded.Close, "")
            }
        }*/

        // --- B. CONTENIDO SCROLLABLE ---
        // Si hay poco texto, se encoge. Si hay mucho, hace scroll.
        if (state.isLoading) {
            Box(Modifier
                .fillMaxWidth()
                .height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            state.uiModel?.let { uiModel ->
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        // LA MAGIA: weight 1f permite scroll si crece,
                        // fill = false permite encogerse si es pequeño.
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Imagen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(top = 16.dp),

                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = uiModel.product.image,
                            contentDescription = null,
                            modifier = Modifier.fillMaxHeight(),
                            contentScale = ContentScale.Fit
                        )
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            FilledTonalIconButton(
                                { onIntent(DetailIntent.OnBackClick) },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(Icons.Rounded.Close, "")
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Título y Precio
                    Text(
                        text = uiModel.product.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiModel.product.price.toUSD(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            SimpleRatingBar(rating = uiModel.product.rating)
                            Text(
                                text = "(${uiModel.product.ratingCount} votos)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Descripción
                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = uiModel.product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                    )

                    Spacer(Modifier.height(24.dp)) // Espacio final antes del botón
                }

                // --- C. FOOTER (Botones de Acción) ---
                // Siempre visible al pie del contenido
                Surface(
                    shadowElevation = 8.dp, // Sombra suave hacia arriba
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        if (uiModel.quantity == 0) {
                            Button(
                                onClick = { onIntent(DetailIntent.IncreaseQuantity(uiModel.product)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = CircleShape
                            ) { Text("Agregar al Carro") }
                        } else {
                            // Tus controles +/-
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledIconButton(
                                    onClick = { onIntent(DetailIntent.DecreaseQuantity(uiModel.product.id)) },
                                    modifier = Modifier.size(48.dp)
                                ) { Icon(Icons.Default.Remove, null) }

                                Text(
                                    text = uiModel.quantity.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )

                                FilledIconButton(
                                    onClick = { onIntent(DetailIntent.IncreaseQuantity(uiModel.product)) },
                                    modifier = Modifier.size(48.dp)
                                ) { Icon(Icons.Default.Add, null) }
                            }
                        }
                    }
                }
            }
        }
    }
}
/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailContent(
    state: DetailState,
    onIntent: (DetailIntent) -> Unit
) {
    Scaffold(
        *//*topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { onIntent(DetailIntent.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },*//*
        bottomBar = {
            state.uiModel?.let { uiModel ->
                Surface(shadowElevation = 16.dp, color = MaterialTheme.colorScheme.surface) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding()
                    ) {
                        if (uiModel.quantity == 0) {
                            Button(
                                onClick = { onIntent(DetailIntent.IncreaseQuantity(uiModel.product)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = CircleShape
                            ) { Text("Agregar al Carro") }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledIconButton(
                                    onClick = { onIntent(DetailIntent.DecreaseQuantity(uiModel.product.id)) },
                                    modifier = Modifier.size(48.dp)
                                ) { Icon(Icons.Default.Remove, null) }

                                Text(
                                    text = uiModel.quantity.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )

                                FilledIconButton(
                                    onClick = { onIntent(DetailIntent.IncreaseQuantity(uiModel.product)) },
                                    modifier = Modifier.size(48.dp)
                                ) { Icon(Icons.Default.Add, null) }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                Modifier.wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            state.uiModel?.let { uiModel ->
                Column(
                    modifier = Modifier
                        //.fillMaxSize()
                        .wrapContentHeight()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(top = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = uiModel.product.image, contentDescription = null,
                            modifier = Modifier.fillMaxHeight(), contentScale = ContentScale.Fit
                        )
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            FilledTonalIconButton(
                                { onIntent(DetailIntent.OnBackClick) },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(Icons.Rounded.Close, "")
                            }
                        }
                        *//**//*
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = uiModel.product.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiModel.product.price.toUSD(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            SimpleRatingBar(rating = uiModel.product.rating)
                            Text(
                                text = "(${uiModel.product.ratingCount} votos)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = uiModel.product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                    )
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}*/

// --- PREVIEW ---
@Preview(showSystemUi = true)
@Composable
fun PreviewDetailContent() {
    val mockProduct = Product(
        1,
        "Chaqueta de Invierno Premium",
        59990.0,
        "Una chaqueta muy abrigadora para el invierno.",
        "Clothing",
        "",
        4.8,
        120
    )

    // Estado con producto cargado y 0 unidades (Botón Agregar)
    val state = DetailState(
        isLoading = false,
        uiModel = ProductUiModel(mockProduct, 0)
    )

    storeTheme {
        DetailContent(state = state, onIntent = {})
    }
}

@Preview(showBackground = true, name = "With Quantity")
@Composable
fun PreviewDetailContentWithQty() {
    val mockProduct = Product(1, "Chaqueta", 59990.0, "Desc", "Cat", "", 4.8, 120)
    val state = DetailState(
        isLoading = false,
        uiModel = ProductUiModel(mockProduct, 2) // Con 2 unidades (Controles +/-)
    )
    storeTheme {
        DetailContent(state = state, onIntent = {})
    }
}