package com.example.focusquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignInActivity : AppCompatActivity() {
    private lateinit var userPrefs: UserPreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        
        userPrefs = UserPreferencesManager(this)
        
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val tvError = findViewById<TextView>(R.id.tvError)
        
        btnSignIn.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            when {
                username.isEmpty() -> {
                    tvError.text = "Please enter your username"
                    tvError.visibility = android.view.View.VISIBLE
                }
                password.isEmpty() -> {
                    tvError.text = "Please enter your password"
                    tvError.visibility = android.view.View.VISIBLE
                }
                else -> {
                    if (userPrefs.loginUser(username, password)) {
                        Toast.makeText(this, "Sign in successful! 🎉", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        tvError.text = "Invalid username or password"
                        tvError.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }
        
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }
}
