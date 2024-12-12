package com.capstone.cofflyze.ui.login

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.capstone.cofflyze.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonSignUp: Button
    private lateinit var buttonBack: Button
    private lateinit var confirmPasswordToggle: ImageView
    private lateinit var passwordToggle: ImageView
    private lateinit var firebaseAuth: FirebaseAuth
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Inisialisasi FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Inisialisasi UI components
        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonSignUp = findViewById(R.id.buttonSignUp)
        buttonBack = findViewById(R.id.buttonBack)
        confirmPasswordToggle = findViewById(R.id.confirmPasswordToggle)
        passwordToggle = findViewById(R.id.passwordToggle)

        // Fungsi untuk sign up
        buttonSignUp.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    // Panggil metode sign up Firebase
                    firebaseSignUp(username, password)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Fungsi untuk toggle password visibility
        passwordToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        confirmPasswordToggle.setOnClickListener {
            toggleConfirmPasswordVisibility()
        }

        buttonBack.setOnClickListener {
            finish() // Menutup aktivitas dan kembali ke layar sebelumnya
        }
    }

    // Fungsi untuk toggle visibility password
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Sembunyikan password
            editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            passwordToggle.setImageResource(R.drawable.ic_eye) // Ikon mata terbuka
        } else {
            // Tampilkan password
            editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            passwordToggle.setImageResource(R.drawable.ic_eye_black) // Ikon mata tertutup
        }
        isPasswordVisible = !isPasswordVisible
        editTextPassword.setSelection(editTextPassword.text.length) // Memastikan kursor tetap di akhir teks
    }

    private fun toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            // Sembunyikan confirm password
            editTextConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            confirmPasswordToggle.setImageResource(R.drawable.ic_eye) // Ikon mata terbuka
        } else {
            // Tampilkan confirm password
            editTextConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            confirmPasswordToggle.setImageResource(R.drawable.ic_eye_black) // Ikon mata tertutup
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        editTextConfirmPassword.setSelection(editTextConfirmPassword.text.length) // Memastikan kursor tetap di akhir teks
    }

    // Fungsi untuk mendaftar menggunakan Firebase Authentication
    private fun firebaseSignUp(username: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Jika signup berhasil, kirim verifikasi email
                    sendEmailVerification()
                } else {
                    // Jika signup gagal
                    Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Fungsi untuk mengirimkan email verifikasi
    private fun sendEmailVerification() {
        val user: FirebaseUser? = firebaseAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Pindah ke halaman OTP
                val intent = Intent(this, OtpActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Failed to send verification email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
