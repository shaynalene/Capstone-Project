package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
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
import kotlinx.serialization.json.Json

class TransactionPage : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private var order_id: String? = null

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Postgrest)
    }

    @Serializable
    data class TransactionItem(
        @SerialName("cart_id") val cartId: Int,
        @SerialName("order_id") val orderId: String,
        @SerialName("food_name") val foodName: String,
        @SerialName("category") val category: String,
        @SerialName("taste") val taste: String,
        @SerialName("price") val price: Double,
        @SerialName("quantity") val quantity: Int,
        @SerialName("payment_status") val paymentStatus: String? = "Unknown" // Allow null values and set default value
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transaction_page)

        // Retrieve and display order ID
        order_id = intent.getStringExtra("RESULT_TEXT")

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)

        // Initialize adapter
        transactionAdapter = TransactionAdapter(emptyList()) { transactionItem ->
            onTransactionItemClick(transactionItem)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = transactionAdapter

        // Load cart items from Supabase
        loadResults()

        // Set up button listeners for redirection
        findViewById<ImageButton>(R.id.scannerButton).setOnClickListener {
            startActivity(Intent(this, QrScanner::class.java))
        }
        findViewById<ImageButton>(R.id.transactionButton).setOnClickListener {
            // No action needed as this is the current activity
        }
        findViewById<ImageButton>(R.id.userButton).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }


    private fun loadResults() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabase.postgrest
                    .from("cart")
                    .select()

                // Assuming the response is in the 'data' property of the result object
                val resultString = result.data?.toString() ?: ""

                val items = Json {
                    ignoreUnknownKeys = true // This will ignore any unknown keys
                    coerceInputValues = true // This will coerce null values to default values
                }.decodeFromString<List<TransactionItem>>(resultString)

                // Group by
                val groupedResults = items
                    .groupBy { it.orderId to it.paymentStatus }
                    .map { (key, groupedItems) ->
                        val totalQuantity = groupedItems.sumOf { it.quantity }
                        val firstItem = groupedItems.first()
                        firstItem.copy(quantity = totalQuantity, price = totalQuantity * firstItem.price)
                    }

                withContext(Dispatchers.Main) {
                    transactionAdapter.updateItems(groupedResults)

                    // Log grouped results
                    Log.d("TransactionPage", "Grouped Results: $groupedResults")
                }
            } catch (e: Exception) {
                Log.e("TransactionPage", "Error loading grouped results", e)
            }
        }
    }

    private fun onTransactionItemClick(transactionItem: TransactionItem) {
        val intent = Intent(this, OrderDetails::class.java).apply {
            putExtra("ORDER_ID", transactionItem.orderId)
        }
        startActivity(intent)
    }
}
