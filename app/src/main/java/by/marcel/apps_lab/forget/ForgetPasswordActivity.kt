package by.marcel.apps_lab.forget

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import by.marcel.apps_lab.R
import by.marcel.apps_lab.databinding.ActivityForgetPasswordBinding
import by.marcel.apps_lab.loginPage.LoginActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ForgetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgetPasswordBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnForget.setOnClickListener {
            val email = binding.edtForgetPassword.text.toString()
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