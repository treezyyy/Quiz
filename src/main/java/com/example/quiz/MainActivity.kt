package com.example.quiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.database.DatabaseHelper

class MainActivity : AppCompatActivity() {
    
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var userEmail: String
    private lateinit var userRole: String
    private var userId: Int = -1
    private var userPenguins: Int = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Check if user is logged in
        userEmail = intent.getStringExtra("USER_EMAIL") ?: ""
        userRole = intent.getStringExtra("USER_ROLE") ?: ""
        userId = intent.getIntExtra("USER_ID", -1)
        
        if (userEmail.isEmpty() || userRole.isEmpty() || userId == -1) {
            // If not logged in, go to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        dbHelper = DatabaseHelper(this)
        
        // Get current penguin balance
        val user = dbHelper.getUserById(userId)
        if (user != null) {
            userPenguins = user.penguins
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Update the UI to show logged in user information
        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        welcomeTextView.text = "Добро пожаловать, $userEmail\nРоль: $userRole"
        
        // Show penguin balance
        val penguinsTextView = findViewById<TextView>(R.id.textViewPenguinsBalance)
        penguinsTextView.text = "Ваши пингвины: $userPenguins"
        
        // Show admin controls if user is admin
        val adminSection = findViewById<View>(R.id.adminSectionLayout)
        val penguinManagementSection = findViewById<View>(R.id.penguinManagementLayout)
        
        if (dbHelper.isAdmin(userEmail)) {
            adminSection.visibility = View.VISIBLE
            penguinManagementSection.visibility = View.VISIBLE
            
            // Load both unverified users and all users for penguin management
            loadUnverifiedUsers()
            loadUsersForPenguinManagement()
        } else {
            adminSection.visibility = View.GONE
            penguinManagementSection.visibility = View.GONE
        }
        
        // Add logout button
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    
    private fun loadUnverifiedUsers() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewUnverifiedUsers)
        val unverifiedUsers = dbHelper.getUnverifiedUsers()
        
        if (unverifiedUsers.isEmpty()) {
            findViewById<TextView>(R.id.textViewNoUnverifiedUsers).visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.textViewNoUnverifiedUsers).visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            
            recyclerView.layoutManager = LinearLayoutManager(this)
            val adapter = UnverifiedUsersAdapter(unverifiedUsers) { userId ->
                verifyUser(userId)
            }
            recyclerView.adapter = adapter
        }
    }
    
    private fun loadUsersForPenguinManagement() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewUsers)
        val users = dbHelper.getAllVerifiedUsers()
        
        if (users.isEmpty()) {
            findViewById<TextView>(R.id.textViewNoUsers).visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.textViewNoUsers).visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            
            recyclerView.layoutManager = LinearLayoutManager(this)
            val adapter = UsersAdapter(
                users,
                { userId, amount -> addPenguinsToUser(userId, amount) },
                { userId, amount -> removePenguinsFromUser(userId, amount) }
            )
            recyclerView.adapter = adapter
        }
    }
    
    private fun addPenguinsToUser(userId: Int, amount: Int) {
        if (dbHelper.updatePenguins(userId, amount)) {
            Toast.makeText(this, "Добавлено $amount пингвинов", Toast.LENGTH_SHORT).show()
            refreshUsersList()
        } else {
            Toast.makeText(this, "Ошибка при добавлении пингвинов", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun removePenguinsFromUser(userId: Int, amount: Int) {
        if (dbHelper.updatePenguins(userId, -amount)) {
            Toast.makeText(this, "Списано $amount пингвинов", Toast.LENGTH_SHORT).show()
            refreshUsersList()
        } else {
            Toast.makeText(this, "Ошибка при списании пингвинов. Возможно, недостаточно средств.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun refreshUsersList() {
        loadUsersForPenguinManagement()
    }
    
    private fun verifyUser(userId: Int) {
        if (dbHelper.verifyUser(userId)) {
            Toast.makeText(this, "Пользователь подтвержден", Toast.LENGTH_SHORT).show()
            loadUnverifiedUsers()
            loadUsersForPenguinManagement()
        } else {
            Toast.makeText(this, "Ошибка при подтверждении пользователя", Toast.LENGTH_SHORT).show()
        }
    }
}