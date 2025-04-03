package com.example.quiz.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.quiz.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 3 // Increased to 3 for new column
        private const val DATABASE_NAME = "UserDatabase.db"

        // Table name
        private const val TABLE_USERS = "users"

        // Column names
        private const val KEY_ID = "id"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_ROLE = "role"
        private const val KEY_IS_VERIFIED = "is_verified"
        private const val KEY_PENGUINS = "penguins" // New column for currency

        // User roles
        const val ROLE_ADMIN = "Администрация"
        const val ROLE_VOLUNTEER = "Волонтёры"
        const val ROLE_PET_OWNER = "Владельцы животных"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_PASSWORD + " TEXT,"
                + KEY_ROLE + " TEXT,"
                + KEY_IS_VERIFIED + " INTEGER DEFAULT 0,"
                + KEY_PENGUINS + " INTEGER DEFAULT 0" + ")")
        db.execSQL(createTableQuery)

        // Insert default users
        insertDefaultUsers(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add is_verified column if upgrading from version 1
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $KEY_IS_VERIFIED INTEGER DEFAULT 0")
                
                // Verify default users
                val adminValues = ContentValues()
                adminValues.put(KEY_IS_VERIFIED, 1)
                db.update(TABLE_USERS, adminValues, "$KEY_ROLE = ?", arrayOf(ROLE_ADMIN))
                
                // Verify all existing users
                val userValues = ContentValues()
                userValues.put(KEY_IS_VERIFIED, 1)
                db.update(TABLE_USERS, userValues, null, null)
            } catch (e: Exception) {
                // If error occurs, just continue to next upgrade step
            }
        }
        
        if (oldVersion < 3) {
            // Add penguins column if upgrading from version 2
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $KEY_PENGUINS INTEGER DEFAULT 0")
            } catch (e: Exception) {
                // If error occurs, recreate the table
                db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
                onCreate(db)
            }
        }
    }

    private fun insertDefaultUsers(db: SQLiteDatabase) {
        // Admin user (with 1000 penguins)
        val adminValues = ContentValues()
        adminValues.put(KEY_EMAIL, "admin@example.com")
        adminValues.put(KEY_PASSWORD, "admin123")
        adminValues.put(KEY_ROLE, ROLE_ADMIN)
        adminValues.put(KEY_IS_VERIFIED, 1)
        adminValues.put(KEY_PENGUINS, 1000) // Admin starts with 1000 penguins
        db.insert(TABLE_USERS, null, adminValues)

        // Regular user
        val userValues = ContentValues()
        userValues.put(KEY_EMAIL, "user@example.com")
        userValues.put(KEY_PASSWORD, "user123")
        userValues.put(KEY_ROLE, ROLE_PET_OWNER)
        userValues.put(KEY_IS_VERIFIED, 1)
        userValues.put(KEY_PENGUINS, 100) // User starts with 100 penguins
        db.insert(TABLE_USERS, null, userValues)
    }

    fun addUser(email: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_EMAIL, email)
        values.put(KEY_PASSWORD, password)
        values.put(KEY_ROLE, ROLE_PET_OWNER)
        values.put(KEY_IS_VERIFIED, 0)
        values.put(KEY_PENGUINS, 0) // New users start with 0 penguins
        
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    fun getUser(email: String, password: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(KEY_ID, KEY_EMAIL, KEY_PASSWORD, KEY_ROLE, KEY_IS_VERIFIED, KEY_PENGUINS),
            "$KEY_EMAIL = ? AND $KEY_PASSWORD = ?",
            arrayOf(email, password),
            null,
            null,
            null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val user = User(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROLE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_VERIFIED)) == 1,
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PENGUINS))
            )
            cursor.close()
            user
        } else {
            cursor?.close()
            null
        }
    }

    // Get all verified users (for admin to manage penguins)
    fun getAllVerifiedUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(KEY_ID, KEY_EMAIL, KEY_PASSWORD, KEY_ROLE, KEY_IS_VERIFIED, KEY_PENGUINS),
            "$KEY_IS_VERIFIED = ?",
            arrayOf("1"),
            null,
            null,
            null
        )
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val user = User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROLE)),
                    true,
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PENGUINS))
                )
                users.add(user)
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return users
    }
    
    // Get user's penguin balance by ID
    fun getUserPenguins(userId: Int): Int {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(KEY_PENGUINS),
            "$KEY_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )
        
        val penguins = if (cursor != null && cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PENGUINS))
        } else {
            0
        }
        cursor?.close()
        return penguins
    }
    
    // Update user's penguin balance
    fun updatePenguins(userId: Int, amount: Int): Boolean {
        val db = this.writableDatabase
        val currentAmount = getUserPenguins(userId)
        val newAmount = currentAmount + amount
        
        // Prevent negative balances
        if (newAmount < 0) return false
        
        val values = ContentValues()
        values.put(KEY_PENGUINS, newAmount)
        
        val result = db.update(
            TABLE_USERS,
            values,
            "$KEY_ID = ?",
            arrayOf(userId.toString())
        )
        
        db.close()
        return result > 0
    }
    
    // Get user by ID
    fun getUserById(userId: Int): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(KEY_ID, KEY_EMAIL, KEY_PASSWORD, KEY_ROLE, KEY_IS_VERIFIED, KEY_PENGUINS),
            "$KEY_ID = ?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val user = User(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROLE)),
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_VERIFIED)) == 1,
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PENGUINS))
            )
            cursor.close()
            user
        } else {
            cursor?.close()
            null
        }
    }
    
    // Other existing methods...
    
    fun getUnverifiedUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(KEY_ID, KEY_EMAIL, KEY_PASSWORD, KEY_ROLE, KEY_IS_VERIFIED, KEY_PENGUINS),
            "$KEY_IS_VERIFIED = ?",
            arrayOf("0"),
            null,
            null,
            null
        )
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val user = User(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROLE)),
                    false,
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PENGUINS))
                )
                users.add(user)
            } while (cursor.moveToNext())
        }
        cursor?.close()
        return users
    }
    
    fun verifyUser(userId: Int): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_IS_VERIFIED, 1)
        
        val result = db.update(
            TABLE_USERS,
            values,
            "$KEY_ID = ?",
            arrayOf(userId.toString())
        )
        
        db.close()
        return result > 0
    }
    
    fun isAdmin(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(KEY_ROLE),
            "$KEY_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        
        if (cursor != null && cursor.moveToFirst()) {
            val role = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROLE))
            cursor.close()
            return role == ROLE_ADMIN
        }
        
        cursor?.close()
        return false
    }
    
    fun checkEmailExists(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(KEY_ID),
            "$KEY_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        val exists = cursor != null && cursor.count > 0
        cursor?.close()
        return exists
    }
}