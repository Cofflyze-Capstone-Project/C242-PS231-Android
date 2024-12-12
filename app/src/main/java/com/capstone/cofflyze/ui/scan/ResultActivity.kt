package com.capstone.cofflyze.ui.scan

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.capstone.cofflyze.MainActivity
import com.capstone.cofflyze.data.model.ApiClient
import com.capstone.cofflyze.databinding.ActivityResultBinding
import com.capstone.cofflyze.data.model.ApiService
import com.capstone.cofflyze.data.model.HistoryRequest
import com.google.firebase.auth.FirebaseAuth
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var apiService: ApiService
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth and ApiService
        firebaseAuth = FirebaseAuth.getInstance()
        val retrofit = Retrofit.Builder()
        apiService = ApiClient.getApiClient().create(ApiService::class.java)

        // Get data from Intent
        val confidence = intent.getStringExtra("confidence")
        val deskripsi = intent.getStringExtra("deskripsi")
        val faktorRisiko = intent.getStringExtra("faktor_risiko")
        val gejala = intent.getStringExtra("gejala")
        val imageUriString = intent.getStringExtra("image_uri") // image URI from uCrop
        val penanganan = intent.getStringExtra("penanganan")
        val pencegahan = intent.getStringExtra("pencegahan")
        val penyakit = intent.getStringExtra("penyakit")
        val penyebab = intent.getStringExtra("penyebab")

        // Load the image from the URI if available
        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            Glide.with(this)
                .load(imageUri)
                .into(binding.resultImageView)
        }

        // Display data
        displayResultData(confidence, deskripsi, faktorRisiko, gejala, penanganan, pencegahan, penyakit, penyebab, imageUriString)

        // Set click listener for the "Save" button
        binding.btnSave.setOnClickListener {
            saveHistory(imageUriString, confidence, deskripsi, faktorRisiko, gejala, penanganan, pencegahan, penyakit, penyebab)
        }
    }

    private fun displayResultData(
        confidence: String?,
        deskripsi: String?,
        faktorRisiko: String?,
        gejala: String?,
        penanganan: String?,
        pencegahan: String?,
        penyakit: String?,
        penyebab: String?,
        imageUrl: String?
    ) {
        binding.penyakitTextView.text = "Penyakit: $penyakit"
        binding.confidenceTextView.text = "Confidence: $confidence"
        binding.gejalaTextView.text = "Gejala: $gejala"
        binding.deskripsiTextView.text = "Deskripsi: $deskripsi"
        binding.faktorRisikoTextView.text = "Faktor Risiko: $faktorRisiko"
        binding.penangananTextView.text = "Penanganan: $penanganan"
        binding.pencegahanTextView.text = "Pencegahan: $pencegahan"
        binding.penyebabTextView.text = "Penyebab: $penyebab"
    }

    private fun saveHistory(
        imageUrl: String?,
        confidence: String?,
        deskripsi: String?,
        faktorRisiko: String?,
        gejala: String?,
        penanganan: String?,
        pencegahan: String?,
        penyakit: String?,
        penyebab: String?
    ) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null || imageUrl.isNullOrEmpty()) {
            showToast("User or image data is missing!")
            return
        }

        val historyRequest = HistoryRequest(
            gambar = imageUrl ?: "coba",
            akurasi = confidence ?: "Unknown",
            nama_penyakit = penyakit ?: "Unknown",
            deskripsi = deskripsi ?: "No description available",
            penyebab = penyebab ?: "Unknown",
            gejala = gejala ?: "No symptoms provided",
            faktor_risiko = faktorRisiko ?: "No risk factors provided",
            penanganan = penanganan ?: "No treatment provided",
            pencegahan = pencegahan ?: "No prevention provided",
            tokenFirebase = currentUser.uid
        )

        apiService.saveHistory(historyRequest).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    navigateToHome()
                } else {
                    showToast("Failed to save history: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra("navigate_to", "home")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
        finish()
    }
}
