package by.marcel.apps_lab.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import by.marcel.apps_lab.R
import by.marcel.apps_lab.databinding.ActivityHomeFirstBinding
import by.marcel.apps_lab.loginPage.LoginActivity
import by.marcel.apps_lab.registerPage.RegisterActivity

class HomeFirstActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeFirstBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}