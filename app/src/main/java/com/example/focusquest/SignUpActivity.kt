package com.example.focusquest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var userPrefs: UserPreferencesManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        
        userPrefs = UserPreferencesManager(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvSignIn = findViewById<TextView>(R.id.tvSignIn)
        val tvError = findViewById<TextView>(R.id.tvError)
        progressBar = findViewById(R.id.progressBar) // Assuming you have one, or I'll add logic
        
        btnSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            
            tvError.visibility = View.GONE
            
            when {
                username.isEmpty() -> showError(tvError, "Please enter a username")
                email.isEmpty() -> showError(tvError, "Please enter your email")
                !isValidEmail(email) -> showError(tvError, "Please enter a valid email")
                password.isEmpty() -> showError(tvError, "Please enter a password")
                password.length < 6 -> showError(tvError, "Password must be at least 6 characters")
                password != confirmPassword -> showError(tvError, "Passwords do not match")
                else -> {
                    setLoading(true)
                    // 1. Create User in Firebase Auth
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid ?: ""
                                val userMap = hashMapOf(
                                    "uid" to uid,
                                    "username" to username,
                                    "email" to email,
                                    "xp" to 0,
                                    "level" to 1
                                )
                                
                                // 2. Save User Profile to Firestore (The Leaderboard Source)
                                db.collection("users").document(uid).set(userMap)
                                    .addOnSuccessListener {
                                        // 3. Save locally for backward compatibility
                                        val newUser = User(username, email, password, 0, 1)
                                        userPrefs.registerUser(newUser)
                                        userPrefs.loginUser(username, password)
                                        
                                        setLoading(false)
                                        Toast.makeText(this, "Welcome to FocusQuest! 🎉", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        setLoading(false)
                                        showError(tvError, "Error saving profile: ${e.message}")
                                    }
                            } else {
                                setLoading(false)
                                showError(tvError, task.exception?.message ?: "Registration failed")
                            }
                        }
                }
            }
        }
        
        tvSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }
    
    private fun showError(tv: TextView, message: String) {
        tv.text = message
        tv.visibility = View.VISIBLE
    }

    private fun setLoading(isLoading: Boolean) {
        // Simple visibility toggle for UI feedback
        findViewById<Button>(R.id.btnSignUp).isEnabled = !isLoading
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
