package cl.clarkxp.store.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.clarkxp.store.presentation.cart.CartScreen
import cl.clarkxp.store.presentation.detail.DetailScreen
import cl.clarkxp.store.presentation.home.HomeScreen
import cl.clarkxp.store.presentation.home.mvi.HomeEffect
import cl.clarkxp.store.presentation.ui.theme.storeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Usamos el tema default de Material por ahora
            storeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. Creamos el controlador de navegación
                    val navController = rememberNavController()

                    // 2. Definimos el Grafo de Navegación
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {

                        // --- RUTA: HOME ---
                        composable("home") {
                            HomeScreen(
                                onNavigate = { effect ->
                                    when (effect) {
                                        is HomeEffect.NavigateToDetail -> {
                                            // Navegación con argumento
                                            navController.navigate("detail/${effect.productId}")
                                        }
                                        is HomeEffect.NavigateToCart -> {
                                            navController.navigate("cart")
                                        }
                                        is HomeEffect.ShowError -> {
                                            // Aquí podrías mostrar un Snackbar global
                                        }
                                    }
                                }
                            )
                        }

                        // --- RUTA: DETALLE ---
                        composable(
                            route = "detail/{productId}",
                            arguments = listOf(
                                navArgument("productId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->

                            DetailScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // --- RUTA: CART ---
                        composable("cart") {
                            CartScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
