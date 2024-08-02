package com.example.voiceassistant

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var tvFirstName: TextView
    private lateinit var tvMiddleName: TextView
    private lateinit var tvLastName: TextView
    private lateinit var tvBirthDate: TextView
    private lateinit var tvContactNumber: TextView
    private lateinit var tvEmailAddress: TextView

    private val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Postgrest)
    }

    @Serializable
    data class RegisterUser(
        @SerialName("id") val id: String,
        @SerialName("username") val userName: String,
        @SerialName("password") val password: String,
        @SerialName("firstname") val firstName: String,
        @SerialName("middlename") val middleName: String,
        @SerialName("lastname") val lastName: String,
        @SerialName("birthday") val birthDate: String,
        @SerialName("contact_number") val contactNumber: String,
        @SerialName("email") val emailAddress: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvUsername = findViewById(R.id.tvUsername)
        tvFirstName = findViewById(R.id.tvFirstName)
        tvMiddleName = findViewById(R.id.tvMiddleName)
        tvLastName = findViewById(R.id.tvLastName)
        tvBirthDate = findViewById(R.id.tvBirthDate)
        tvContactNumber = findViewById(R.id.tvContactNumber)
        tvEmailAddress = findViewById(R.id.tvEmailAddress)

        val userId = intent.getStringExtra("USER_ID") ?: return

        lifecycleScope.launch {
            loadUserProfile(userId)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Set the selected item to Cart
        bottomNavigationView.selectedItemId = R.id.action_profile

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
    }

    private suspend fun loadUserProfile(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = supabase.postgrest.from("accounts_list").select(){
                    filter {
                        eq("id", userId)
                    }
                }
                val items = response.decodeList<RegisterUser>()

                withContext(Dispatchers.Main) {
                    val user = response.data as RegisterUser
                    tvUsername.text = user.userName
                    tvFirstName.text = user.firstName
                    tvMiddleName.text = user.middleName
                    tvLastName.text = user.lastName
                    tvBirthDate.text = user.birthDate
                    tvContactNumber.text = user.contactNumber
                    tvEmailAddress.text = user.emailAddress
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading cart items", e)
            }
        }


    }
}
