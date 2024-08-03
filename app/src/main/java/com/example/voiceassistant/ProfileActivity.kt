package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.regex.Pattern

class ProfileActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etMiddleName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etBirthDate: TextInputEditText
    private lateinit var etContactNumber: TextInputEditText
    private lateinit var etEmailAddress: TextInputEditText
    private lateinit var btnEdit1: Button
    private lateinit var btnLogout: Button

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
        @SerialName("firstname") val firstName: String,
        @SerialName("middlename") val middleName: String,
        @SerialName("lastname") val lastName: String,
        @SerialName("birthday") val birthDate: String,
        @SerialName("contact_number") val contactNumber: Long,
        @SerialName("email") val emailAddress: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etUsername = findViewById(R.id.etUsername)
        etFirstName = findViewById(R.id.etFirstName)
        etMiddleName = findViewById(R.id.etMiddleName)
        etLastName = findViewById(R.id.etLastName)
        etBirthDate = findViewById(R.id.etBirthDate)
        etContactNumber = findViewById(R.id.etContactNumber)
        etEmailAddress = findViewById(R.id.etEmailAddress)
        btnEdit1 = findViewById(R.id.btnEdit)
        btnLogout = findViewById(R.id.btnLogout)


        //val userId = intent.getStringExtra("USER_ID") ?: return


        // In MainActivity or any other activity
        val userId= LoginActivity.UserData.uuid
        Log.d("MainActivity", "User UUID: $userId")

        val userId2 = extractUuid(userId) ?: "default-uuid-value"
        Log.d("MainActivity", "User UUID2: $userId2")

        lifecycleScope.launch {
            loadUserProfile(userId2)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigationView.selectedItemId = R.id.action_profile

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.action_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.action_profile -> {
                    // Already on Profile, no action needed
                    true
                }
                else -> false
            }
        }

        btnEdit1.setOnClickListener {
            // Toggle editable state
            toggleEditable()
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun extractUuid(jsonString: String): String? {
        val pattern = Pattern.compile("\"id\":\"([a-fA-F0-9-]{36})\"")
        val matcher = pattern.matcher(jsonString)
        return if (matcher.find()) {
            matcher.group(1)  // Group 1 contains the UUID
        } else {
            null
        }
    }

    private fun toggleEditable() {
        // Check if fields are currently enabled
        val isEditable = !etFirstName.isEnabled

        // Set fields to editable or non-editable
        //etUsername.isEnabled = isEditable
        etFirstName.isEnabled = isEditable
        etMiddleName.isEnabled = isEditable
        etLastName.isEnabled = isEditable
        etBirthDate.isEnabled = isEditable
        etContactNumber.isEnabled = isEditable
        //etEmailAddress.isEnabled = isEditable

        if (isEditable) {
            btnEdit1.text = "Save" // Change button text to Save when fields are editable
        } else {
            // Save changes to Supabase
            val updatedUser = RegisterUser(
                id = extractUuid(LoginActivity.UserData.uuid) ?: "default-uuid-value",
                userName = etUsername.text.toString(),
                firstName = etFirstName.text.toString(),
                middleName = etMiddleName.text.toString(),
                lastName = etLastName.text.toString(),
                birthDate = etBirthDate.text.toString(),
                contactNumber = etContactNumber.text.toString().toLong(),
                emailAddress = etEmailAddress.text.toString()
            )
            lifecycleScope.launch {
                updateUserProfile(updatedUser)
            }
            btnEdit1.text = "Edit" // Change button text back to Edit after saving
        }
    }

    private fun logoutUser() {
        lifecycleScope.launch {
            try {
                // Use the activity context explicitly
                val context = this@ProfileActivity

                // Redirect to LoginActivity
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                finish()
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }


    private suspend fun updateUserProfile(user: RegisterUser) {
        withContext(Dispatchers.IO) {
            try {
                val response = supabase.postgrest.from("accounts_list").update(user) {
                    filter {
                        eq("id", user.id)
                    }
                }
                Log.d("ProfileActivity", "Update Response: $response")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error updating user profile", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error updating profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun loadUserProfile(userId2: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = supabase.postgrest.from("accounts_list").select() {
                    filter {
                        eq("id", userId2)
                    }

                }
                // Debugging response
                Log.d("ProfileActivity", "USER ID: $userId2")
                Log.d("ProfileActivity", "Response: $response")

                val items = response.decodeList<RegisterUser>()

                // Debugging items
                Log.d("ProfileActivity", "Fetched Items: $items")

                withContext(Dispatchers.Main) {
                    val user = items.firstOrNull()
                    if (user != null) {
                        etUsername.setText(user.userName)
                        etFirstName.setText(user.firstName)
                        etMiddleName.setText(user.middleName)
                        etLastName.setText(user.lastName)
                        etBirthDate.setText(user.birthDate)
                        etContactNumber.setText(user.contactNumber.toString())
                        etEmailAddress.setText(user.emailAddress)
                    } else {
                        Toast.makeText(this@ProfileActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Error loading user profile", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error loading user profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
