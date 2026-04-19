package com.example.focusquest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val userPrefs = UserPreferencesManager(this)
        
        // 2-3 seconds delay then move to appropriate screen
        Handler(Looper.getMainLooper()).postDelayed({
            val nextActivity = if (userPrefs.isLoggedIn()) {
                MainActivity::class.java
            } else {
                SignInActivity::class.java
            }
            startActivity(Intent(this, nextActivity))
            finish()
        }, 2000)
    }
}
