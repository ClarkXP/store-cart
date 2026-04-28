package cl.clarkxp.store.presentation.cart

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import cl.clarkxp.store.core.extensions.toUSD
import cl.clarkxp.store.mvvm.R
import cl.clarkxp.store.mvvm.databinding.FragmentCartBinding
import cl.clarkxp.store.presentation.cart.adapter.CartAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CartFragment : Fragment(R.layout.fragment_cart) {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCartBinding.bind(view)

        setupToolbar()
        setupRecyclerView()
        observeState()

        binding.btnPurchase.setOnClickListener {
            Toast.makeText(requireContext(), "Compra simulada exitosa", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onPlusClick = { item -> viewModel.increaseQuantity(item) },
            onMinusClick = { item -> viewModel.decreaseQuantity(item) }
        )
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    // Actualizamos lista
                    cartAdapter.submitList(state.items)

                    // Actualizamos Total
                    binding.tvTotalAmount.text = state.totalAmount.toUSD()

                    // Si la lista está vacía, mostramos mensaje de carrito
                    if (state.items.isEmpty()) {
                        binding.tvEmptyCart.visibility = View.VISIBLE
                        binding.rvCartItems.visibility = View.GONE
                        binding.footerContainer.visibility = View.GONE
                    } else {
                        binding.tvEmptyCart.visibility = View.GONE
                        binding.rvCartItems.visibility = View.VISIBLE
                        binding.footerContainer.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_clear -> {
                        // Verificar que el carro no esté vacío antes de preguntar
                        if (viewModel.state.value.items.isNotEmpty()) {
                            showClearCartConfirmation()
                        } else {
                            Toast.makeText(context, "El carro ya está vacío", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun showClearCartConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Vaciar Carrito")
            .setMessage("¿Estás seguro que deseas eliminar todos los productos del carro?")
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Vaciar") { dialog, _ ->
                viewModel.clearCart()
                Toast.makeText(requireContext(), "Carro vaciado", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}