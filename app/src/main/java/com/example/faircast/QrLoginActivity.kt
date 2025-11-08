package com.example.faircast

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class QrLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_login)

        Toast.makeText(this, "QR Code scanning coming soon!", Toast.LENGTH_SHORT).show()
    }
}
