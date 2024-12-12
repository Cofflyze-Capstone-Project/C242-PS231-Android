package com.capstone.cofflyze

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.capstone.cofflyze.ui.onboarding.OnboardingActivity
import com.capstone.cofflyze.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Delay for 3 seconds before navigating to the next activity
        Handler(Looper.getMainLooper()).postDelayed({
            checkOnboardingStatus()
        }, 3000L) // Delay 3 seconds
    }

    private fun checkOnboardingStatus() {
        // Retrieve the value of "isOnboardingShown" from SharedPreferences
        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isOnboardingShown = sharedPreferences.getBoolean("isOnboardingShown", false)

        if (isOnboardingShown) {
            // If onboarding is already shown, navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // If onboarding hasn't been shown, navigate to OnboardingActivity
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
        }

        finish()  // Close SplashActivity
    }
}
