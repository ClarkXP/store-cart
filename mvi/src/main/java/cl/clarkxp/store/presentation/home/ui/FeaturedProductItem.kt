package cl.clarkxp.store.presentation.home.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.clarkxp.store.core.extensions.toUSD
import cl.clarkxp.store.presentation.common.SimpleRatingBar
import cl.clarkxp.store.presentation.home.model.ProductUiModel
import cl.clarkxp.store.presentation.home.mvi.HomeIntent
import coil.compose.AsyncImage

@Composable
fun FeaturedProductItem(
    uiModel: ProductUiModel,
    onIntent: (HomeIntent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp) // Margen externo más generoso como en la foto
            .clickable { onIntent(HomeIntent.OnProductClick(uiModel.product.id)) },
        elevation = CardDefaults.cardElevation(2.dp),
        //border = BorderStroke(1.dp, Color(0xFFE0E0E0)), // Borde suave gris
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onSecondary) // Fondo lavanda muy suave
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .height(IntrinsicSize.Min) // Ajusta altura al contenido
        ) {
            // 1. IMAGEN (Izquierda)
            AsyncImage(
                model = uiModel.product.image,
                contentDescription = null,
                modifier = Modifier
                    .weight(0.5f) // Ocupa el 40% del ancho
                    .aspectRatio(0.8f) // Relación de aspecto vertical
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. COLUMNA DERECHA
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Etiqueta
                    Text(
                        text = "MEJOR VALORADO",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFD32F2F), // Rojo intenso
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Título
                    Text(
                        text = uiModel.product.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Rating (Estrellas + Conteo)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SimpleRatingBar(
                            rating = uiModel.product.rating,
                            starSize = 14.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${uiModel.product.ratingCount})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Precio
                    Text(
                        text = uiModel.product.price.toUSD(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary // Morado del tema
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. CONTROLES (Abajo a la derecha)
                if (uiModel.quantity == 0) {
                    // Botón "Agregar" (Estilo Outlined Pill como los de abajo)
                    OutlinedButton(
                        onClick = { onIntent(HomeIntent.IncreaseQuantity(uiModel.product)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50) // Pill shape
                    ) {
                        Text("Agregar")
                    }
                } else {
                    // Controles +/- (Estilo Cuadrado Redondeado Relleno)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón Menos
                        FilledIconButton(
                            onClick = { onIntent(HomeIntent.DecreaseQuantity(uiModel.product.id)) },
                            shape = RoundedCornerShape(12.dp), // Cuadrado redondeado
                            modifier = Modifier.size(48.dp),
                            /*colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )*/
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Restar")
                        }

                        // Cantidad
                        Text(
                            text = uiModel.quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Botón Más
                        FilledIconButton(
                            onClick = { onIntent(HomeIntent.IncreaseQuantity(uiModel.product)) },
                            shape = RoundedCornerShape(12.dp), // Cuadrado redondeado
                            modifier = Modifier.size(48.dp),
                            /*colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )*/
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Sumar")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360) // widthDp simula el ancho de un teléfono
@Composable
fun PreviewFeaturedItem() {
    cl.clarkxp.store.presentation.ui.theme.storeTheme {
        FeaturedProductItem(
            uiModel = mockUiModel.copy(product = mockProduct.copy(title = "Producto Destacado")),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, name = "Item Agregado (Cantidad 3)", widthDp = 360)
@Composable
fun PreviewFeaturedItemAdded() {
    cl.clarkxp.store.presentation.ui.theme.storeTheme {
        FeaturedProductItem(
            uiModel = mockUiModel.copy(quantity = 3),
            onIntent = {}
        )
    }
}