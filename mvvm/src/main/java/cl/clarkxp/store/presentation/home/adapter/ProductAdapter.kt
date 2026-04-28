package cl.clarkxp.store.presentation.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cl.clarkxp.store.core.extensions.toUSD
import cl.clarkxp.store.domain.model.Product
import cl.clarkxp.store.mvvm.databinding.ItemProductFeaturedBinding
import cl.clarkxp.store.mvvm.databinding.ItemProductStandardBinding
import cl.clarkxp.store.presentation.home.model.ProductUiModel
import coil.load

class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onIncreaseClick: (Product) -> Unit,
    private val onDecreaseClick: (Product) -> Unit
) : ListAdapter<ProductUiModel, RecyclerView.ViewHolder>(ProductDiffCallback()) {

    companion object {
        private const val TYPE_FEATURED = 0
        private const val TYPE_STANDARD = 1
    }

    // Si es el primero (item 0), es destacado.
    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_FEATURED else TYPE_STANDARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_FEATURED) {
            val binding = ItemProductFeaturedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            FeaturedViewHolder(binding)
        } else {
            val binding = ItemProductStandardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            StandardViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = getItem(position)
        when (holder) {
            is FeaturedViewHolder -> holder.bind(product)
            is StandardViewHolder -> holder.bind(product)
        }
    }

    // ViewHolder para el destacado
    inner class FeaturedViewHolder(private val binding: ItemProductFeaturedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uiModel: ProductUiModel) {

            val product = uiModel.product

            binding.tvTitle.text = product.title
            binding.tvPrice.text = product.price.toUSD()
            binding.ivProduct.load(product.image) // Coil carga la imagen
            binding.ratingBar.rating = product.rating.toFloat()
            binding.tvRatingCount.text = "(${product.ratingCount})"

            if (uiModel.quantity > 0) {
                binding.btnInitialAdd.visibility = View.GONE
                binding.quantityControlsGroup.visibility = View.VISIBLE
                binding.tvQuantity.text = uiModel.quantity.toString()
            } else {
                binding.btnInitialAdd.visibility = View.VISIBLE
                binding.quantityControlsGroup.visibility = View.GONE
            }

            binding.root.setOnClickListener { onProductClick(product) }
            binding.btnInitialAdd.setOnClickListener { onIncreaseClick(product) }
            binding.btnPlus.setOnClickListener { onIncreaseClick(product) }
            binding.btnMinus.setOnClickListener { onDecreaseClick(product) }


        }
    }

    // ViewHolder Normal
    inner class StandardViewHolder(private val binding: ItemProductStandardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uiModel: ProductUiModel) {

            val product = uiModel.product

            binding.tvTitle.text = product.title
            binding.tvPrice.text = product.price.toUSD()
            binding.ivProduct.load(product.image)

            if (uiModel.quantity > 0) {
                // Mostrar controles +/-
                binding.btnInitialAdd.visibility = View.GONE
                binding.quantityControlsGroup.visibility = View.VISIBLE
                binding.tvQuantity.text = uiModel.quantity.toString()
            } else {
                // Mostrar botón "Agregar"
                binding.btnInitialAdd.visibility = View.VISIBLE
                binding.quantityControlsGroup.visibility = View.GONE
            }

            binding.root.setOnClickListener { onProductClick(product) }
            binding.btnInitialAdd.setOnClickListener { onIncreaseClick(product) }
            binding.btnPlus.setOnClickListener { onIncreaseClick(product) }
            binding.btnMinus.setOnClickListener { onDecreaseClick(product) }
        }
    }
}

class ProductDiffCallback : DiffUtil.ItemCallback<ProductUiModel>() {
    override fun areItemsTheSame(oldItem: ProductUiModel, newItem: ProductUiModel) = oldItem.product.id == newItem.product.id
    override fun areContentsTheSame(oldItem: ProductUiModel, newItem: ProductUiModel) = oldItem == newItem
}