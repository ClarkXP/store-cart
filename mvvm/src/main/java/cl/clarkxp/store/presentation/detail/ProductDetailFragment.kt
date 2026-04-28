package cl.clarkxp.store.presentation.detail

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import cl.clarkxp.store.core.extensions.toUSD
import cl.clarkxp.store.core.utils.Resource
import cl.clarkxp.store.mvvm.R
import cl.clarkxp.store.mvvm.databinding.FragmentProductDetailBinding
import cl.clarkxp.store.presentation.home.model.ProductUiModel
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductDetailViewModel by viewModels()
    private val args: ProductDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        // Verificamos si el dispositivo soporta Blur Nativo (Android 12 / API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val window = dialog.window
            val wm = requireContext().getSystemService(WindowManager::class.java)

            // Verificamos si el hardware/sistema soporta blur activamente
            if (wm.isCrossWindowBlurEnabled) {
                window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                val attributes = window?.attributes
                attributes?.blurBehindRadius = 60
                window?.attributes = attributes

                // Si hay blur, hacemos la sombra negra más suave
                window?.setDimAmount(0.4f)
                //Log.d("ProductDetailFragment", "Blur habilitado")
            } else {
                // Si NO hay blur (o el usuario lo desactivó), oscurecemos más el fondo
                // para mantener el contraste.
                window?.setDimAmount(0.7f)
                //Log.d("ProductDetailFragment", "Blur desactivado")

            }

            // cambios en tiempo real (por si el usuario lo activa mientras usa la app)
            val blurListener = java.util.function.Consumer<Boolean> { enabled ->
                if (enabled) {
                    window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    window?.setDimAmount(0.4f)
                    //Log.d("ProductDetailFragment", "Blur habilitado")

                } else {
                    window?.clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    window?.setDimAmount(0.7f)
                    //Log.d("ProductDetailFragment", "Blur inhabilitado")

                }
            }
            wm.addCrossWindowBlurEnabledListener(blurListener)
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar datos
        viewModel.loadProduct(args.productId)

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            resource.data?.let { uiModel -> populateUI(uiModel) }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun populateUI(uiModel: ProductUiModel) {
        val product= uiModel.product

        with(binding) {
            tvTitle.text = product.title
            tvPrice.text = product.price.toUSD()
            tvDescription.text = product.description

            ratingBar.rating = product.rating.toFloat()
            tvRatingCount.text = "(${product.ratingCount} votos)"

            ivProduct.load(product.image)

            if (uiModel.quantity > 0) {
                btnInitialAdd.visibility = View.GONE
                quantityControlsGroup.visibility = View.VISIBLE
                tvQuantity.text = uiModel.quantity.toString()
            } else {
                btnInitialAdd.visibility = View.VISIBLE
                quantityControlsGroup.visibility = View.GONE
            }

            btnInitialAdd.setOnClickListener { viewModel.increaseQuantity(product) }
            btnPlus.setOnClickListener { viewModel.increaseQuantity(product) }
            btnMinus.setOnClickListener { viewModel.decreaseQuantity(product.id) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}