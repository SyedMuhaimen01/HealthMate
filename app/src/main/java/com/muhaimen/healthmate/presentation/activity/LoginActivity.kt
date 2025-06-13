package com.muhaimen.healthmate.presentation.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.muhaimen.healthmate.R
import com.muhaimen.healthmate.databinding.ActivityLoginBinding
import com.muhaimen.healthmate.viewModel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty()) {
                binding.emailEditText.error = "Enter email address"
                binding.emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEditText.error = "Enter a valid email address"
                binding.emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.passwordEditText.error = "Enter password"
                binding.passwordEditText.requestFocus()
                return@setOnClickListener
            }

            authViewModel.signInUser(email, password) { isSuccess, errorMessage ->
                if (isSuccess) {
                    if(authViewModel.checkIfEmailVerified()){
                        // Navigate to the main activity
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Handle email not verified
                        binding.emailEditText.error = "Please verify your email address"
                        binding.emailEditText.requestFocus()
                    }
                } else {
                    // Handle login failure
                    binding.passwordEditText.error = errorMessage ?: "Invalid email or password"
                    binding.passwordEditText.requestFocus()
                }
            }

        }

        binding.signUpButton.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.resetPasswordButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            authViewModel.sendPasswordResetEmail(email) { isSuccess, errorMessage ->
                if (isSuccess) {
                    // Handle success
                    binding.emailEditText.error = "Password reset email sent"
                    binding.emailEditText.requestFocus()
                } else {
                    // Handle failure
                    binding.emailEditText.error = errorMessage ?: "Failed to send password reset email"
                    binding.emailEditText.requestFocus()
                }
            }
        }
    }
}