package cl.clarkxp.store.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import cl.clarkxp.store.core.extensions.capitalizeWords
import cl.clarkxp.store.mvvm.R
import cl.clarkxp.store.mvvm.databinding.ActivityMainBinding
import cl.clarkxp.store.presentation.home.HomeFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDrawer()
    }

    private fun setupDrawer() {
        // Cargar categorías dinámicamente
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.categories.collect { categories ->
                    updateMenu(categories)
                }
            }
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            // Activa visualmente el item cliqueado
            menuItem.isChecked = true
            //extraigo la categoría
            val category = menuItem.title.toString()
            // busco en el navhost el HomeFragment
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()

            if (currentFragment is HomeFragment) {
                //cargo los productos de la categoría seleccionada
                currentFragment.updateCategory(category)
            }

            binding.drawerLayout.closeDrawers()
            true
        }
    }

    private fun updateMenu(categories: List<String>) {
        val menu = binding.navView.menu
        menu.clear()

        val GROUP_ID = 1

        val allItem = menu.add(GROUP_ID, 0, 0, "Todos")
        //allItem.setIcon(android.R.drawable.ic_menu_view)
        allItem.isCheckable = true
        allItem.isChecked = true

        categories.forEachIndexed { index, category ->
            val title = category.capitalizeWords()
            val item = menu.add(GROUP_ID, index + 1, 0, title)
            item.isCheckable = true
        }

        menu.setGroupCheckable(GROUP_ID, true, true)
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }
}