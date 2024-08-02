package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
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
import java.nio.file.DirectoryStream
import java.util.logging.Filter

class GcashPage : AppCompatActivity() {

    val supabase = createSupabaseClient(
        supabaseUrl = "https://uwhuzbxzexkldttxxeee.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InV3aHV6Ynh6ZXhrbGR0dHh4ZWVlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjIzNDQ0OTUsImV4cCI6MjAzNzkyMDQ5NX0.vIlTT6qLZkwjd3FY0sCx8UKzkHlsxjPXykv5Xy63vQw"
    ) {
        install(Postgrest)
    }

    @Serializable
    data class Payment(
        @SerialName("payment_status") val paymentStatus: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gcash)

        /*gcash number*/
        val gcashNumber: EditText = findViewById(R.id.gcashNumber)

        /*confirm payment*/
        val btnConfirmPayment: Button = findViewById(R.id.gcashConfirm)
        btnConfirmPayment.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val orderid = randomCode
                    // Update payment status
                    val response = supabase.postgrest.from("cart").update(Payment(paymentStatus = "Paid: Gcash"))
                    {
                        filter {
                            eq("order_id", orderid)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        // Log success and navigate to the next activity
                        Log.d("GcashPage", "Payment status updated: $response")
                        startActivity(Intent(this@GcashPage, QrGenerator::class.java))
                    }
                } catch (e: Exception) {
                    // Handle exception (e.g., show user feedback)
                    Log.e("GcashPage", "Error updating payment status", e)
                }
            }
        }
    }
}
