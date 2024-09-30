package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Properties
import javax.mail.*
import javax.mail.internet.*

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnResetPassword: Button

    private val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Postgrest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Correctly access the TextInputEditText directly
        etEmail = findViewById(R.id.etForgotEmail)
        btnResetPassword = findViewById(R.id.btnResetPassword)

        btnResetPassword.setOnClickListener {
            val email = etEmail.text.toString()
            lifecycleScope.launch {
                if (checkIfEmailExists(email)) {
                    if (generateResetCode(email)) {
                        Toast.makeText(this@ForgotPasswordActivity, "Password reset email sent!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, "Error sending password reset email", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, "Email not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private suspend fun checkIfEmailExists(email: String): Boolean {
        return try {
            val response = supabase.postgrest.from("accounts_list").select(columns = Columns.list("email")) {
                filter {
                    eq("email", email)
                }
            }

            val existingemail = response.data
            val regex = """"email":"(.*?)"""".toRegex()
            val matchResult = regex.find(existingemail)
            val etdexistingemail = matchResult?.groupValues?.get(1)


            if(etdexistingemail == email) {
                return true
            }
            Log.d("ForgotPasswordActivity", "FORGOT EMAIL: $etdexistingemail")

            false
        } catch (e: Exception) {
            Toast.makeText(this, "Error checking email: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private suspend fun generateResetCode(email: String): Boolean {
        val resetCode = generateRandomCode()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, 1) // Set expiration to 1 hour from now
        val expiresAt = calendar.time

        return try {
            val expiresAtFormatted = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(expiresAt)
            val data = mapOf(
                "email" to email,
                "reset_code" to resetCode,
                "expires_at" to expiresAtFormatted
            )

            Log.d("ForgotPasswordActivity", "Inserting data: $data")

            // Insert the data into Supabase
            val response = supabase.postgrest.from("password_reset_tokens").insert(data)

            // Check if the data was inserted successfully
            if (response.data != null && response.data.isNotEmpty()) {
                sendResetCodeEmail(email, resetCode) // Function to send email

                val intent = Intent(this, ResetPasswordActivity::class.java)
                startActivity(intent)

                true
            } else {
                sendResetCodeEmail(email, resetCode) // Function to send email

                val intent = Intent(this, ResetPasswordActivity::class.java)
                startActivity(intent)

                true
            }
        } catch (e: Exception) {
            Log.e("ForgotPasswordActivity", "Error generating reset code: ${e.message}", e)
            Toast.makeText(this, "Error generating reset code: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun generateRandomCode(): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..6).map { chars.random() }.joinToString("")
    }

    private suspend fun sendResetCodeEmail(email: String, resetCode: String) {
        withContext(Dispatchers.IO) {
            val properties = Properties().apply {
                put("mail.smtp.host", "smtp.sendgrid.net")
                put("mail.smtp.port", "587")
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
            }

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication("apikey", "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") // CHANGE THE PASSWORD WITH THE API KEY
                }
            })

            try {
                val message = MimeMessage(session)
                message.setFrom(InternetAddress("shaynalene.pabalate@adamson.edu.ph"))
                message.addRecipient(MimeMessage.RecipientType.TO, InternetAddress(email))
                message.subject = "Your Password Reset Code"
                message.setText("Thank you for choosing Mcdo! " +
                        "Your password reset code is: $resetCode")

                Transport.send(message)
                Log.d("ForgotPasswordActivity", "Sent message successfully....")
            } catch (e: Exception) {
                Log.e("ForgotPasswordActivity", "Error sending email: ${e.message}", e)
            }
        }
    }

}
