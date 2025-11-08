package com.example.faircast

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val email = findViewById<EditText>(R.id.etEmail)
        val btn = findViewById<Button>(R.id.btnResetPassword)

        btn.setOnClickListener {
            val enteredEmail = email.text.toString().trim()
            if (enteredEmail.isEmpty()) {
                Toast.makeText(this, "Please enter your voter ID (email)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Reset link will be sent via Firebase later", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
