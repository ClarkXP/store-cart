package cl.clarkxp.store.presentation.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun SimpleRatingBar(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp, // Tamaño configurable para no usar scale()
    starColor: Color = Color(0xFFFFC107)
) {
    Row(modifier = modifier) {
        val maxStars = 5
        // Redondeamos al entero más cercano (ej: 3.6 -> 4, 3.2 -> 3)
        val filledStars = rating.roundToInt()

        repeat(maxStars) { index ->
            // index comienza en 0.
            // Si rating es 4:
            // 0 < 4 (Star), 1 < 4 (Star), 2 < 4 (Star), 3 < 4 (Star), 4 < 4 (False -> Border)
            val isFilled = index < filledStars

            val icon = if (isFilled) Icons.Default.Star else Icons.Default.StarBorder
            val color = if (isFilled) starColor else Color.LightGray

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(starSize) // Tamaño explícito
            )
        }
    }
}