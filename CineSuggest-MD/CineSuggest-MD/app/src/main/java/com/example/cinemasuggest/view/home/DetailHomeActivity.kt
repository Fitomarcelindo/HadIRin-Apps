package com.example.cinemasuggest.view.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.cinemasuggest.data.room.AppDatabase
import com.example.cinemasuggest.data.room.recommendation.UserMovie
import com.example.cinemasuggest.databinding.ActivityDetailhomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DetailHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailhomeBinding
    private lateinit var db: AppDatabase
    private lateinit var auth: FirebaseAuth

    companion object {
        const val EXTRA_POSTER = "extra_poster"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_ID = "extra_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailhomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "cinema-suggest-db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

        auth = FirebaseAuth.getInstance()

        val poster = intent.getStringExtra(EXTRA_POSTER)
        val title = intent.getStringExtra(EXTRA_TITLE)
        val movieId = intent.getStringExtra(EXTRA_ID) ?: ""

        binding.category.text = title
        if (poster != null) {
            Glide.with(this).load("https://image.tmdb.org/t/p/w500$poster").into(binding.imageView)
        }

        binding.ibBack.setOnClickListener {
            finish()
        }

        binding.ibSave.setOnClickListener {
            saveMovie(title, poster, movieId)
        }

        binding.button.setOnClickListener {
            showConfirmationDialog(title)
        }
    }

    private fun saveMovie(title: String?, poster: String?, movieId: String) {
        val userId = auth.currentUser?.uid ?: return
        if (title == null || poster == null) {
            Snackbar.make(binding.root, "Failed to save movie", Snackbar.LENGTH_SHORT).show()
            return
        }

        val userMovie = UserMovie(userId = userId, movieId = movieId, title = title, posterPath = poster)
        lifecycleScope.launch {
            val existingMovie = withContext(Dispatchers.IO) {
                db.userMovieDao().getMovieByTitle(title, userId)
            }
            if (existingMovie != null) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Movie is already saved", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.IO) {
                    db.userMovieDao().insert(userMovie)
                }
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "Movie saved", Snackbar.LENGTH_SHORT).show()
                }
            }
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
