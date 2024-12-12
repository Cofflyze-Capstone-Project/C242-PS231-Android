package com.capstone.cofflyze.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.capstone.cofflyze.R
import com.capstone.cofflyze.data.model.ApiClient
import com.capstone.cofflyze.data.model.ApiService
import com.capstone.cofflyze.data.model.HistoryResponse
import com.capstone.cofflyze.databinding.FragmentHistoryDetailBinding
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryDetailFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var _binding: FragmentHistoryDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryDetailBinding.inflate(inflater, container, false)

        // Mendapatkan history yang dikirim dari fragment sebelumnya
        val selectedHistory = arguments?.getParcelable<HistoryResponse>("historyDetail")
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null && selectedHistory != null) {
            val tokenFirebase = currentUser.uid
            fetchHistoryDetail(tokenFirebase, selectedHistory.id_history)
        } else {
            showError("Failed to load details")
        }

        return binding.root
    }

    private fun fetchHistoryDetail(tokenFirebase: String, idHistory: Int) {
        val apiService = ApiClient.getApiClient().create(ApiService::class.java)
        apiService.getHistoryDetail(tokenFirebase, idHistory).enqueue(object : Callback<List<HistoryResponse>> {
            override fun onResponse(call: Call<List<HistoryResponse>>, response: Response<List<HistoryResponse>>) {
                if (response.isSuccessful) {
                    val historyList = response.body()
                    if (!historyList.isNullOrEmpty()) {
                        val selectedHistory = historyList.first() // Ambil history pertama dari list jika ada
                        populateDetails(selectedHistory)
                    } else {
                        showError("No details available")
                    }
                } else {
                    showError("Failed to load details: ${response.code()}")
                    Log.e("HistoryDetailFragment", "API Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<HistoryResponse>>, t: Throwable) {
                showError("Error: ${t.message}")
                Log.e("HistoryDetailFragment", "Failed to fetch details: ${t.message}")
            }
        })
    }

    private fun populateDetails(historyDetail: HistoryResponse) {
        // Menampilkan data ke dalam TextView dan ImageView
        binding.tvDiseaseName.text = "Penyakit: ${historyDetail.nama_penyakit}"
        binding.tvAccuracy.text = "Confidence: ${historyDetail.akurasi}"
        binding.tvDescription.text = "Deskripsi: ${historyDetail.deskripsi}"
        binding.tvRiskFactors.text = "Faktor Risiko: ${historyDetail.faktor_risiko}"
        binding.tvSymptoms.text = "Gejala: ${historyDetail.gejala}"
        binding.tvCauses.text = "Penyebab: ${historyDetail.penyebab}"
        binding.tvHandling.text = "Penanganan: ${historyDetail.penanganan}"
        binding.tvPrevention.text = "Pencegahan: ${historyDetail.pencegahan}"
        binding.tvDate.text = "Tanggal: ${historyDetail.tanggal}"

        Glide.with(requireContext())
            .load(historyDetail.gambar)
            .placeholder(R.drawable.ic_gallery)
            .error(R.drawable.ic_profile)
            .into(binding.ivDiseaseImage)
    }

    private fun showError(message: String) {
        // Menampilkan pesan error di Toast
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear binding reference to prevent memory leaks
    }
}
