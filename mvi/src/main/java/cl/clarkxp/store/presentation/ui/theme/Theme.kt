package cl.clarkxp.store.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define tus colores aquí (Reemplazando a colors.xml)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
    // Puedes personalizar background, surface, etc.
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    // surface = Color(0xFF1C1B1F), // Opcional: Personalizar fondo negro/gris
    // background = Color(0xFF1C1B1F)
)

@Composable
fun storeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta el sistema automáticamente
    // Dynamic color está disponible en Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Soporte para Dynamic Color (Material You) en Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Si no hay dynamic color o está apagado, usa tus colores fijos
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Lógica para pintar la Barra de Estado (Status Bar)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // En Edge-to-Edge solemos usar transparente, pero aquí aseguramos iconos legibles
            window.statusBarColor = Color.Transparent.toArgb()
            // Si es tema oscuro -> Iconos blancos (isAppearanceLightStatusBars = false)
            // Si es tema claro -> Iconos negros (isAppearanceLightStatusBars = true)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // typography = Typography, // Asegúrate de tener Typography definido o usa el default
        content = content
    )
}