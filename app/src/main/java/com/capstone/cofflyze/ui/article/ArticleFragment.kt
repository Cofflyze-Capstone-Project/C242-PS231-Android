package com.capstone.cofflyze.ui.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.cofflyze.R
import com.capstone.cofflyze.data.model.ApiClient
import com.capstone.cofflyze.data.model.ApiService
import com.capstone.cofflyze.ui.home.Article
import com.capstone.cofflyze.ui.home.ArticleSliderAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArticleFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.article_fragment, container, false)

        // Inisialisasi RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewArticles)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Memuat artikel
        fetchArticles()

        return view
    }

    private fun navigateToArticleDetail(article: Article) {
        // Membuat Bundle untuk mengirimkan data artikel
        val bundle = Bundle().apply {
            putString("imageUrl", article.foto)
            putString("title", article.judul)
            putString("description", article.article)
        }

        // Menggunakan NavController untuk melakukan navigasi
        findNavController().navigate(R.id.action_article_to_articleDetail, bundle)
    }

    private fun fetchArticles() {
        val apiService = ApiClient.getApiClient().create(ApiService::class.java)
        apiService.getArticles().enqueue(object : Callback<List<Article>> {
            override fun onResponse(call: Call<List<Article>>, response: Response<List<Article>>) {
                if (response.isSuccessful) {
                    // Mengambil daftar artikel dari respons
                    val articles = response.body() ?: emptyList()

                    // Menetapkan adapter ke RecyclerView
                    articleAdapter = ArticleAdapter(articles, requireContext()) { article ->
                        // Menangani klik item artikel
                            navigateToArticleDetail(article)
                    }
                    recyclerView.adapter = articleAdapter


                } else {
                    // Menangani jika respons tidak berhasil
                    Toast.makeText(context, "Failed to load articles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Article>>, t: Throwable) {
                // Menangani kesalahan saat koneksi API gagal
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
