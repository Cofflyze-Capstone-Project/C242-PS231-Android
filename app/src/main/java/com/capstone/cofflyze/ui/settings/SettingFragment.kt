package com.capstone.cofflyze.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.capstone.cofflyze.R
import com.capstone.cofflyze.data.model.ApiClient
import com.capstone.cofflyze.data.model.ApiService
import com.capstone.cofflyze.data.model.UserProfile
import com.capstone.cofflyze.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var apiService: ApiService

    // UI Elements
    private lateinit var photoProfile: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var editProfileButton: Button  // Button untuk edit profile

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)

        // Inisialisasi FirebaseAuth dan ApiService
        firebaseAuth = FirebaseAuth.getInstance()
        apiService = ApiClient.getApiClient().create(ApiService::class.java)

        // Bind views
        photoProfile = view.findViewById(R.id.photoProfile)
        nameTextView = view.findViewById(R.id.edit_text_name)
        emailTextView = view.findViewById(R.id.edit_text_email)
        phoneTextView = view.findViewById(R.id.edit_text_no)
        genderTextView = view.findViewById(R.id.edit_text_gender)
        locationTextView = view.findViewById(R.id.edit_text_location)
        logoutButton = view.findViewById(R.id.buttonLogout)
        editProfileButton = view.findViewById(R.id.buttonEditProfile)  // Inisialisasi tombol edit profil

        // Get current Firebase user
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            fetchUserProfile(currentUser)
        } else {
            // Jika user tidak terautentikasi atau email belum diverifikasi
            Toast.makeText(
                requireContext(),
                "Login diperlukan atau email belum diverifikasi",
                Toast.LENGTH_SHORT
            ).show()
            navigateToLogin()
        }

        // Handle logout button click
        logoutButton.setOnClickListener {
            firebaseAuth.signOut()

            // Hapus status login di SharedPreferences
            val sharedPreferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isLoggedIn", false) // Tandai pengguna sebagai belum login
            editor.apply()

            Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }

        // Handle edit profile button click
        editProfileButton.setOnClickListener {
            // Navigasi ke UpdateFragment untuk mengedit profil
            findNavController().navigate(R.id.action_settingFragment_to_updateFragment)
        }

        return view
    }

    // Fungsi untuk mengambil data profil pengguna melalui API
    private fun fetchUserProfile(user: FirebaseUser) {
        val firebaseUid = user.uid

        // Panggil API untuk mendapatkan daftar pengguna
        apiService.getAllUsers().enqueue(object : Callback<List<UserProfile>> {
            override fun onResponse(
                call: Call<List<UserProfile>>,
                response: Response<List<UserProfile>>
            ) {
                if (response.isSuccessful) {
                    val users = response.body()

                    if (users != null) {
                        val userProfile = users.find { it.tokenFirebase == firebaseUid }

                        if (userProfile != null) {
                            displayUserProfile(userProfile, user)  // Pass FirebaseUser to displayUserProfile
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Profil pengguna tidak ditemukan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Data pengguna tidak ditemukan",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat data profil", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<UserProfile>>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Kesalahan jaringan: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Fungsi untuk menampilkan data profil pengguna di UI
    private fun displayUserProfile(userProfile: UserProfile, currentUser: FirebaseUser) {
        nameTextView.text = userProfile.namaLengkap ?: "Nama tidak tersedia"
        emailTextView.text = currentUser.email ?: "Email tidak tersedia"
        phoneTextView.text = userProfile.nomorHp ?: "Nomor HP tidak tersedia"
        genderTextView.text = userProfile.jenisKelamin ?: "Jenis kelamin tidak tersedia"
        locationTextView.text = userProfile.alamat ?: "Alamat tidak tersedia"

        // Menampilkan foto profil menggunakan Glide
        Glide.with(this)
            .load(userProfile.fotoProfile ?: R.drawable.ic_profile_placeholder)
            .placeholder(R.drawable.ic_profile_placeholder)
            .circleCrop()
            .into(photoProfile)
    }

    // Fungsi untuk navigasi ke LoginActivity
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
