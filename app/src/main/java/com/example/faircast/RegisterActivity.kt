package com.example.faircast

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            registerUser()
        }

        findViewById<TextView>(R.id.tvLoginLink).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()
        val confirm = findViewById<EditText>(R.id.etConfirmPassword).text.toString().trim()
        val name = findViewById<EditText>(R.id.etName).text.toString().trim()
        val voterId = findViewById<EditText>(R.id.etVoterId).text.toString().trim()
        val phone = findViewById<EditText>(R.id.etPhone).text.toString().trim()

        if (!isValid(email, password, confirm, name, voterId, phone)) return

        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name).build()

                user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                    sendEmailVerification(user.uid, name, email, voterId, phone)
                }
            } else {
                showToast("Registration failed: ${task.exception?.message}")
                showLoading(false)
            }
        }
    }

    private fun sendEmailVerification(uid: String, name: String, email: String, voterId: String, phone: String) {
        val user = auth.currentUser ?: return
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveToFirestore(uid, name, email, voterId, phone)
            } else {
                showToast("Email verification failed: ${task.exception?.message}")
                showLoading(false)
            }
        }
    }

    private fun saveToFirestore(uid: String, name: String, email: String, voterId: String, phone: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "fullName" to name,
            "email" to email,
            "voterId" to voterId,
            "phone" to phone,
            "emailVerified" to true,
            "registrationDate" to System.currentTimeMillis()
        )

        db.collection("user").document(name)
            .set(userMap).
            addOnSuccessListener {
                showToast("Registered! Check your email to verify.")
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }.
            addOnFailureListener {
                showToast("Error saving data: ${it.message}")
                showLoading(false)
        }
    }

    private fun isValid(email: String, pass: String, confirm: String, name: String, voterId: String, phone: String): Boolean {
        return when {
            email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showToast("Enter valid email"); false
            }
            pass.length < 6 -> {
                showToast("Password must be at least 6 characters"); false
            }
            pass != confirm -> {
                showToast("Passwords do not match"); false
            }
            name.isEmpty() -> {
                showToast("Name required"); false
            }
            voterId.isEmpty() -> {
                showToast("Voter ID required"); false
            }
            phone.isEmpty() -> {
                showToast("Phone required"); false
            }
            else -> true
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        findViewById<ProgressBar>(R.id.progressBar).visibility = if (show) View.VISIBLE else View.GONE
        findViewById<Button>(R.id.btnRegister).isEnabled = !show
    }
}
