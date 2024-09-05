package by.marcel.apps_lab.onboardingPage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import by.marcel.apps_lab.R
import by.marcel.apps_lab.adapter.OnBoardingAdapter
import by.marcel.apps_lab.databinding.ActivityOnBoardPageBinding

class OnBoardPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.onboardVp.adapter = OnBoardingAdapter(this.supportFragmentManager, lifecycle)

    }
}