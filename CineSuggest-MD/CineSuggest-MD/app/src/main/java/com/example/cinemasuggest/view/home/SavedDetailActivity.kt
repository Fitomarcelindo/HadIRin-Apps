package com.example.cinemasuggest.view.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cinemasuggest.databinding.ActivitySavedDetailBinding
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SavedDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedDetailBinding

    companion object {
        const val EXTRA_POSTER = "extra_poster"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ID = "extra_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val poster = intent.getStringExtra(EXTRA_POSTER)
        val title = intent.getStringExtra(EXTRA_TITLE)

        binding.category.text = title
        val posterUrl = "https://image.tmdb.org/t/p/w500$poster"
        Glide.with(this)
            .load(posterUrl)
            .into(binding.imageView)

        binding.ibBack.setOnClickListener {
            finish()
        }

        binding.button.setOnClickListener {
            showConfirmationDialog(title)
        }
    }

    private fun showConfirmationDialog(title: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to see more details about this movie on IMDb?")
            .setPositiveButton("Yes") { _, _ ->
                val imdbUrl = createImdbUrl(title)
                val imdbIntent = Intent(Intent.ACTION_VIEW, Uri.parse(imdbUrl))
                startActivity(imdbIntent)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun createImdbUrl(title: String?): String {
        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        return "https://www.imdb.com/find?q=$encodedTitle"
    }
}
