package cl.clarkxp.store.presentation.cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cl.clarkxp.store.core.extensions.toUSD
import cl.clarkxp.store.domain.model.CartItem
import cl.clarkxp.store.mvvm.databinding.ItemCartProductBinding
import coil.load

class CartAdapter(
    private val onPlusClick: (CartItem) -> Unit,
    private val onMinusClick: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CartViewHolder(private val binding: ItemCartProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {
            with(binding) {
                tvTitle.text = item.title
                tvPrice.text = item.price.toUSD()
                tvQuantity.text = item.quantity.toString()
                ivProduct.load(item.image)

                // Botones de acción
                btnPlus.setOnClickListener { onPlusClick(item) }
                btnMinus.setOnClickListener { onMinusClick(item) }
            }
        }
    }
}

class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem) = oldItem == newItem
}