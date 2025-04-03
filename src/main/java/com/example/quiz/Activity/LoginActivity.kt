package com.example.quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quiz.database.DatabaseHelper
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewRegister: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewRegister = findViewById(R.id.textViewRegister)
        
        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up login button click listener
        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                val user = dbHelper.getUser(email, password)
                if (user != null) {
                    // Check if user is verified
                    if (user.isVerified) {
                        // Login successful, navigate to main activity
                        Toast.makeText(this, "Вход выполнен успешно!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("USER_EMAIL", user.email)
                        intent.putExtra("USER_ROLE", user.role)
                        intent.putExtra("USER_ID", user.id) // Pass user ID to MainActivity
                        startActivity(intent)
                        finish()
                    } else {
                        // User is not verified
                        Toast.makeText(this, "Ваш аккаунт ожидает подтверждения администратором", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Login failed
                    Toast.makeText(this, "Неверный email или пароль", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up register text view click listener
        textViewRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            editTextEmail.error = "Введите email"
            return false
        }

        if (password.isEmpty()) {
            editTextPassword.error = "Введите пароль"
            return false
        }

        return true
    }
}