package com.example.cinemasuggest.view.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.cinemasuggest.R
import com.example.cinemasuggest.databinding.ActivitySplashBinding
import com.example.cinemasuggest.view.login.LoginActivity
import com.example.cinemasuggest.view.onboarding.OnBoardingActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up)

        scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Do Nothing
            }

            override fun onAnimationEnd(animation: Animation?) {
                val intent = Intent(this@SplashActivity, OnBoardingActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // Do Nothing
            }
        })

        binding.applogo.startAnimation(scaleAnimation)
        binding.tvVersion.startAnimation(scaleAnimation)
    }
}