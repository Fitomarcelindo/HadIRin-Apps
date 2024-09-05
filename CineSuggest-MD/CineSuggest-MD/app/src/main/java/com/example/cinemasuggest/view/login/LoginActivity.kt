package com.example.cinemasuggest.view.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.cinemasuggest.R
import com.example.cinemasuggest.databinding.ActivityLoginBinding
import com.example.cinemasuggest.view.forgotpassword.ForgotPasswordActivity
import com.example.cinemasuggest.view.main.MainActivity
import com.example.cinemasuggest.view.signIn.SignInActivity
import com.google.android.ads.mediationtestsuite.activities.HomeActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Check if user is already logged in and navigate directly to HomeActivity if data is saved
        auth.currentUser?.let {
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val isUserDataSaved = sharedPref.getBoolean("isUserDataSaved", false)

            if (isUserDataSaved) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                return
            } else {
                showProgressBar()
                checkUserData(it.uid)
            }
        }

        // Navigate to sign-in page
        binding.signInHere.setOnClickListener {
            showProgressBar()
            navigateToSignIn()
        }

        // Navigate to forgot password page
        binding.forgotPasswordText.setOnClickListener {
            showProgressBar()
            navigateToForgotPassword()
        }

        // User login input
        binding.loginButton.setOnClickListener {
            val email = binding.emaillogin.text.toString()
            val password = binding.passwordlogin.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                showSnackbar("Please fill in both email and password fields")
            } else {
                showProgressBar()
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    hideProgressBar()
                    if (it.isSuccessful) {
                        showProgressBar()
                        checkUserData(auth.currentUser!!.uid)
                    }
                }.addOnFailureListener {
                    hideProgressBar()
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
        // User login with Google account
        binding.signInButton.setOnClickListener {
            signIn()
        }

    }

    private fun checkUserData(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                hideProgressBar()
                if (document.exists() && document.contains("city") && document.contains("name") && document.contains("phone")) {
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                hideProgressBar()
                showSnackbar("Failed to check user data")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToForgotPassword() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun signIn() {
        val credentialManager = CredentialManager.create(this)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                hideProgressBar()
                Log.d("Error", "No credentials available: ${e.message}")
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        hideProgressBar()
                        Log.e(SignInActivity.TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    hideProgressBar()
                    Log.e(SignInActivity.TAG, "Unexpected type of credential")
                }
            }
            else -> {
                hideProgressBar()
                Log.e(SignInActivity.TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideProgressBar()
                if (task.isSuccessful) {
                    Log.d(SignInActivity.TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(SignInActivity.TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        hideProgressBar()
        if (currentUser != null) {
            showProgressBar()
            checkUserData(currentUser.uid)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
