package com.example.cinemasuggest.view.forgotpassword

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.cinemasuggest.databinding.ActivityForgotPasswordBinding
import com.example.cinemasuggest.view.login.LoginActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Navigate back
        binding.btnBack.setOnClickListener {
            navigateToLogin()
        }

        // Reset password action
        binding.submitEmailButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Snackbar.make(binding.root, "Please check your email!", Snackbar.LENGTH_LONG).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        navigateToLogin()
                    }, 2000) // 2 second delay
                }
                .addOnFailureListener {
                    Snackbar.make(binding.root, it.toString(), Snackbar.LENGTH_LONG).show()
                }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
