package com.example.voiceassistant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceassistant.TransactionPage.TransactionItem

class TransactionAdapter(
    private var items: List<TransactionItem>,
    private val onViewButtonClick: (TransactionItem) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = items[position]
        holder.orderIdTransactionView.text = item.orderId.toString()
        holder.totalTransactionView.text = item.price.toString()
        holder.paymentTransactionView.text = item.paymentStatus

        holder.viewButton.setOnClickListener {
            onViewButtonClick(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateItems(newItems: List<TransactionItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val orderIdTransactionView: TextView = itemView.findViewById(R.id.orderIdTransaction)
        val totalTransactionView: TextView = itemView.findViewById(R.id.totalTransaction)
        val paymentTransactionView: TextView = itemView.findViewById(R.id.paymentTransaction)
        val viewButton: Button = itemView.findViewById(R.id.viewButton) // Assuming this is your "View" button
    }
}
