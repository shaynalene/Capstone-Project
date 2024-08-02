package com.example.voiceassistant

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.OtpType
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var etResetCode: EditText
    private lateinit var resetCodeInputLayout: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnUpdatePassword: Button


    private val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Postgrest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        etResetCode = findViewById(R.id.etResetCode)
        resetCodeInputLayout = findViewById(R.id.textInputLayoutResetCode)
        etNewPassword = findViewById(R.id.etNewPassword)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)

        btnUpdatePassword.setOnClickListener {
            val resetCode = etResetCode.text.toString()
            val newPassword = etNewPassword.text.toString()
            lifecycleScope.launch {
                if (validateResetCode(resetCode)) {
                    if (updatePassword(newPassword, resetCode)) {
                        Toast.makeText(this@ResetPasswordActivity, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity
                    } else {
                        Toast.makeText(this@ResetPasswordActivity, "Error updating password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ResetPasswordActivity, "Invalid or expired reset code", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun validateResetCode(resetCode: String): Boolean {
        return try {
            val response = supabase.postgrest.from("password_reset_tokens").select(columns = Columns.list("expires_at")) {
                filter {
                    eq("reset_code", resetCode)
                }
            }

            Log.d("ResetPasswordActivity", "Supabase response: ${response.data}")

        true
        } catch (e: Exception) {
            Log.e("ResetPasswordActivity", "Error validating reset code: ${e.message}", e)
            false
        }
    }

    private suspend fun updatePassword(newPassword: String, resetCode: String): Boolean {
        return try {
            // Fetch the email associated with the reset code
            val response = supabase.postgrest.from("password_reset_tokens").select(columns = Columns.list("email")) {
                filter {
                    eq("reset_code", resetCode)
                }
            }

            // Log the entire response data for debugging
            Log.d("ResetPasswordActivity", "Response data: ${response.data}")

            // Safely extract the email from the response data
            val email =  response.data
            val jsonString = """$email"""
            val regex = Regex("\"email\":\"(.*?)\"")
            val match = regex.find(jsonString)

            val emailfinal = match?.groups?.get(1)?.value ?: "No email found"

            Log.d("ResetPasswordActivity", "Response data: $emailfinal")

            // Update the password in the accounts_list table for the user with the provided email
                val updateResponse = supabase.postgrest.from("accounts_list").update(mapOf("password" to newPassword)) {
                    filter {
                        eq("email", emailfinal)
                    }
                }

                // Log the update response for debugging
                Log.d("ResetPasswordActivity", "Update response: ${updateResponse.data}")

                // Optionally, delete the reset code from the table after use
                supabase.postgrest.from("password_reset_tokens").delete {
                    filter {
                        eq("reset_code", resetCode)
                    }
                }
                true

        } catch (e: Exception) {
            Log.e("ResetPasswordActivity", "Error updating password: ${e.message}", e)
            false
        }
    }

}
