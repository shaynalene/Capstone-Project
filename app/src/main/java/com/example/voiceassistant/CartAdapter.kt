package com.example.voiceassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private var items: List<CartActivity.CartItem>,
    private val deleteItem: (CartActivity.CartItem) -> Unit // Change here to define the callback
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

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

        // Set the click listener for the delete button
        holder.buttonDeleteItem.setOnClickListener {
            deleteItem(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(newItems: List<CartActivity.CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodNameTextView: TextView = itemView.findViewById(R.id.textViewFoodName)
        val quantityTextView: TextView = itemView.findViewById(R.id.textViewQuantity)
        val priceTextView: TextView = itemView.findViewById(R.id.textViewPrice)
        val buttonDeleteItem: ImageButton = itemView.findViewById(R.id.button_delete)
    }
}
