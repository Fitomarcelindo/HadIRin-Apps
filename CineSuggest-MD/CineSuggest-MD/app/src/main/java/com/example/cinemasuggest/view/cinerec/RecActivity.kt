package com.example.cinemasuggest.view.cinerec

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.cinemasuggest.R
import com.example.cinemasuggest.data.adapter.RecommendationsAdapter
import com.example.cinemasuggest.data.response.RecommendationResponseItem
import com.example.cinemasuggest.data.retrofit.ApiConfig
import com.example.cinemasuggest.data.room.AppDatabase
import com.example.cinemasuggest.data.room.auth.User
import com.example.cinemasuggest.databinding.ActivityRecBinding
import com.example.cinemasuggest.view.home.SavedMovieActivity
import com.example.cinemasuggest.view.login.LoginActivity
import com.example.cinemasuggest.view.search.SearchActivity
import com.example.cinemasuggest.view.setting.ProfileActivity
import com.google.android.ads.mediationtestsuite.activities.HomeActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "cinema-suggest-db")
            .build()

        setupBottomNavigation()
        showProgressBar()
        getUserName()

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        binding.btnSaved.setOnClickListener {
            val intent = Intent(this, SavedMovieActivity::class.java)
            startActivity(intent)
        }

        val selectedGenres = intent.getStringExtra("selectedGenres")
        val selectedMood = intent.getStringExtra("selectedMood")

        // Log the genres and mood to verify they are passed correctly
        Log.d("RecActivity", "Selected Genres: $selectedGenres")
        Log.d("RecActivity", "Selected Mood: $selectedMood")

        binding.rvRecommended.layoutManager = LinearLayoutManager(this)
        getRecommendations(selectedGenres, selectedMood)

        // Set the selected item in the bottom navigation
        binding.bottomNavigationView.selectedItemId = R.id.bottom_recommendation
    }

    private fun getRecommendations(genres: String?, mood: String?) {
        showProgressBar()
        val call = ApiConfig.apiService.getRecommendations(genres ?: "", mood ?: "")
        call.enqueue(object : Callback<List<RecommendationResponseItem>> {
            override fun onResponse(
                call: Call<List<RecommendationResponseItem>>,
                response: Response<List<RecommendationResponseItem>>
            ) {
                hideProgressBar()
                if (response.isSuccessful) {
                    val recommendations = response.body()?.toMutableList()
                    Log.d("RecActivity", "Recommendations received: ${recommendations?.size} items")
                    recommendations?.shuffle()
                    binding.rvRecommended.adapter = recommendations?.let { RecommendationsAdapter(it) }
                } else {
                    Toast.makeText(this@RecActivity, "Failed to get recommendations.", Toast.LENGTH_SHORT).show()
                    Log.e("RecActivity", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<RecommendationResponseItem>>, t: Throwable) {
                showProgressBar()
                Toast.makeText(this@RecActivity, "An error occurred.", Toast.LENGTH_SHORT).show()
                Log.e("RecActivity", "onFailure: ", t)
            }
        })
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
                                if (document != null) {
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
                val credentialManager = CredentialManager.create(this@RecActivity)
                auth.signOut()
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    val intent = Intent(this@RecActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_recommendation -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_search -> {
                    val intent = Intent(this@RecActivity, SearchActivity::class.java)
                    startActivity(intent)
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_settings -> {
                    val intent = Intent(this@RecActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
    }

    private fun navigateToLogin() {
        showProgressBar()
        val intent = Intent(this@RecActivity, LoginActivity::class.java)
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
}
