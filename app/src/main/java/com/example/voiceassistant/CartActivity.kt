package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceassistant.LoginActivity.Companion.randomCode
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


class CartActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var totalAmountTextView: TextView

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Postgrest)
    }

    @Serializable
    data class CartItem(
        @SerialName("cart_id") val cart_id: Int,
        @SerialName("order_id") val orderid: String,
        @SerialName("food_name") val foodName: String,
        @SerialName("category") val category: String,
        @SerialName("taste") val taste: String,
        @SerialName("price") val price: Double,
        @SerialName("quantity") val quantity: Int,
        @SerialName("payment_status") val paymentStatus: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerView = findViewById(R.id.recyclerViewCart)
        totalAmountTextView = findViewById(R.id.textViewTotalAmount)

        recyclerView.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(emptyList(), this::deleteItem, this::minusItem, this::plusItem)
        recyclerView.adapter = cartAdapter

        findViewById<Button>(R.id.btnGoBack).setOnClickListener {
            finish()
        }

        val btnConfirmOrder = findViewById<Button>(R.id.btnConfirmOrder)
        btnConfirmOrder.setOnClickListener {
            startActivity(Intent(this, Payment::class.java))
        }

        loadCartItems()
    }

    private fun loadCartItems() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val orderid = randomCode
                val result = supabase.postgrest.from("cart").select {
                    filter {
                        eq("order_id", orderid)
                    }
                }
                val items = result.decodeList<CartItem>() ?: emptyList()

                // Aggregate items by food_name and order_id
                val aggregatedItems = items
                    .groupBy { it.foodName to it.orderid }
                    .map { (key, groupedItems) ->
                        val totalQuantity = groupedItems.sumOf { it.quantity }
                        val firstItem = groupedItems.first()
                        firstItem.copy(quantity = totalQuantity, price = totalQuantity * firstItem.price)
                    }

                val totalAmount = aggregatedItems.sumOf { it.price }

                withContext(Dispatchers.Main) {
                    cartAdapter.updateItems(aggregatedItems)
                    totalAmountTextView.text = String.format("Total Amount: $%.2f", totalAmount)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cart items", e)
            }
        }
    }


    private fun deleteItem(cartItem: CartItem) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = supabase.postgrest.from("cart").delete {
                    filter {
                        eq("food_name", cartItem.foodName)
                        eq("order_id", cartItem.orderid)
                    }
                }

                if (response != null) {
                    withContext(Dispatchers.Main) {
                        loadCartItems()
                    }
                } else {
                    Log.e(TAG, "Failed to delete cart items: No data in response")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting cart items", e)
            }
        }
    }

    private fun minusItem(cartItem: CartItem) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedItem = cartItem.copy(quantity = cartItem.quantity - 1)

                if (updatedItem.quantity <= 0) {
                    deleteItem(cartItem)
                } else {
                    val response = supabase.postgrest.from("cart").update(updatedItem) {
                        filter {
                            eq("cart_id", cartItem.cart_id)
                        }
                    }

                    if (response != null) {
                        withContext(Dispatchers.Main) {
                            loadCartItems()
                        }
                    } else {
                        Log.e(TAG, "Failed to update cart item: No data in response")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating cart item", e)
            }
        }
    }

    private fun plusItem(cartItem: CartItem) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Increase quantity
                val updatedItem = cartItem.copy(quantity = cartItem.quantity + 1)

                // Update the existing item in Supabase
                val response = supabase.postgrest.from("cart").update(updatedItem) {
                    filter {
                        eq("cart_id", cartItem.cart_id)
                    }
                }

                // Check if the response contains data or if there's an error
                if (response != null) {
                    // Reload cart items to reflect the changes
                    withContext(Dispatchers.Main) {
                        loadCartItems()
                    }
                } else {
                    // Handle error or empty response
                    Log.e(TAG, "Failed to update cart item: No data in response")
                }
            } catch (e: Exception) {
                // Handle any exceptions thrown during the operation
                Log.e(TAG, "Error updating cart item", e)
            }
        }
    }

    companion object {
        private const val TAG = "CartActivity"
    }
}