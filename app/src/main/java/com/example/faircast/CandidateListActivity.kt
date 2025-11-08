package com.example.faircast

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CandidateListActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var hasVoted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candidate_list)

        val college = intent.getStringExtra("collegeName") ?: return
        val position = intent.getStringExtra("position") ?: return

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val layout = findViewById<LinearLayout>(R.id.candidateListContainer)

        // Check if already voted
        val userId = auth.currentUser?.uid ?: "anonymous_user"
        val votedDoc = db.collection("Votes")
            .document("$college-$position")
            .collection("voters")
            .document(userId)

        votedDoc.get().addOnSuccessListener {
            hasVoted = it.exists()
        }

        val ref = db.collection("CollegeElections").document(college).collection(position).document("candidates")

        ref.addSnapshotListener { snapshot, _ ->
            layout.removeAllViews()
            if (snapshot != null && snapshot.exists()) {
                for ((_, data) in snapshot.data ?: emptyMap<String, Any>()) {
                    val name = (data as Map<*, *>)["name"]?.toString() ?: "Unknown"
                    val photo = data["photoUrl"]?.toString() ?: ""
                    val bio = data["bio"]?.toString() ?: ""
                    val votes = (data["votes"] as? Long) ?: 0

                    val card = layoutInflater.inflate(R.layout.item_candidate, layout, false)

                    card.findViewById<TextView>(R.id.candidateName).text = name
                    card.findViewById<TextView>(R.id.voteCount).text = "Votes: $votes"
                    Glide.with(this).load(photo).into(card.findViewById(R.id.candidateImage))

                    card.setOnClickListener {
                        val intent = Intent(this, CandidateDetailsActivity::class.java)
                        intent.putExtra("name", name)
                        intent.putExtra("bio", bio)
                        intent.putExtra("photoUrl", photo)
                        startActivity(intent)
                    }

                    card.findViewById<Button>(R.id.voteButton).setOnClickListener {
                        if (hasVoted) {
                            Toast.makeText(this, "You've already voted", Toast.LENGTH_SHORT).show()
                        } else {
                            ref.update("$name.votes", votes + 1)
                            votedDoc.set(mapOf("voted" to true))
                            hasVoted = true
                            Toast.makeText(this, "Vote cast!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    layout.addView(card)
                }
            }
        }
    }
}
