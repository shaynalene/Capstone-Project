package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
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


        findViewById<BottomNavigationView>(R.id.bottomNavigation).setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.action_home -> {
                    // Handle home action
                    true
                }
                R.id.action_cart -> {
                    // Handle cart action
                    true
                }
                R.id.action_profile -> {
                    // Handle profile action
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
                val result = supabase.postgrest.from("cart").select()
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
                        eq("food_name", cartItem.foodName)
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
