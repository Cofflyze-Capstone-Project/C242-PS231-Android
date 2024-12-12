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

class ArticleSliderAdapter(
    private val articles: List<Article>,
    private val context: Context,
    private val onItemClickListener: (Article) -> Unit
) : RecyclerView.Adapter<ArticleSliderAdapter.ArticleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articles[position]
        holder.bind(article)
    }

    override fun getItemCount(): Int = articles.size

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.articleImage)
        private val titleTextView: TextView = itemView.findViewById(R.id.articleTitle)

        fun bind(article: Article) {
            // Set data ke tampilan
            titleTextView.text = article.judul
            Glide.with(context)
                .load(article.foto)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(imageView)

            // Set listener untuk item click
            itemView.setOnClickListener {
                onItemClickListener(article)
            }
        }
    }
}

