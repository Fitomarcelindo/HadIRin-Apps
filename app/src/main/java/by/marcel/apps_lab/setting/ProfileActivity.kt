package by.marcel.apps_lab.setting

import by.marcel.apps_lab.mainhome.MainActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import by.marcel.apps_lab.databinding.ActivityProfileBinding
import by.marcel.apps_lab.registerPage.User
import by.marcel.apps_lab.room.AppDatabase
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
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "HadIRin-db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
        loadUserData()

        binding.edtBtn.setOnClickListener {
            showEditConfirmationDialog()
        }

    }

    override fun onResume() {
        super.onResume()
        binding.tvPopEmail.text = updateData()
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
                    val username = document.getString("Username") ?: "Unknown User"
                    val phone = document.getString("Phone") ?: ""
                    val nim = document.getString("Nim") ?: ""

                    binding.edtUsername.setText(username)
                    binding.edtphone.setText(phone)
                    binding.edtNim.setText(nim)
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
        val username = binding.edtUsername.text.toString().trim()
        val phone = binding.edtphone.text.toString().trim()
        val nim = binding.edtNim.text.toString().trim()

        val userData = mapOf(
            "Username" to username,
            "Phone" to phone,
            "Nim" to nim
        )

        firestore.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                hideProgressBar()
                showSnackbar("Profile updated successfully")
                saveUserToLocalDb(uid, username)
                navigateToHome(username)
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
        val intent = Intent(this@ProfileActivity, MainActivity::class.java).apply {
            putExtra("updatedName", updatedName)
        }
        startActivity(intent)
        finish()
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}