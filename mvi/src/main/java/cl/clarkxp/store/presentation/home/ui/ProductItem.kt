package cl.clarkxp.store.presentation.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.clarkxp.store.core.extensions.toUSD
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.presentation.home.model.ProductUiModel
import cl.clarkxp.store.presentation.home.mvi.HomeIntent
import coil.compose.AsyncImage

@Composable
fun ProductItem(
    uiModel: ProductUiModel,
    onIntent: (HomeIntent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp) // 1. ALTURA FIJA: Normaliza todas las tarjetas
            .padding(4.dp)
            .clickable { onIntent(HomeIntent.OnProductClick(uiModel.product.id)) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSecondary),

    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(), // Ocupar todo el alto fijo
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // 2. IMPORTANTE: Empuja el botón al fondo
        ) {

            // --- BLOQUE SUPERIOR (Imagen + Textos) ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Imagen
                AsyncImage(
                    model = uiModel.product.image,
                    contentDescription = null,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Título (Reservamos espacio para 2 líneas siempre)
                Text(
                    text = uiModel.product.title,
                    maxLines = 2,
                    minLines = 2, // 3. TRUCO: Reserva espacio aunque el texto sea corto
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Precio
                Text(
                    text = uiModel.product.price.toUSD(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // --- BLOQUE INFERIOR (Botones) ---
            // Al estar en un Column con SpaceBetween, esto se irá al fondo
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (uiModel.quantity == 0) {
                    OutlinedButton(
                        onClick = { onIntent(HomeIntent.IncreaseQuantity(uiModel.product)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text("Agregar")
                    }
                } else {
                    // Controles de cantidad
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Botón Menos (Morado sólido)
                        FilledIconButton(
                            onClick = { onIntent(HomeIntent.DecreaseQuantity(uiModel.product.id)) },
                            modifier = Modifier.size(40.dp),

                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Restar")
                        }

                        Text(
                            text = uiModel.quantity.toString(),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Botón Más (Morado sólido)
                        FilledIconButton(
                            onClick = { onIntent(HomeIntent.IncreaseQuantity(uiModel.product)) },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Sumar")
                        }
                    }
                }
            }
        }
    }
}

// 1. Datos Falsos para la Preview
val mockProduct = Product(
    id = 1,
    title = "Televisor Samsung 55' 4K UHD Smart TV",
    price = 350000.0,
    description = "Descripción de prueba",
    category = "Electronics",
    image = "", // Url vacía o un placeholder local
    rating = 4.5,
    ratingCount = 100
)

val mockUiModel = ProductUiModel(
    product = mockProduct,
    quantity = 0 // Prueba con 0 y con 1 para ver ambos estados
)

// 2. La Función de Preview
@Preview(showBackground = true, name = "Item Normal - Sin agregar")
@Composable
fun PreviewProductItem() {
    // Usamos tu tema para que los colores sean correctos
    cl.clarkxp.store.presentation.ui.theme.storeTheme {
        ProductItem(
            uiModel = mockUiModel,
            onIntent = {} // Lambda vacía, no hace nada en la preview
        )
    }
}

@Preview(showBackground = true, name = "Item Agregado (Cantidad 3)")
@Composable
fun PreviewProductItemAdded() {
    cl.clarkxp.store.presentation.ui.theme.storeTheme {
        ProductItem(
            uiModel = mockUiModel.copy(quantity = 3),
            onIntent = {}
        )
    }
}