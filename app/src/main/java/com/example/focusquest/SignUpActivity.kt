package com.example.focusquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    private lateinit var userPrefs: UserPreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        
        userPrefs = UserPreferencesManager(this)
        
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvSignIn = findViewById<TextView>(R.id.tvSignIn)
        val tvError = findViewById<TextView>(R.id.tvError)
        
        btnSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            
            tvError.visibility = android.view.View.GONE
            
            when {
                username.isEmpty() -> {
                    tvError.text = "Please enter a username"
                    tvError.visibility = android.view.View.VISIBLE
                }
                email.isEmpty() -> {
                    tvError.text = "Please enter your email"
                    tvError.visibility = android.view.View.VISIBLE
                }
                !isValidEmail(email) -> {
                    tvError.text = "Please enter a valid email"
                    tvError.visibility = android.view.View.VISIBLE
                }
                password.isEmpty() -> {
                    tvError.text = "Please enter a password"
                    tvError.visibility = android.view.View.VISIBLE
                }
                password.length < 6 -> {
                    tvError.text = "Password must be at least 6 characters"
                    tvError.visibility = android.view.View.VISIBLE
                }
                password != confirmPassword -> {
                    tvError.text = "Passwords do not match"
                    tvError.visibility = android.view.View.VISIBLE
                }
                else -> {
                    val newUser = User(
                        username = username,
                        email = email,
                        password = password,
                        xp = 0,
                        level = 1
                    )
                    
                    if (userPrefs.registerUser(newUser)) {
                        Toast.makeText(this, "Account created successfully! 🎉", Toast.LENGTH_SHORT).show()
                        // Auto login the new user
                        userPrefs.loginUser(username, password)
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        tvError.text = "Username already exists"
                        tvError.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }
        
        tvSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
