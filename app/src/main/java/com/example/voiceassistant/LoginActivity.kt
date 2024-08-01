package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {

    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText

    @Serializable
    data class UserItem(
        @SerialName("username") val userName: String,
        @SerialName("password") val password: String,
        @SerialName("id") val id: String
    )

    // Define the Supabase client
    private val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Auth) {
            alwaysAutoRefresh = false // default: true
            autoLoadFromStorage = false // default: true
            // and more...
        }
        install(Postgrest)
    }

    companion object {
        var randomCode: String = ""
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            lifecycleScope.launch {
                generateRandomCode()
                fetchUserData(username, password)
            }
        }

        btnRegister.setOnClickListener {
            Toast.makeText(this, "Register functionality not yet implemented", Toast.LENGTH_SHORT).show()
        }
    }


    private fun generateRandomCode() {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        randomCode = (1..10).map { chars.random() }.joinToString("")
        Log.d("LoginActivity", "Generated random code: $randomCode")
    }


    private suspend fun fetchUserData(username: String, password: String) {
        try {

            // Fetch user data from accounts_list table using the filter method
            val response = supabase.postgrest.from("accounts_list").select(columns = Columns.list("username")){
                filter {
                    //UserItem::userName eq username
                    //or
                    eq("username", username)
                }
            }

            //USERDATA: EMAIL
            //USERDATA2: PASSWORD
            //USERDATA3: UUID

            // Process the fetched user data
            val userData = response.data // This contains the user data from accounts_list
            Log.d("LoginActivity", "User data: $userData")

            // Fetch user data from accounts_list table using the filter method
            val response2 = supabase.postgrest.from("accounts_list").select(columns = Columns.list("password")){
                filter {
                    //UserItem::userName eq username
                    //or
                    eq("password", password)
                }
            }

            // Process the fetched user data
            val userData2 = response2.data // This contains the user data from accounts_list
            Log.d("LoginActivity", "User data: $userData2")

            if((userData == "[{\"username\":\"$username\"}]") && (userData2 == "[{\"password\":\"$password\"}]")){

                // Fetch user data from accounts_list table using the filter method
                val response3 = supabase.postgrest.from("accounts_list").select(columns = Columns.list("id")){
                    filter {
                        //UserItem::userName eq username
                        //or
                        eq("username", username)
                    }
                }

                val userData3 = response3.data // This contains the user data from accounts_list
                Log.d("LoginActivity", "User data: $userData3")

                // Store the UUID in the UserData singleton
                UserData.uuid = userData3

                // Proceed with successful data fetch
                Toast.makeText(this, "Data fetched successfully!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            else{
                Toast.makeText(this, "Incorrect credentials", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("LoginActivity", "Error during data fetch: ${e.message}")
            Toast.makeText(this, "Error during data fetch: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    object UserData {
        var uuid = ""
    }

}
