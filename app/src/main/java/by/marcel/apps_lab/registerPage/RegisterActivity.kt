package by.marcel.apps_lab.registerPage

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import by.marcel.apps_lab.R
import by.marcel.apps_lab.biodata.BiodataActivity
import by.marcel.apps_lab.databinding.ActivityRegisterBinding
import by.marcel.apps_lab.loginPage.LoginActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val tvMessage = binding.textView4
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

        binding.buttonGoogle.setOnClickListener(){
            showProgressBar()
            google()
        }

        binding.signupButton.setOnClickListener {
            val username = binding.edtUsername.text.toString()
            val email = binding.edtEmailRegister.text.toString()
            val password = binding.edtPasswordRegister.text.toString()

            when {
                username.isEmpty() -> binding.edtUsername.error = "Username Must be Fill"
                email.isEmpty() -> binding.edtEmailRegister.error = "Email Ust be Fill"
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.edtEmailRegister.error = "Email not Valid"
                password.isEmpty() -> binding.edtPasswordRegister.error = "Password Must Be Fill"
                password.length < 8 -> binding.edtPasswordRegister.error = "Password Must be at Least 8 Character"
                else -> registerFirebase(email, password)
            }

            binding.edtPasswordRegister.requestFocus()
        }
    }

    private fun registerFirebase( email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this){
            if(it.isSuccessful){
                auth.signOut()
                Toast.makeText(this, "Register Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }else {
                hideProgressBar()
                Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
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
                    context = this@RegisterActivity,
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                hideProgressBar()
                Log.e(ContentValues.TAG, "GetCredentialException: ${e.message}", e)
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
                        Log.e(ContentValues.TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    Log.e(ContentValues.TAG, "Unexpected type of credential: ${credential.type}")
                }
            }
            else -> {
                Log.e(ContentValues.TAG, "Unexpected type of credential")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(ContentValues.TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        hideProgressBar()
        if (currentUser != null) {
            startActivity(Intent(this@RegisterActivity, BiodataActivity::class.java))
            finish()
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

    companion object {
        const val TAG = "AuthActivity"
    }
}