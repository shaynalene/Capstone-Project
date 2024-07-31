package com.example.voiceassistant

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

class MenuAdapter(
    private var items: List<MainActivity.MenuItem>, // Change to var to allow updates
    private val onAddToCartClick: (MainActivity.MenuItem) -> Unit // Callback
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {
    val supabase = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Postgrest)
    }

    @Serializable
    data class MenuItem(
        @SerialName("food_name") val foodName: String,
        @SerialName("category") val category: String,
        @SerialName("taste") val taste: String,
        @SerialName("price") val price: Double
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.foodName
        holder.itemDetails.text = "${item.category}, ${item.taste}"
        holder.itemPrice.text = "$${item.price}"

        // Use normalized foodName to match drawable resource
        val imageResId = holder.itemView.context.resources.getIdentifier(
            item.foodName.toLowerCase(Locale.getDefault()).replace(" ", "_"),
            "drawable",
            holder.itemView.context.packageName
        )
        if (imageResId != 0) {
            holder.itemImage.setImageResource(imageResId)
        } else {
            holder.itemImage.setImageResource(R.drawable.ic_launcher_foreground) // Fallback image
        }

        // Handle "Add to Cart" button click
        holder.buttonAddToCart.setOnClickListener {
            onAddToCartClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    // Add this method to update the list of items
    fun updateItems(newItems: List<MainActivity.MenuItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.item_image)
        val itemName: TextView = view.findViewById(R.id.item_name)
        val itemDetails: TextView = view.findViewById(R.id.item_details)
        val itemPrice: TextView = view.findViewById(R.id.item_price)
        val buttonAddToCart: Button = view.findViewById(R.id.button_add_to_cart)
    }
}