package com.capstone.cofflyze.ui.login

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.capstone.cofflyze.R
import com.google.firebase.auth.FirebaseAuth

class OtpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var textViewResendEmail: TextView
    private lateinit var buttonBack: Button
    private var verificationThread: Thread? = null
    private var shouldStopThread = false // Flag untuk menghentikan thread dengan aman
    private var isNavigated = false // Flag untuk memastikan navigasi hanya dilakukan sekali

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setContentView(R.layout.activity_otp)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inisialisasi View
        textViewResendEmail = findViewById(R.id.textViewResendEmail)
        buttonBack = findViewById(R.id.buttonBack)

        // Tombol "Resend Email"
        textViewResendEmail.setOnClickListener {
            resendVerificationEmail()
        }

        // Tombol "Back"
        buttonBack.setOnClickListener {
            finish()
        }

        // Pantau status pengguna hanya sekali setelah activity dimulai
        monitorVerificationStatus()
    }

    private fun resendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Email verifikasi telah dikirim. Silakan cek email Anda.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal mengirim email verifikasi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToLogin() {
        if (!isNavigated) { // Cek jika belum menavigasi
            isNavigated = true // Tandai bahwa navigasi sudah dilakukan
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun monitorVerificationStatus() {
        val user = auth.currentUser
        verificationThread = Thread {
            while (!shouldStopThread) {
                user?.reload()?.addOnCompleteListener { task ->
                    if (task.isSuccessful && user.isEmailVerified && !isNavigated) {
                        runOnUiThread {
                            navigateToLogin() // Hanya dipanggil sekali
                        }
                        shouldStopThread = true // Hentikan thread setelah navigasi
                    }
                }
                try {
                    Thread.sleep(3000) // Periksa status setiap 3 detik
                } catch (e: InterruptedException) {
                    e.printStackTrace() // Tangani InterruptedException jika terjadi
                }
            }
        }
        verificationThread?.start()
    }

    // Gunakan flag untuk menghentikan thread dengan aman
    override fun onDestroy() {
        super.onDestroy()
        shouldStopThread = true // Set flag agar thread berhenti dengan aman
    }
}
