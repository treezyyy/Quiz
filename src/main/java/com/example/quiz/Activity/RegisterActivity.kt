package com.example.quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quiz.database.DatabaseHelper
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextRegEmail: TextInputEditText
    private lateinit var editTextRegPassword: TextInputEditText
    private lateinit var editTextRegConfirmPassword: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var textViewLogin: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        editTextRegEmail = findViewById(R.id.editTextRegEmail)
        editTextRegPassword = findViewById(R.id.editTextRegPassword)
        editTextRegConfirmPassword = findViewById(R.id.editTextRegConfirmPassword)
        buttonRegister = findViewById(R.id.buttonRegister)
        textViewLogin = findViewById(R.id.textViewLogin)
        
        // Initialize database helper
        dbHelper = DatabaseHelper(this)

        // Set up register button click listener
        buttonRegister.setOnClickListener {
            val email = editTextRegEmail.text.toString().trim()
            val password = editTextRegPassword.text.toString().trim()
            val confirmPassword = editTextRegConfirmPassword.text.toString().trim()

            if (validateInputs(email, password, confirmPassword)) {
                if (dbHelper.checkEmailExists(email)) {
                    Toast.makeText(this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show()
                } else {
                    val userId = dbHelper.addUser(email, password)
                    if (userId != -1L) {
                        Toast.makeText(this, "Регистрация прошла успешно!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Ошибка при регистрации", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Set up login text view click listener
        textViewLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
        if (email.isEmpty()) {
            editTextRegEmail.error = "Введите email"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextRegEmail.error = "Введите корректный email"
            return false
        }

        if (password.isEmpty()) {
            editTextRegPassword.error = "Введите пароль"
            return false
        }

        if (password.length < 6) {
            editTextRegPassword.error = "Пароль должен содержать минимум 6 символов"
            return false
        }

        if (confirmPassword.isEmpty()) {
            editTextRegConfirmPassword.error = "Подтвердите пароль"
            return false
        }

        if (password != confirmPassword) {
            editTextRegConfirmPassword.error = "Пароли не совпадают"
            return false
        }

        return true
    }
}