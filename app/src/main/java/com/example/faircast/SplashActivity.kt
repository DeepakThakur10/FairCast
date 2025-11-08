package com.example.faircast

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var imgLogo: ImageView
    private lateinit var tvAppName: TextView
    private lateinit var tvSlogan: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        imgLogo = findViewById(R.id.imgLogo)
        tvAppName = findViewById(R.id.tvAppName)
        tvSlogan = findViewById(R.id.tvSlogan)

        val fadeAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUpAnim = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        imgLogo.startAnimation(fadeAnim)
        tvAppName.startAnimation(fadeAnim)

        tvSlogan.postDelayed({
            tvSlogan.alpha = 1f
            tvSlogan.startAnimation(slideUpAnim)
        }, 1000)

        Handler(mainLooper).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2500)
    }
}
