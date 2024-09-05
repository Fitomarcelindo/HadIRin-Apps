package com.example.cinemasuggest.view.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.cinemasuggest.R
import com.example.cinemasuggest.data.room.AppDatabase
import com.example.cinemasuggest.data.room.auth.User
import com.example.cinemasuggest.databinding.ActivityProfileBinding
import com.google.android.ads.mediationtestsuite.activities.HomeActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "cinema-suggest-db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

        // Load user data
        loadUserData()

        // Handle back button click
        binding.btnBack.setOnClickListener {
            val intent = Intent(this@ProfileActivity, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        // Handle edit button click
        binding.editButton.setOnClickListener {
            showEditConfirmationDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvHello.text = updateData()
    }

    private fun updateData(): String {
        return auth.currentUser?.email ?: "Not logged in"
    }

    private fun loadUserData() {
        showProgressBar()
        val user = auth.currentUser ?: return
        val uid = user.uid

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                hideProgressBar()
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "Unknown User"
                    val phone = document.getString("phone") ?: ""
                    val city = document.getString("city") ?: ""

                    binding.nameEditText.setText(name)
                    binding.phoneEditText.setText(phone)
                    binding.cityEditText.setText(city)
                }
            }
            .addOnFailureListener {
                hideProgressBar()
                // Handle the error
            }
    }

    private fun showEditConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage("Are you sure you want to edit your profile information?")
            .setPositiveButton("Yes") { _, _ ->
                editUserProfile()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun editUserProfile() {
        showProgressBar()
        val user = auth.currentUser ?: return
        val uid = user.uid
        val name = binding.nameEditText.text.toString().trim()
        val phone = binding.phoneEditText.text.toString().trim()
        val city = binding.cityEditText.text.toString().trim()

        val userData = mapOf(
            "name" to name,
            "phone" to phone,
            "city" to city
        )

        firestore.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                hideProgressBar()
                showSnackbar("Profile updated successfully")
                saveUserToLocalDb(uid, name)
                navigateToHome(name)
            }
            .addOnFailureListener {
                hideProgressBar()
                showSnackbar("Failed to update profile")
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

    private fun navigateToHome(updatedName: String) {
        val intent = Intent(this@ProfileActivity, HomeActivity::class.java).apply {
            putExtra("updatedName", updatedName)
        }
        startActivity(intent)
        finish()
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
        binding.darkOverlay.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
        binding.darkOverlay.visibility = View.GONE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
