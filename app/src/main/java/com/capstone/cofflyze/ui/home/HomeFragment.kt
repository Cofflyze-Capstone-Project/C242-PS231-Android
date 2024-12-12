package com.capstone.cofflyze.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.capstone.cofflyze.R
import com.capstone.cofflyze.data.model.ApiClient
import com.capstone.cofflyze.data.model.ApiService
import com.capstone.cofflyze.data.model.HistoryResponse
import com.capstone.cofflyze.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var apiService: ApiService

    // UI Elements
    private lateinit var usernameText: TextView
    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var viewPager: ViewPager2
    private lateinit var profileImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize FirebaseAuth and ApiService
        firebaseAuth = FirebaseAuth.getInstance()
        apiService = ApiClient.getApiClient().create(ApiService::class.java)

        // Bind views
        usernameText = view.findViewById(R.id.edit_text_name)
        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory)
        viewPager = view.findViewById(R.id.viewPager)
        profileImage = view.findViewById(R.id.profileImage)

        // Set up RecyclerView for history
        recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())

        // Set up ViewPager
        setupViewPager()

        // Get current Firebase user
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            fetchUserProfile(currentUser)
            fetchHistoryData(currentUser.uid)
        } else {
            Toast.makeText(
                requireContext(),
                "Login diperlukan atau email belum diverifikasi",
                Toast.LENGTH_SHORT
            ).show()
        }

        return view
    }

    private fun fetchUserProfile(user: FirebaseUser) {
        val firebaseUid = user.uid

        apiService.getAllUsers().enqueue(object : Callback<List<UserProfile>> {
            override fun onResponse(
                call: Call<List<UserProfile>>,
                response: Response<List<UserProfile>>
            ) {
                if (response.isSuccessful) {
                    val users = response.body()
                    val userProfile = users?.find { it.tokenFirebase == firebaseUid }

                    if (userProfile != null) {
                        displayUserProfile(userProfile)
                    } else {
                        showToast("Profil pengguna tidak ditemukan")
                    }
                } else {
                    showToast("Gagal memuat data profil")
                }
            }

            override fun onFailure(call: Call<List<UserProfile>>, t: Throwable) {
                showToast("Kesalahan jaringan: ${t.message}")
            }
        })
    }

    private fun displayUserProfile(userProfile: UserProfile) {
        usernameText.text = userProfile.namaLengkap ?: "Nama tidak tersedia"

        Glide.with(requireContext())
            .load(userProfile.fotoProfile)
            .placeholder(R.drawable.ic_profile_placeholder)
            .error(R.drawable.ic_profile_placeholder)
            .circleCrop()
            .into(profileImage)
    }

    private fun setupViewPager() {
        apiService.getArticles().enqueue(object : Callback<List<Article>> {
            override fun onResponse(call: Call<List<Article>>, response: Response<List<Article>>) {
                if (response.isSuccessful) {
                    val articles = response.body() ?: emptyList()

                    val adapter = ArticleSliderAdapter(articles, requireContext()) { article ->
                        navigateToArticleDetail(article)
                    }
                    viewPager.adapter = adapter
                } else {
                    showToast("Gagal memuat artikel")
                }
            }

            override fun onFailure(call: Call<List<Article>>, t: Throwable) {
                showToast("Kesalahan jaringan: ${t.message}")
            }
        })
    }

    private fun navigateToArticleDetail(article: Article) {
        val bundle = Bundle().apply {
            putString("imageUrl", article.foto)
            putString("title", article.judul)
            putString("description", article.article)
        }

        findNavController().navigate(R.id.action_home_to_articleDetail, bundle)
    }

    private fun fetchHistoryData(tokenFirebase: String) {
        apiService.getHistoryByTokenFirebase(tokenFirebase).enqueue(object : Callback<List<HistoryResponse>> {
            override fun onResponse(
                call: Call<List<HistoryResponse>>,
                response: Response<List<HistoryResponse>>
            ) {
                if (response.isSuccessful) {
                    val historyList = response.body() ?: emptyList()

                    if (historyList.isNotEmpty()) {
                        recyclerViewHistory.adapter = HistoryAdapter(historyList, requireContext()) { selectedHistory ->
                            val navOptions = NavOptions.Builder()
                                .setEnterAnim(R.anim.fragment_fade_in)
                                .setExitAnim(R.anim.fragment_fade_out)
                                .setPopEnterAnim(R.anim.fragment_fade_in)
                                .setPopExitAnim(R.anim.fragment_fade_out)
                                .build()

                            val bundle = Bundle().apply {
                                putParcelable("historyDetail", selectedHistory)
                            }

                            findNavController().navigate(
                                R.id.historyDetailFragment,
                                bundle,
                                navOptions
                            )
                        }
                    } else {
                        showToast("No history data available")
                    }
                } else {
                    showToast("Failed to load history: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<HistoryResponse>>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
