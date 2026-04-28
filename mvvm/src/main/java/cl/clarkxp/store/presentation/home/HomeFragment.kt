package cl.clarkxp.store.presentation.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.mvvm.R
import cl.clarkxp.store.mvvm.databinding.FragmentHomeBinding
import cl.clarkxp.store.presentation.MainActivity
import cl.clarkxp.store.presentation.home.adapter.ProductAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        observeState()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onProductClick = { product ->
                // Navegación al detalle
                val action = HomeFragmentDirections.actionShowProductDetail(product.id)
                findNavController().navigate(action)

            },
            onIncreaseClick = { product ->
                // Lógica de carrito
                viewModel.increaseQuantity(product)

            },
            onDecreaseClick = { product ->
                // Lógica de carrito
                viewModel.decreaseQuantity(product)
            }
        )

        val layoutManager = GridLayoutManager(requireContext(), 2)

        //para que el primer item ocupe el ancho de la pantalla
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) 2 else 1
            }
        }

        binding.rvProducts.apply {
            this.layoutManager = layoutManager
            adapter = productAdapter
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            //  Click menú hamburguesa (Navigation Icon)
            setNavigationOnClickListener {
                (requireActivity() as MainActivity).openDrawer()
            }

            val cartMenuItem = menu.findItem(R.id.action_cart)
            val actionView = cartMenuItem.actionView

            // Configura  click en todo el layout del botón del carrito
            actionView?.setOnClickListener {
                findNavController().navigate(R.id.action_open_cart)
            }
        }
    }

    // Función para actualizar el contador rojo desde el ViewModel
    fun updateCartBadge(count: Int) {
        //busco el item del menu
        val cartMenuItem = binding.toolbar.menu.findItem(R.id.action_cart)
        //referencia a su customview
        val actionView = cartMenuItem.actionView
        //busco el textview del customview
        val tvCount = actionView?.findViewById<TextView>(R.id.tvCartCount)

        if (count > 0) {
            tvCount?.visibility = View.VISIBLE
            tvCount?.text = count.toString()
        } else {
            tvCount?.visibility = View.GONE
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.rvProducts.visibility = View.GONE
                                binding.tvError.visibility = View.GONE
                            }

                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                binding.rvProducts.visibility = View.VISIBLE
                                productAdapter.submitList(resource.data)
                            }

                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.tvError.visibility = View.VISIBLE
                                binding.tvError.text = resource.message
                                Toast.makeText(
                                    requireContext(),
                                    resource.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.cartCount.collect { count ->
                        updateCartBadge(count)
                    }
                }
            }
        }
    }

    fun updateCategory(category: String) {
        binding.toolbar.title = if (category == "Todos") getString(R.string.app_name) else category
        viewModel.loadProducts(if (category == "Todos") null else category.lowercase())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}