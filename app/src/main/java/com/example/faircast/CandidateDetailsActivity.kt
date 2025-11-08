package com.example.faircast

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class CandidateDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candidate_details)

        val name = intent.getStringExtra("name") ?: ""
        val photoUrl = intent.getStringExtra("photoUrl") ?: ""
        val bio = intent.getStringExtra("bio") ?: ""

        findViewById<TextView>(R.id.candidateName).text = name
        findViewById<TextView>(R.id.candidateBio).text = bio
        Glide.with(this).load(photoUrl).into(findViewById(R.id.candidateImage))
    }
}
