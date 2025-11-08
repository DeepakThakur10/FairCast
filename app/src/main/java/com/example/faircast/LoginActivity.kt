package com.example.faircast


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Initialize Firebase
        FirebaseAuth.getInstance()

        // Find all views
        val etVoterId = findViewById<EditText>(R.id.etVoterId)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val cbRemember = findViewById<CheckBox>(R.id.cbRemember)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val btnBiometric = findViewById<Button>(R.id.btnBiometric)
        val btnQRLogin = findViewById<Button>(R.id.btnQRLogin)

        // SharedPreferences for remembering login
        val sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        // Load saved credentials if "Remember Me" was checked
        if (sharedPreferences.getBoolean("RememberMe", false)) {
            etVoterId.setText(sharedPreferences.getString("VoterID", ""))
            etPassword.setText(sharedPreferences.getString("Password", ""))
            cbRemember.isChecked = true
        }

        // Login button click
        btnLogin.setOnClickListener {
            val voterId = etVoterId.text.toString()
            val password = etPassword.text.toString()

            if (voterId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(voterId, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Save credentials if "Remember Me" is checked
                        if (cbRemember.isChecked) {
                            val editor = sharedPreferences.edit()
                            editor.putString("VoterID", voterId)
                            editor.putString("Password", password)
                            editor.putBoolean("RememberMe", true)
                            editor.apply()
                        } else {
                            sharedPreferences.edit().clear().apply()
                        }

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Forgot password click
        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Biometric login click
        btnBiometric.setOnClickListener {
            startActivity(Intent(this, BiometricLoginActivity::class.java))
        }

        // QR login click
        btnQRLogin.setOnClickListener {
            startActivity(Intent(this, QrLoginActivity::class.java))
        }

        // Register text click

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}