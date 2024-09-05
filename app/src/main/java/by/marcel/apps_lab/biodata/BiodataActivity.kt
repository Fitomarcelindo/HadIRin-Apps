package by.marcel.apps_lab.biodata


import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import by.marcel.apps_lab.databinding.ActivityBiodataBinding
import by.marcel.apps_lab.loginPage.LoginActivity
import by.marcel.apps_lab.mainhome.MainActivity
import by.marcel.apps_lab.registerPage.User
import by.marcel.apps_lab.room.AppDatabase
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BiodataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBiodataBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isUserDataSaved = sharedPref.getBoolean("isUserDataSaved", false)

        if (isUserDataSaved) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityBiodataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "HadIRin-db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

        if (auth.currentUser == null) {
            // User is not logged in, redirect to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        } else {
            showProgressBar()
            checkUserData(auth.currentUser!!.uid)
        }

        binding.edtBtnContinue.isEnabled = false

        addTextWatchers()

        binding.edtBtnContinue.setOnClickListener {
            showProgressBar()
            checkAndSaveUserData()
        }

        binding.ivLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun addTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = binding.edtUsername.text.toString().trim()
                val phone = binding.edtPhone.text.toString().trim()
                val nim = binding.edtNim.text.toString().trim()

                binding.edtBtnContinue.isEnabled = name.isNotEmpty() && phone.isNotEmpty() && nim.isNotEmpty()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.edtUsername.addTextChangedListener(textWatcher)
        binding.edtPhone.addTextChangedListener(textWatcher)
        binding.edtNim.addTextChangedListener(textWatcher)
    }

    private fun checkAndSaveUserData() {
        val user = auth.currentUser ?: return
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                hideProgressBar()
                if (!document.exists() || document.getString("username").isNullOrEmpty() || document.getString("phone").isNullOrEmpty() || document.getString("nim").isNullOrEmpty()) {
                    saveUserData()
                } else {
                    val name = document.getString("username")
                    saveUserToLocalDb(user.uid, name!!)
                    navigateToHome()
                }
            }
            .addOnFailureListener { exception ->
                hideProgressBar()
                if (exception.message?.contains("PERMISSION_DENIED") == true) {
                    Toast.makeText(this, "Permission denied. Please check Firestore rules.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to check user data: ${exception.message}", Toast.LENGTH_SHORT).show()
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


    private fun saveUserData() {
        val user = auth.currentUser ?: return
        val userData = mapOf(
            "Username" to binding.edtUsername.text.toString().trim(),
            "phone" to binding.edtPhone.text.toString().trim(),
            "nim" to binding.edtNim.text.toString().trim()
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
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                hideProgressBar()
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserData(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                hideProgressBar()
                if (document.exists() && document.contains("Username") && document.contains("Phone") && document.contains("Nim")) {
                    startActivity(Intent(this, LoginActivity::class.java))
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
        binding.tvPopEmail.text = updateData()
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
        val intent = Intent(this@BiodataActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        lifecycleScope.launch {
            val credentialManager = CredentialManager.create(this@BiodataActivity)
            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            finish()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
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