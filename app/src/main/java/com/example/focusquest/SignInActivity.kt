package com.example.focusquest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {
    private lateinit var userPrefs: UserPreferencesManager
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        
        userPrefs = UserPreferencesManager(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        val etEmail = findViewById<EditText>(R.id.etUsername) // Note: Using the username field for email or I should check layout
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val tvError = findViewById<TextView>(R.id.tvError)
        
        // Check if layout uses etUsername or etEmail for the first field
        etEmail.hint = "Email" 

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            
            tvError.visibility = View.GONE
            
            when {
                email.isEmpty() -> showError(tvError, "Please enter your email")
                password.isEmpty() -> showError(tvError, "Please enter your password")
                else -> {
                    setLoading(true)
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid ?: ""
                                
                                // Fetch user details from Firestore to update local prefs
                                db.collection("users").document(uid).get()
                                    .addOnSuccessListener { document ->
                                        if (document != null && document.exists()) {
                                            val username = document.getString("username") ?: ""
                                            val xp = document.getLong("xp")?.toInt() ?: 0
                                            val level = document.getLong("level")?.toInt() ?: 1
                                            
                                            // Sync to local preferences
                                            val user = User(username, email, password, xp, level)
                                            userPrefs.registerUser(user) // Ensure user exists locally
                                            userPrefs.loginUser(username, password)
                                            
                                            setLoading(false)
                                            Toast.makeText(this, "Welcome back, $username! 👋", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, MainActivity::class.java))
                                            finish()
                                        }
                                    }
                                    .addOnFailureListener {
                                        setLoading(false)
                                        // If firestore fails, we still have auth, but local state might be stale
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                            } else {
                                setLoading(false)
                                showError(tvError, "Invalid email or password")
                            }
                        }
                }
            }
        }
        
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun showError(tv: TextView, message: String) {
        tv.text = message
        tv.visibility = View.VISIBLE
    }

    private fun setLoading(isLoading: Boolean) {
        findViewById<Button>(R.id.btnSignIn).isEnabled = !isLoading
    }
}
