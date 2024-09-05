package by.marcel.apps_lab.loginPage

import by.marcel.apps_lab.mainhome.MainActivity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import by.marcel.apps_lab.R
import by.marcel.apps_lab.biodata.BiodataActivity
import by.marcel.apps_lab.databinding.ActivityLoginBinding
import by.marcel.apps_lab.forget.ForgetPasswordActivity
import by.marcel.apps_lab.registerPage.RegisterActivity
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
    private lateinit var binding : ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        auth.currentUser?.let {
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val isUserDataSaved = sharedPref.getBoolean("isUserDataSaved", false)

            if (isUserDataSaved) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                return
            } else {
                showProgressBar()
                checkUserData(it.uid)
            }
        }

        binding.btnGoogle.setOnClickListener(){
            showProgressBar()
            google()
        }

        binding.tvRegister.setOnClickListener {
            showProgressBar()
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tvForget.setOnClickListener {
            showProgressBar()
            val intent = Intent(this, ForgetPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.signInBtn.setOnClickListener {
            val email = binding.edtEmailLogin.text.toString()
            val password = binding.edtPasswordLogin.text.toString()
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
        val tvMessage = binding.tvRegister
        val fullText = getString(R.string.dontHaveAccount)
        val spannableString = SpannableString(fullText)
        val appsLabStart = fullText.indexOf("Login")
        val appLabEnd = appsLabStart + "Login".length

        spannableString.setSpan(
            ForegroundColorSpan(Color.YELLOW),
            appsLabStart,
            appLabEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvMessage.text = spannableString
    }

    private fun checkUserData(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                hideProgressBar()
                if (document.exists() && document.contains("Username") && document.contains("Phone") && document.contains("Nim")) {
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    startActivity(Intent(this, BiodataActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener {
                hideProgressBar()
                showSnackbar("Failed to check user data")
                startActivity(Intent(this, BiodataActivity::class.java))
                finish()
            }
    }

    private fun google() {
        val credentialManager = CredentialManager.create(this)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.your_app_client_id)) // Ensure this is set in your strings.xml
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
                Log.e(TAG, "GetCredentialException: ${e.message}", e)
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
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    Log.e(TAG, "Unexpected type of credential: ${credential.type}")
                }
            }
            else -> {
                Log.e(TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideProgressBar()
                if (task.isSuccessful) {
                    Log.d(RegisterActivity.TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(RegisterActivity.TAG, "signInWithCredential:failure", task.exception)
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