package com.capstone.cofflyze.ui.onboarding

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.viewpager2.widget.ViewPager2
import com.capstone.cofflyze.R
import com.capstone.cofflyze.ui.login.LoginActivity

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        supportActionBar?.hide()

        // Initialize ViewPager2 and its adapter
        viewPager = findViewById(R.id.viewPager)
        val adapter = OnboardingAdapter(this)
        viewPager.adapter = adapter

        // Initialize buttons
        nextButton = findViewById(R.id.finish_button)
        backButton = findViewById(R.id.back_button)

        // Handle "Next" button click to move to the next page
        nextButton.setOnClickListener {
            if (viewPager.currentItem < 2) {
                viewPager.currentItem += 1  // Move to next page
            } else {
                // On the last page, finish onboarding and go to LoginActivity
                saveOnboardingStatus(true)
                val intent = Intent(this, LoginActivity::class.java)  // Navigate to LoginActivity
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()  // Close OnboardingActivity
            }
        }

        // Handle "Back" button click to move to the previous page
        backButton.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1  // Move to previous page
            }
        }

        // Change "Next" text to "Sign In" on the last page
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Change the button text to "Sign In" on the last page
                if (position == 2) {
                    nextButton.text = "Sign In"
                } else {
                    nextButton.text = "Next"
                }

                // Hide "Back" button on the first page
                if (position == 0) {
                    backButton.visibility = View.INVISIBLE
                } else {
                    backButton.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun saveOnboardingStatus(isShown: Boolean) {
        // Save the onboarding status in SharedPreferences
        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isOnboardingShown", isShown)
        editor.apply()  // Use apply() as it is asynchronous
    }
}
