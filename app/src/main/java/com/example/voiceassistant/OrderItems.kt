package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceassistant.GcashPage.Payment
import com.example.voiceassistant.LoginActivity.Companion.randomCode
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
    private lateinit var paymentStatusView: TextView
    private var order_id: String? = null

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
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
        @SerialName("quantity") val quantity: Int,
        @SerialName("payment_status") val paymentStatus: String
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
        paymentStatusView = findViewById(R.id.paymentStatusView)
        val btnGoBack: Button = findViewById(R.id.buttonGoBack)
        val btnConfirmOrder: Button = findViewById(R.id.orderComplete)

        // Initialize adapter
        orderAdapter = OrderItemsAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = orderAdapter

        // Handle button clicks
        btnGoBack.setOnClickListener {
            finish()
        }

        btnConfirmOrder.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val orderId = order_id ?: return@launch // Ensure order_id is not null

                    // Update payment status
                    val response = supabase.postgrest.from("cart").update(mapOf("payment_status" to "Paid: Cash")) {
                        filter {
                            eq("order_id", orderId)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        // Log success and navigate to the next activity
                        Log.d("Order Complete", "Payment status updated: $response")
                        startActivity(Intent(this@OrderItems, QrScanner::class.java))
                    }
                } catch (e: Exception) {
                    // Handle exception (e.g., show user feedback)
                    Log.e("OrderItems", "Error updating payment status", e)
                }
            }
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

                val paymentStatuses = items.map { it.paymentStatus }.distinct().joinToString(", ")

                withContext(Dispatchers.Main) {
                    orderAdapter.updateItems(items)
                    totalAmountTextView.text = String.format("Total Amount: $%.2f", totalAmount)
                    paymentStatusView.text = paymentStatuses
                }
            } catch (e: Exception) {
                Log.e("OrderItems", "Error loading cart items", e)
            }
        }
    }
}