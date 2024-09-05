package com.example.cinemasuggest.view.search

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.cinemasuggest.R
import com.example.cinemasuggest.data.retrofit.ApiConfig
import com.example.cinemasuggest.data.response.SearchResponseItem
import com.example.cinemasuggest.data.room.AppDatabase
import com.example.cinemasuggest.data.room.auth.User
import com.example.cinemasuggest.databinding.ActivitySearchBinding
import com.example.cinemasuggest.view.cinerec.Rec1Activity
import com.example.cinemasuggest.view.home.HomeActivity
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

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "cinema-suggest-db")
            .build()

        // Set the selected item in the bottom navigation
        binding.bottomNavigationView.selectedItemId = R.id.bottom_search

        // Get user name
        showProgressBar()
        getUserName()

        with(binding) {
            searchView.setupWithSearchBar(searchBar)
            searchView
                .editText
                .setOnEditorActionListener { textView, actionId, event ->
                    searchBar.setText(searchView.text)
                    searchView.hide()
                    performSearch(searchView.text.toString())
                    false
                }
        }

        // Set up RecyclerView
        binding.rvRecommendedMovies.layoutManager = LinearLayoutManager(this)

        // Fetch and display movies with title "Avengers"
        performSearch("Avengers")

        // Set up bottom navigation
        setupBottomNavigation()
    }

    private fun performSearch(query: String) {
        showProgressBar()
        val call = ApiConfig.apiService.searchMovies(query)
        call.enqueue(object : Callback<List<SearchResponseItem>> {
            override fun onResponse(
                call: Call<List<SearchResponseItem>>,
                response: Response<List<SearchResponseItem>>
            ) {
                hideProgressBar()
                if (response.isSuccessful) {
                    val searchResults = response.body() ?: emptyList()
                    binding.rvRecommendedMovies.adapter = SearchMoviesAdapter(searchResults)
                } else {
                    Toast.makeText(this@SearchActivity, "Failed to get search results.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<SearchResponseItem>>, t: Throwable) {
                hideProgressBar()
                Toast.makeText(this@SearchActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserName() {
        val user = auth.currentUser ?: return
        val uid = user.uid

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // Try to fetch from local database first
                val localUser = db.userDao().getUserById(uid)
                withContext(Dispatchers.Main) {
                    if (localUser != null) {
                        binding.tvUsername.text = localUser.name
                        hideProgressBar()
                    } else {
                        // Fetch from Firestore if not in local database
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

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    val intent = Intent(this@SearchActivity, HomeActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_recommendation -> {
                    val intent = Intent(this@SearchActivity, Rec1Activity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_search -> {
                    // Stay on the SearchActivity
                    return@OnNavigationItemSelectedListener true
                }
                R.id.bottom_settings -> {
                    val intent = Intent(this@SearchActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })
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
