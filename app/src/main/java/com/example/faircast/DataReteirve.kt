package com.example.faircast
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DataReteirve : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_reteirve)
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val entry = findViewById<TextView>(R.id.waterEntriesTextView)
        val user = auth.currentUser?.uid ?: return
        db.collection("water_intake")
            .whereEqualTo("uid", user)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("FirestoreError", "Listener failed: ${error.message}")
                }
                if (snap != null && !snap.isEmpty) {
                    val builder = StringBuilder()
                    builder.append("\uD83D\uDCA7 Your Water Intake Logs:\n\n")
                    for (i in snap.documents) {
                        val amount = i.getString("amount") ?: ""
                        val time = i.getString("time") ?: ""
                        builder.append("• $amount at $time\n")
                    }
                    entry.text = builder.toString()
                } else if (snap != null) {
                    entry.text = "No entries yet. Stay hydrated! 💧"
                } else {
                    entry.text = "Failed to load entries."
                }
            }

    }
}