package com.example.voiceassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private var items: List<CartActivity.CartItem>,
    private val deleteItem: (CartActivity.CartItem) -> Unit,
    private val minusItem: (CartActivity.CartItem) -> Unit,
    private val plusItem: (CartActivity.CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.foodNameTextView.text = item.foodName
        holder.quantityTextView.text = "Quantity: ${item.quantity}"
        holder.priceTextView.text = String.format("Price: $%.2f", item.price)

        holder.quantityControlTextView.text = item.quantity.toString()

        holder.buttonDeleteItem.setOnClickListener {
            deleteItem(item)
        }

        holder.buttonMinusItem.setOnClickListener {
            minusItem(item)
        }

        holder.buttonPlusItem.setOnClickListener {
            plusItem(item)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].cart_id.toLong()

    fun updateItems(newItems: List<CartActivity.CartItem>) {
        val diffCallback = CartDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodNameTextView: TextView = itemView.findViewById(R.id.textViewFoodName)
        val quantityTextView: TextView = itemView.findViewById(R.id.textViewQuantity)
        val priceTextView: TextView = itemView.findViewById(R.id.textViewPrice)
        val buttonDeleteItem: ImageButton = itemView.findViewById(R.id.button_delete)
        val buttonMinusItem: Button = itemView.findViewById(R.id.button_minus)
        val buttonPlusItem: Button = itemView.findViewById(R.id.button_plus)
        val quantityControlTextView: TextView = itemView.findViewById(R.id.textViewQuantityControl)
    }

    class CartDiffCallback(
        private val oldList: List<CartActivity.CartItem>,
        private val newList: List<CartActivity.CartItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].cart_id == newList[newItemPosition].cart_id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}