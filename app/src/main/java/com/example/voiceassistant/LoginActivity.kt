package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
        @SerialName("id") val id: String,
        @SerialName("user_type") val userType: String
    )

    // Define the Supabase client
    private val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Auth) {
            alwaysAutoRefresh = false
            autoLoadFromStorage = false
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
            // Redirect to RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        findViewById<TextView>(R.id.textForgot).setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
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
                    eq("username", username)
                }
            }

            //USERDATA: USERNAME
            //USERDATA2: PASSWORD
            //USERDATA3: UUID

            // Process the fetched user data
            val userData = response.data
            Log.d("LoginActivity", "User data: $userData")

            // Fetch user data from accounts_list table using the filter method
            val response2 = supabase.postgrest.from("accounts_list").select(columns = Columns.list("password")){
                filter {
                    eq("username", username)
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
                        eq("username", username)
                    }
                }

                val userData3 = response3.data // This contains the user data from accounts_list
                Log.d("LoginActivity", "User data: $userData3")

                // Store the UUID in the UserData singleton
                UserData.uuid = userData3

                // Proceed with successful data fetch

                var user_type = supabase.postgrest.from("accounts_list")
                    .select(columns = Columns.list("user_type")) {
                        filter {
                            eq("username", username)
                        }
                    }

                val inputUT = user_type.data
                val regex3 = """"user_type":"(.*?)"""".toRegex()
                val matchResult3 = regex3.find(inputUT)
                val userType = matchResult3?.groupValues?.get(1)

                if (userType == "user") {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else if (userType == "admin") {
                    val intent = Intent(this, QrScanner::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, user_type.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this, "Incorrect credentials.", Toast.LENGTH_SHORT).show()
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
