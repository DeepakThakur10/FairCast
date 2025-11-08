package com.example.faircast

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ThankYouActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thank_you)

        val thankYouText = findViewById<TextView>(R.id.thankYouMessage)
        thankYouText.text = "🎉 Thank you for casting your vote!\nYour voice matters."
    }
}
