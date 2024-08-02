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
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

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
        @SerialName("food_name") val foodName: String,
        @SerialName("category") val category: String,
        @SerialName("taste") val taste: String,
        @SerialName("price") val price: Double,
        @SerialName("quantity") val quantity: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerView = findViewById(R.id.recyclerViewCart)
        totalAmountTextView = findViewById(R.id.textViewTotalAmount)

        recyclerView.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(emptyList(), this::deleteItem)
        recyclerView.adapter = cartAdapter

        findViewById<Button>(R.id.btnGoBack).setOnClickListener {
            finish()
        }

        val btnConfirmOrder = findViewById<Button>(R.id.btnConfirmOrder)
        btnConfirmOrder.setOnClickListener {
            startActivity(Intent(this, Payment::class.java))
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Set the selected item to Cart
        bottomNavigationView.selectedItemId = R.id.action_cart

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    // Handle Home navigation
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_search -> {
                    // Handle Search navigation
                    true
                }
                R.id.action_cart -> {
                    // Navigate to CartActivity
                    val intent = Intent(this, CartActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_profile -> {
                    // Handle Profile navigation
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        loadCartItems()
    }

    private fun loadCartItems() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val orderid = randomCode
                val result = supabase.postgrest.from("cart").select(){
                    filter {
                        eq("order_id", orderid)
                    }
                }
                val items = result.decodeList<CartItem>()
                val totalAmount = items.sumOf { it.price * it.quantity }

                withContext(Dispatchers.Main) {
                    cartAdapter.updateItems(items)
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

                // Fetch user data from accounts_list table using the filter method
                val response = supabase.postgrest.from("cart").delete(){
                    filter {
                        //UserItem::userName eq username
                        //or
                        eq("cart_id", cartItem.cart_id)
                    }
                }

                // Reload cart items
                loadCartItems()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting cart item", e)
            }
        }
    }

    companion object {
        private const val TAG = "CartActivity"
    }
}
