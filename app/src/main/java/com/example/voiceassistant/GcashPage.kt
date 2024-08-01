package com.example.voiceassistant

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class GcashPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gcash)

        /*gcash number*/
        val gcashNumber: EditText = findViewById(R.id.gcashNumber)

        /*confirm payment*/
        val btnConfirmPayment: Button = findViewById(R.id.gcashConfirm)
        btnConfirmPayment.setOnClickListener {
            startActivity(Intent(this, QrGenerator::class.java))
        }
    }
}