package com.example.cinemasuggest.view.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.cinemasuggest.databinding.ActivityMainBinding
import com.example.cinemasuggest.view.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.cinemasuggest.data.room.AppDatabase
import com.example.cinemasuggest.data.room.auth.User
import com.google.android.ads.mediationtestsuite.activities.HomeActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isUserDataSaved = sharedPref.getBoolean("isUserDataSaved", false)

        if (isUserDataSaved) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize the database
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "cinema-suggest-db"
        ).build()

        // Check if user is logged in
        if (auth.currentUser == null) {
            // User is not logged in, redirect to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        } else {
            showProgressBar()
            checkUserData(auth.currentUser!!.uid)
        }

        // Disable continue button initially
        binding.continueButton.isEnabled = false

        // Add TextWatchers to input fields
        addTextWatchers()

        // Navigate to home
        binding.continueButton.setOnClickListener {
            showProgressBar()
            checkAndSaveUserData()
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun addTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = binding.nameEditText.text.toString().trim()
                val phone = binding.phoneEditText.text.toString().trim()
                val city = binding.cityEditText.text.toString().trim()

                binding.continueButton.isEnabled = name.isNotEmpty() && phone.isNotEmpty() && city.isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.nameEditText.addTextChangedListener(textWatcher)
        binding.phoneEditText.addTextChangedListener(textWatcher)
        binding.cityEditText.addTextChangedListener(textWatcher)
    }

    private fun checkAndSaveUserData() {
        val user = auth.currentUser ?: return
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                hideProgressBar()
                if (!document.exists() || document.getString("name").isNullOrEmpty() || document.getString("phone").isNullOrEmpty() || document.getString("city").isNullOrEmpty()) {
                    saveUserData()
                } else {
                    val name = document.getString("name")
                    saveUserToLocalDb(user.uid, name!!)
                    navigateToHome()
                }
            }
            .addOnFailureListener {
                hideProgressBar()
                Toast.makeText(this, "Failed to check user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData() {
        val user = auth.currentUser ?: return
        val userData = mapOf(
            "name" to binding.nameEditText.text.toString().trim(),
            "phone" to binding.phoneEditText.text.toString().trim(),
            "city" to binding.cityEditText.text.toString().trim()
        )

        firestore.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                hideProgressBar()
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putBoolean("isUserDataSaved", true)
                    apply()
                }
                saveUserToLocalDb(user.uid, userData["name"]!!)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                hideProgressBar()
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
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

    private fun checkUserData(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                hideProgressBar()
                if (document.exists() && document.contains("city") && document.contains("name") && document.contains("phone")) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener {
                hideProgressBar()
                showSnackbar("Failed to check user data")
            }
    }

    override fun onResume() {
        super.onResume()
        binding.tvHello.text = updateData()
    }

    private fun updateData(): String {
        return auth.currentUser?.email ?: "Not logged in"
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, id ->
                signOut()
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    private fun signOut() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@MainActivity)
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            finish()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showProgressBar() {
        binding.darkOverlay.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.darkOverlay.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
