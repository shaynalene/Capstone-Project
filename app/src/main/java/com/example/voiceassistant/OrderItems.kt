package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class OrderItems : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderItemsAdapter
    private lateinit var totalAmountTextView: TextView
    private lateinit var username: TextView
    private var order_id: String? = null

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "your-supabase-key" // replace with your actual key
    ) {
        install(Postgrest)
    }

    @Serializable
    data class CartItem(
        @SerialName("cart_id") val cart_id: Int,
        @SerialName("food_name") val foodName: String,
        @SerialName("category") val category: String,
        @SerialName("taste") val taste: String,
        @SerialName("price") val price: Double,
        @SerialName("quantity") val quantity: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_items)

        // Retrieve and display order ID
        order_id = intent.getStringExtra("RESULT_TEXT")

        // Initialize views
        recyclerView = findViewById(R.id.orderItems)
        totalAmountTextView = findViewById(R.id.totalAmount)
        username = findViewById(R.id.userName)
        val btnGoBack: Button = findViewById(R.id.buttonGoBack)
        val btnConfirmOrder: Button = findViewById(R.id.orderComplete)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        orderAdapter = OrderItemsAdapter(emptyList(), this::deleteItem)
        recyclerView.adapter = orderAdapter

        // Handle button clicks
        btnGoBack.setOnClickListener {
            finish()
        }

        btnConfirmOrder.setOnClickListener {
            startActivity(Intent(this, QrScanner::class.java))
        }

        // Set the username TextView with order ID
        username.text = order_id

        // Load cart items from Supabase
        loadCartItems(order_id)
    }

    private fun loadCartItems(orderId: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabase.postgrest.from("cart").select {
                    filter {
                        order_id?.let { eq("order_id", it) }
                    }
                }
                val items = result.decodeList<CartItem>()
                val totalAmount = items.sumOf { it.price * it.quantity }

                withContext(Dispatchers.Main) {
                    orderAdapter.updateItems(items)
                    totalAmountTextView.text = String.format("Total Amount: $%.2f", totalAmount)
                }
            } catch (e: Exception) {
                Log.e("Order Items", "Error loading cart items", e)
            }
        }
    }

    // Method to handle item deletion (if needed)
    private fun deleteItem(cartItem: CartItem) {
        // Implement the logic to delete an item if necessary
    }
}
