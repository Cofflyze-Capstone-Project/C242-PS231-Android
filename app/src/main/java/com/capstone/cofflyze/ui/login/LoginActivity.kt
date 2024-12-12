package com.capstone.cofflyze.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.cofflyze.MainActivity
import com.capstone.cofflyze.R
import com.capstone.cofflyze.data.model.ApiClient
import com.capstone.cofflyze.data.model.ApiService
import com.capstone.cofflyze.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var eyeIcon: ImageView
    private lateinit var buttonLogin: Button
    private lateinit var buttonSignUp: Button
    private var isPasswordVisible = false
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Menyembunyikan action bar
        setContentView(R.layout.activity_login)

        // Inisialisasi FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Inisialisasi View
        editTextEmail = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        eyeIcon = findViewById(R.id.eyeIcon)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonSignUp = findViewById(R.id.buttonSignUp)

        // Listener untuk ikon mata (eyeIcon)
        eyeIcon.setOnClickListener {
            togglePasswordVisibility()
        }

        // Listener tombol Login
        buttonLogin.setOnClickListener {
            performLogin()
        }

        // Listener tombol Sign Up
        buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi untuk toggle visibility password
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Sembunyikan password
            editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            eyeIcon.setImageResource(R.drawable.ic_eye) // Ikon mata tertutup
        } else {
            // Tampilkan password
            editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            eyeIcon.setImageResource(R.drawable.ic_eye_black) // Ikon mata terbuka
        }
        isPasswordVisible = !isPasswordVisible

        // Pindahkan kursor ke akhir teks
        editTextPassword.setSelection(editTextPassword.text.length)
    }

    // Fungsi untuk login menggunakan Firebase Authentication
    private fun performLogin() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty()) {
            editTextEmail.error = "Email tidak boleh kosong"
            editTextEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            editTextPassword.error = "Password tidak boleh kosong"
            editTextPassword.requestFocus()
            return
        }

        // Menggunakan Firebase Authentication untuk login
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null && user.isEmailVerified) {
                        // Jika email sudah diverifikasi
                        checkUserProfile(user) // Pengecekan profil setelah login berhasil
                    } else {
                        // Email belum diverifikasi
                        Toast.makeText(
                            this,
                            "Email Anda belum diverifikasi. Silakan cek email Anda.",
                            Toast.LENGTH_SHORT
                        ).show()
                        firebaseAuth.signOut() // Keluar dari akun yang belum diverifikasi
                    }
                } else {
                    // Jika login gagal
                    Toast.makeText(this, "Email atau password salah!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Fungsi untuk mengecek profil pengguna melalui API
    private fun checkUserProfile(user: FirebaseUser) {
        val firebaseUid = user.uid

        // Gunakan ApiClient untuk mendapatkan instance ApiService
        val apiService = ApiClient.getApiClient().create(ApiService::class.java)
        val call = apiService.getAllUsers() // Endpoint untuk mendapatkan semua pengguna

        call.enqueue(object : Callback<List<UserProfile>> {
            override fun onResponse(call: Call<List<UserProfile>>, response: Response<List<UserProfile>>) {
                if (response.isSuccessful) {
                    val users = response.body()

                    if (users != null) {
                        // Periksa apakah UID pengguna ada di daftar pengguna
                        val userExists = users.any { it.tokenFirebase == firebaseUid }

                        if (userExists) {
                            // Jika UID ditemukan, arahkan ke MainActivity
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                            // Simpan status bahwa onboarding tidak perlu ditampilkan lagi
                            val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isOnboardingShown", true)  // Tandai onboarding sudah ditampilkan
                            editor.apply()
                        } else {
                            // Jika UID tidak ditemukan, arahkan ke ProfileActivity
                            val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        // Jika tidak ada pengguna yang ditemukan
                        Toast.makeText(
                            this@LoginActivity,
                            "Tidak ada data pengguna yang ditemukan.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Jika respons API tidak berhasil
                    Toast.makeText(
                        this@LoginActivity,
                        "Gagal mendapatkan data pengguna: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<UserProfile>>, t: Throwable) {
                // Gagal melakukan koneksi atau terjadi kesalahan jaringan
                Toast.makeText(
                    this@LoginActivity,
                    "Gagal terhubung ke server: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
