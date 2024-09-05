package com.example.cinemasuggest.view.cinerec

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import com.example.cinemasuggest.R
import com.example.cinemasuggest.databinding.ActivityRec1Binding
import com.example.cinemasuggest.view.home.HomeActivity

class Rec1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRec1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRec1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the back button to go to HomeActivity
        binding.tvBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
            startActivity(intent, options.toBundle())
            finish()
        }

        val toggleButtons = listOf(
            binding.toggleButtonAction,
            binding.toggleButtonAdventure,
            binding.toggleButtonAnimation,
            binding.toggleButtonComedy,
            binding.toggleButtonCrime,
            binding.toggleButtonDocumentary,
            binding.toggleButtonDrama,
            binding.toggleButtonFamily,
            binding.toggleButtonFantasy,
            binding.toggleButtonHistory,
            binding.toggleButtonHorror,
            binding.toggleButtonMusic,
            binding.toggleButtonMystery,
            binding.toggleButtonScifi,
            binding.toggleButtonSports,
            binding.toggleButtonThriller,
            binding.toggleButtonTvMovie,
            binding.toggleButtonWar,
            binding.toggleButtonWestern
        )

        binding.buttonNext.setOnClickListener {
            val selectedGenres = toggleButtons.filter { it.isChecked }.joinToString(",") { it.text.toString() }
            if (selectedGenres.isNotEmpty()) {
                val intent = Intent(this, Rec2Activity::class.java)
                intent.putExtra("selectedGenres", selectedGenres)
                val options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.slide_in_right, R.anim.slide_out_left)
                startActivity(intent, options.toBundle())
            } else {
                Toast.makeText(this, "Please select at least one genre.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}