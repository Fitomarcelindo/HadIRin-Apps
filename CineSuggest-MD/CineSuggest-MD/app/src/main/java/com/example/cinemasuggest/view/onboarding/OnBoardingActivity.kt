package com.example.cinemasuggest.view.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.cinemasuggest.data.adapter.onboarding.OnBoardingAdapter
import com.example.cinemasuggest.databinding.ActivityOnBoardingBinding
import com.example.cinemasuggest.view.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.onboardVp.adapter = OnBoardingAdapter(this.supportFragmentManager, lifecycle)

        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        if (auth.currentUser != null) {
            // User is logged in, redirect to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
    }
}