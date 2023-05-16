package com.example.dacs3.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.dacs3.add_acc.RegisterActivity
import com.example.dacs3.admin.DashboardActivity
import com.example.dacs3.dataBase
import com.example.dacs3.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            dataBase.email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            dataBase.password = binding.passwordEt.text.toString().trim()
            viewModel.login(email, password)
        }

        viewModel.loginSuccess.observe(this, {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        })

        viewModel.loginError.observe(this, { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        })

        binding.noAccountTv.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}