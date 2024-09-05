package com.example.cinemasuggest.view.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.cinemasuggest.R
import com.example.cinemasuggest.data.response.Movie
import com.example.cinemasuggest.data.retrofit.ApiConfig
import com.example.cinemasuggest.data.room.AppDatabase
import com.example.cinemasuggest.data.room.auth.User
import com.example.cinemasuggest.databinding.ActivityHomeBinding
import com.example.cinemasuggest.utils.OnSwipeTouchListener
import com.example.cinemasuggest.view.cinerec.Rec1Activity
import com.example.cinemasuggest.view.login.LoginActivity
import com.example.cinemasuggest.view.search.SearchActivity
import com.example.cinemasuggest.view.setting.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var db: AppDatabase

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "cinema-suggest-db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

        setupBottomNavigation()
        showProgressBar()
        getUserName()

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Add the OnClickListener for the button
        binding.btnSaved.setOnClickListener {
            val intent = Intent(this, SavedMovieActivity::class.java)
            startActivity(intent)
        }

        binding.root.setOnTouchListener(object : OnSwipeTouchListener(this@HomeActivity) {
            override fun onSwipeLeft() {
                val intent = Intent(this@HomeActivity, Rec1Activity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            }
        })

        fetchPopularMovies()
    }

    private fun fetchPopularMovies() {
        Log.d("HomeActivity", "Starting fetchPopularMovies")
        showProgressBar()

        // Fetch the first movie
        ApiConfig.apiService.getPopularMovies().enqueue(object : Callback<Movie> {
            override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                Log.d("HomeActivity", "First call onResponse: ${response.code()}")
                if (response.isSuccessful) {
                    val movie1 = response.body()
                    Log.d("HomeActivity", "First call response body: $movie1")
                    if (movie1 != null) {
                        // Fetch the second movie
                        ApiConfig.apiService.getPopularMovies().enqueue(object : Callback<Movie> {
                            override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                                Log.d("HomeActivity", "Second call onResponse: ${response.code()}")
                                if (response.isSuccessful) {
                                    val movie2 = response.body()
                                    Log.d("HomeActivity", "Second call response body: $movie2")
                                    if (movie2 != null) {
                                        if (movie1.id != movie2.id) {
                                            Log.d("HomeActivity", "Movies fetched successfully: ${movie1.title}, ${movie2.title}")
                                            displayTrendingMovies(listOf(movie1, movie2))
                                        } else {
                                            // Fetch another movie if they are the same
                                            fetchAdditionalMovie(movie1)
                                        }
                                    } else {
                                        Log.d("HomeActivity", "No second movie found")
                                        hideProgressBar()
                                    }
                                } else {
                                    Log.e("HomeActivity", "Failed to fetch second movie: ${response.errorBody()?.string()}")
                                    hideProgressBar()
                                }
                            }

                            override fun onFailure(call: Call<Movie>, t: Throwable) {
                                Log.e("HomeActivity", "Error fetching second movie: ${t.message}", t)
                                hideProgressBar()
                            }
                        })
                    } else {
                        Log.d("HomeActivity", "No first movie found")
                        hideProgressBar()
                    }
                } else {
                    Log.e("HomeActivity", "Failed to fetch first movie: ${response.errorBody()?.string()}")
                    hideProgressBar()
                }
            }

            override fun onFailure(call: Call<Movie>, t: Throwable) {
                Log.e("HomeActivity", "Error fetching first movie: ${t.message}", t)
                hideProgressBar()
            }
        })
    }

    private fun fetchAdditionalMovie(movie1: Movie) {
        ApiConfig.apiService.getPopularMovies().enqueue(object : Callback<Movie> {
            override fun onResponse(call: Call<Movie>, response: Response<Movie>) {
                Log.d("HomeActivity", "Additional call onResponse: ${response.code()}")
                if (response.isSuccessful) {
                    val movie3 = response.body()
                    Log.d("HomeActivity", "Additional call response body: $movie3")
                    if (movie3 != null) {
                        if (movie1.id != movie3.id) {
                            Log.d("HomeActivity", "Movies fetched successfully: ${movie1.title}, ${movie3.title}")
                            displayTrendingMovies(listOf(movie1, movie3))
                        } else {
                            Log.d("HomeActivity", "Additional movie is the same as the first, trying again")
                            fetchAdditionalMovie(movie1)
                        }
                    } else {
                        Log.d("HomeActivity", "No additional movie found")
                    }
                } else {
                    Log.e("HomeActivity", "Failed to fetch additional movie: ${response.errorBody()?.string()}")
                }
                hideProgressBar()
            }

            override fun onFailure(call: Call<Movie>, t: Throwable) {
                Log.e("HomeActivity", "Error fetching additional movie: ${t.message}", t)
                hideProgressBar()
            }
        })
    }

    private fun displayTrendingMovies(movies: List<Movie>) {
        val movie1 = movies[0]
        val movie2 = movies[1]

        val posterUrl1 = "https://image.tmdb.org/t/p/w500${movie1.poster}"
        binding.movieTitle.text = movie1.title
        Glide.with(this)
            .load(posterUrl1)
            .into(binding.roundedImage)

        val posterUrl2 = "https://image.tmdb.org/t/p/w500${movie2.poster}"
        binding.movieTitle2.text = movie2.title
        Glide.with(this)
            .load(posterUrl2)
            .into(binding.roundedImage2)

        binding.trendingMov.setOnClickListener {
            navigateToDetail(movie1)
        }

        binding.trendingMov2.setOnClickListener {
            navigateToDetail(movie2)
        }
    }

    private fun navigateToDetail(movie: Movie) {
        Intent(this@HomeActivity, DetailHomeActivity::class.java).apply {
            putExtra(DetailHomeActivity.EXTRA_ID, movie.id)
            putExtra(DetailHomeActivity.EXTRA_TITLE, movie.title)
            putExtra(DetailHomeActivity.EXTRA_POSTER, movie.poster)
            startActivity(this)
        }
    }

    private fun getUserName() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val localUser = db.userDao().getUserById(uid)
                withContext(Dispatchers.Main) {
                    if (localUser != null) {
                        binding.tvUsername.text = localUser.name
                        hideProgressBar()
                    } else {
                        firestore.collection("users").document(uid).get()
                            .addOnSuccessListener { document ->
                                hideProgressBar()
                                if (document != null && document.exists()) {
                                    val name = document.getString("name") ?: "Unknown User"
                                    binding.tvUsername.text = name
                                    saveUserToLocalDb(uid, name)
                                } else {
                                    binding.tvUsername.text = "Unknown User"
                                }
                            }
                            .addOnFailureListener {
                                hideProgressBar()
                                binding.tvUsername.text = "Error fetching name"
                            }
                    }
                }
            }
        }
    }

    private fun saveUserToLocalDb(uid: String, name: String) {
        val user = User(uid, name)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.userDao().insert(user)
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, id ->
                showProgressBar()
                navigateToLogin()
                signOut()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }


    private fun signOut() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val credentialManager = CredentialManager.create(this@HomeActivity)
                auth.signOut()
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_recommendation -> {
                    val intent = Intent(this@HomeActivity, Rec1Activity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_search -> {
                    val intent = Intent(this@HomeActivity, SearchActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_settings -> {
                    val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
                    startActivityForResult(intent, SETTINGS_REQUEST_CODE)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    private fun navigateToLogin() {
        showProgressBar()
        val intent = Intent(this@HomeActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showProgressBar() {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.loadingOverlay.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
            val updatedName = data?.getStringExtra("updatedName")
            if (!updatedName.isNullOrEmpty()) {
                binding.tvUsername.text = updatedName
            }
        }
    }

    companion object {
        private const val SETTINGS_REQUEST_CODE = 1001
    }
}
