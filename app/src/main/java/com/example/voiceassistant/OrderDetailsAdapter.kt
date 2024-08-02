package com.example.voiceassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderDetailsAdapter(
    private var items: List<OrderDetails.CartItem>
) : RecyclerView.Adapter<OrderDetailsAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_details, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.foodNameTextView.text = item.foodName
        holder.quantityTextView.text = "Quantity: ${item.quantity}"
        holder.priceTextView.text = String.format("Price: $%.2f", item.price)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(newItems: List<OrderDetails.CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodNameTextView: TextView = itemView.findViewById(R.id.textViewFoodName)
        val quantityTextView: TextView = itemView.findViewById(R.id.textViewQuantity)
        val priceTextView: TextView = itemView.findViewById(R.id.textViewPrice)
    }
}
