package com.example.focusquest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply dark mode before super/setContentView
        val userPrefs = UserPreferencesManager(this)
        val nightMode = when (userPrefs.getDarkMode()) {
            1 -> AppCompatDelegate.MODE_NIGHT_NO
            2 -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = when {
                !userPrefs.hasSeenOnboarding() -> OnboardingActivity::class.java
                userPrefs.isLoggedIn()          -> MainActivity::class.java
                else                            -> SignInActivity::class.java
            }
            startActivity(Intent(this, nextActivity))
            finish()
        }, 2000)
    }
}
