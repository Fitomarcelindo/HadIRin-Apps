package com.example.cinemasuggest.view.cinerec

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import com.example.cinemasuggest.R
import com.example.cinemasuggest.databinding.ActivityRec2Binding
import com.example.cinemasuggest.view.home.HomeActivity

class Rec2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRec2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRec2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        val selectedGenres = intent.getStringExtra("selectedGenres")

        // Set up the back button to go to HomeActivity
        binding.tvBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
            startActivity(intent, options.toBundle())
            finish()
        }

        val toggleButtons = listOf(
            binding.toggleButtonHappy,
            binding.toggleButtonSad,
            binding.toggleButtonAngry,
            binding.toggleButtonAmused,
            binding.toggleButtonBored,
            binding.toggleButtonCalm,
            binding.toggleButtonCheerful,
            binding.toggleButtonJealous,
            binding.toggleButtonMotivated,
            binding.toggleButtonJoyful,
            binding.toggleButtonNervous,
            binding.toggleButtonRelaxed,
            binding.toggleButtonShock,
            binding.toggleButtonTense,
            binding.toggleButtonWorried
        )

        binding.buttonContinue.setOnClickListener {
            val selectedMood = toggleButtons.find { it.isChecked }?.text.toString()
            if (selectedMood.isNotEmpty()) {
                val intent = Intent(this, RecActivity::class.java)
                intent.putExtra("selectedGenres", selectedGenres)
                intent.putExtra("selectedMood", selectedMood)
                val options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
                startActivity(intent,options.toBundle())
            } else {
                Toast.makeText(this, "Please select a mood.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}