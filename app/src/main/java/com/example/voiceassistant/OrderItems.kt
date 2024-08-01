package com.example.voiceassistant

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class OrderItems : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_items)

        /*order id*/
        val orderId = intent.getStringExtra("RESULT_TEXT")

        /*retrieve data*/
        val username: TextView = findViewById(R.id.userName)
        val orderItems: RecyclerView = findViewById(R.id.orderItems)
        val totalAmount: TextView = findViewById(R.id.totalAmount)
        val paymentStatus: TextView = findViewById(R.id.paymentStatus)

        // Example usage: setting the result text to the username TextView
        username.text = orderId

    }

}