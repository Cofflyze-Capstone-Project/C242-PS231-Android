package com.capstone.cofflyze.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.cofflyze.R
import com.capstone.cofflyze.data.model.HistoryResponse

class HistoryAdapter(
    private val histories: List<HistoryResponse>,
    private val context: Context,
    private val onItemClickListener: (HistoryResponse) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = histories[position]
        holder.bind(history)
    }

    override fun getItemCount(): Int = histories.size

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        private val diseaseTextView: TextView = itemView.findViewById(R.id.textViewDisease)
        private val accuracyTextView: TextView = itemView.findViewById(R.id.textViewAccuracy)

        fun bind(history: HistoryResponse) {
            // Set data ke tampilan
            dateTextView.text = "Date: ${history.tanggal}"
            diseaseTextView.text = "Disease: ${history.nama_penyakit}"
            accuracyTextView.text = "Accuracy: ${history.akurasi}%"

            Glide.with(context)
                .load(history.gambar)
                .placeholder(R.drawable.ic_gallery)
                .error(R.drawable.ic_profile)
                .into(imageView)

            // Klik item
            itemView.setOnClickListener {
                onItemClickListener(history)
            }
        }
    }
}
