package com.example.faircast

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ElectionListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_election_list)

        val list = findViewById<ListView>(R.id.electionList)
        val elections = listOf("Presidential Election 2025", "Parliamentary Election 2025")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, elections)
        list.adapter = adapter

        list.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, CandidateDetailsActivity::class.java)
            intent.putExtra("election_name", elections[position])
            startActivity(intent)
        }
    }
}
