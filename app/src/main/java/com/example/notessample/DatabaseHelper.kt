package com.example.notessample

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "myapp.db"
        private const val DATABASE_VERSION = 1

        // Таблиці
        private const val TABLE_USERS = "users"
        private const val TABLE_NOTES = "notes"
        private const val TABLE_CATEGORIES = "categories"

        // Загальні колонки
        private const val KEY_ID = "id"
        private const val KEY_CREATED_AT = "created_at"

        // Колонки для users
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"

        // Колонки для notes
        private const val KEY_TITLE = "title"
        private const val KEY_CONTENT = "content"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_CATEGORY_ID = "category_id"

        // Колонки для categories
        private const val KEY_NAME = "name"
        private const val KEY_DESCRIPTION = "description"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Створення таблиці користувачів
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_USERNAME TEXT NOT NULL,
                $KEY_EMAIL TEXT UNIQUE NOT NULL,
                $KEY_PASSWORD TEXT NOT NULL,
                $KEY_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // Створення таблиці категорій
        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_NAME TEXT NOT NULL,
                $KEY_DESCRIPTION TEXT,
                $KEY_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // Створення таблиці нотаток
        val createNotesTable = """
            CREATE TABLE $TABLE_NOTES (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_TITLE TEXT NOT NULL,
                $KEY_CONTENT TEXT,
                $KEY_USER_ID INTEGER,
                $KEY_CATEGORY_ID INTEGER,
                $KEY_CREATED_AT DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY ($KEY_USER_ID) REFERENCES $TABLE_USERS($KEY_ID),
                FOREIGN KEY ($KEY_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($KEY_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createCategoriesTable)
        db.execSQL(createNotesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Видалення старих таблиць
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")

        // Створення нових таблиць
        onCreate(db)
    }

    // Методи для роботи з користувачами
    fun addUser(username: String, email: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_USERNAME, username)
            put(KEY_EMAIL, email)
            put(KEY_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    // Методи для роботи з категоріями
    fun addCategory(name: String, description: String? = null): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_NAME, name)
            description?.let { put(KEY_DESCRIPTION, it) }
        }
        return db.insert(TABLE_CATEGORIES, null, values)
    }

    // Методи для роботи з нотатками
    fun addNote(title: String, content: String?, userId: Long, categoryId: Long? = null): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TITLE, title)
            content?.let { put(KEY_CONTENT, it) }
            put(KEY_USER_ID, userId)
            categoryId?.let { put(KEY_CATEGORY_ID, it) }
        }
        return db.insert(TABLE_NOTES, null, values)
    }

    // Метод для отримання всіх нотаток з категорією
    fun getAllNotesWithCategory(): android.database.Cursor {
        val db = this.readableDatabase
        return db.rawQuery("""
            SELECT n.*, c.$KEY_NAME as category_name 
            FROM $TABLE_NOTES n 
            LEFT JOIN $TABLE_CATEGORIES c ON n.$KEY_CATEGORY_ID = c.$KEY_ID 
            ORDER BY n.$KEY_CREATED_AT DESC
        """.trimIndent(), null)
    }

    // Метод для пошуку нотаток за заголовком
    fun searchNotesByTitle(query: String): android.database.Cursor {
        val db = this.readableDatabase
        return db.query(
            TABLE_NOTES,
            null,
            "$KEY_TITLE LIKE ?",
            arrayOf("%$query%"),
            null,
            null,
            "$KEY_CREATED_AT DESC"
        )
    }

    // Метод для видалення нотатки
    fun deleteNote(noteId: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NOTES, "$KEY_ID = ?", arrayOf(noteId.toString()))
    }

    // Метод для оновлення нотатки
    fun updateNote(noteId: Long, title: String, content: String?, categoryId: Long?): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_TITLE, title)
            content?.let { put(KEY_CONTENT, it) }
            categoryId?.let { put(KEY_CATEGORY_ID, it) }
        }
        return db.update(TABLE_NOTES, values, "$KEY_ID = ?", arrayOf(noteId.toString()))
    }
} 