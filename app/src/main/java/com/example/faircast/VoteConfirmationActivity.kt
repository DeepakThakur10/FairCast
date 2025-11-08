package com.example.faircast

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class VoteConfirmationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vote_confirmation)

        val candidate = intent.getStringExtra("candidate")
        val confirmText = findViewById<TextView>(R.id.tvConfirm)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmVote)

        confirmText.text = "Are you sure you want to vote for $candidate?"

        btnConfirm.setOnClickListener {
            Toast.makeText(this, "Vote casted successfully!", Toast.LENGTH_SHORT).show()
            finishAffinity() // Optional: close all previous activities
        }
    }
}
