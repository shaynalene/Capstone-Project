package com.example.voiceassistant

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Payment : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment)

        /*back button*/
        val btnBack: TextView = findViewById(R.id.backButton)
        btnBack.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        // Function to update button colors
        fun updateButtonColors(selectedButton: Button, otherButton: Button) {
            selectedButton.setTextColor(Color.BLACK)
            otherButton.setTextColor(Color.WHITE)
        }

        /* Confirm order */
        val btnConfirmOrder: Button = findViewById(R.id.orderButton)
        val btnCashPayment: Button = findViewById(R.id.cashPayment)
        val btnGcashPayment: Button = findViewById(R.id.gcashPayment)
        var paymentMethod: String? = null

        btnCashPayment.setOnClickListener {
            paymentMethod = "cash"
            updateButtonColors(btnCashPayment, btnGcashPayment)
        }
        btnGcashPayment.setOnClickListener {
            paymentMethod = "gcash"
            updateButtonColors(btnGcashPayment, btnCashPayment)
        }

        btnConfirmOrder.setOnClickListener {
            /* confirm payment method */
            when (paymentMethod) {
                "cash" -> startActivity(Intent(this, QrGenerator::class.java))
                "gcash" -> startActivity(Intent(this, GcashPage::class.java))
                else -> {
                    // no payment method chosen
                    Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}