package by.marcel.apps_lab.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import by.marcel.apps_lab.R
import by.marcel.apps_lab.databinding.ActivitySpalshBinding
import by.marcel.apps_lab.onboardingPage.OnBoardPageActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpalshBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpalshBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up)

        scaleAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Do Nothing
            }

            override fun onAnimationEnd(animation: Animation?) {
                val intent = Intent(this@SplashActivity, OnBoardPageActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // Do Nothing
            }
        })

        binding.hadirin.startAnimation(scaleAnimation)
        binding.irLogo.startAnimation(scaleAnimation)
        binding.tvVersion.startAnimation(scaleAnimation)
    }
}