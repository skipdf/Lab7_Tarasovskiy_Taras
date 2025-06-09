package com.example.notessample

import android.app.AlertDialog
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var notes = mutableListOf<String>()
    private var ids = mutableListOf<Long>()
    private var currentUserId: Long = 1 // Для демонстрації використовуємо ID 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        dbHelper = DatabaseHelper(this)

        // Створюємо тестового користувача, якщо він ще не існує
        createTestUser()

        val editText = findViewById<EditText>(R.id.edit_text)
        val saveButton = findViewById<Button>(R.id.save_button)
        listView = findViewById(R.id.myListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notes)
        listView.adapter = adapter
        registerForContextMenu(listView)

        loadNotes()

        saveButton.setOnClickListener {
            val text = editText.text.toString()
            if (text.isNotBlank()) {
                // Створюємо нотатку з заголовком та вмістом
                dbHelper.addNote(
                    title = text,
                    content = text,
                    userId = currentUserId
                )
                editText.text.clear()
                loadNotes()
            }
        }
    }

    private fun createTestUser() {
        // Створюємо тестового користувача для демонстрації
        currentUserId = dbHelper.addUser(
            username = "test_user",
            email = "test@example.com",
            password = "password123"
        )
    }

    private fun loadNotes() {
        notes.clear()
        ids.clear()
        val cursor = dbHelper.getAllNotesWithCategory()
        while (cursor.moveToNext()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
            val categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
            
            // Форматуємо текст нотатки
            val noteText = if (categoryName != null) {
                "[$categoryName] $title: $content"
            } else {
                "$title: $content"
            }
            
            ids.add(id)
            notes.add(noteText)
        }
        cursor.close()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("Опції")
        menu.add(0, v.id, 0, "Видалити")
        menu.add(0, v.id, 0, "Редагувати")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val position = info.position
        val id = ids[position]

        when (item.title) {
            "Видалити" -> {
                AlertDialog.Builder(this)
                    .setTitle("Підтвердження")
                    .setMessage("Ви впевнені, що хочете видалити цю нотатку?")
                    .setPositiveButton("Так") { _, _ ->
                        dbHelper.deleteNote(id)
                        loadNotes()
                    }
                    .setNegativeButton("Ні", null)
                    .show()
                return true
            }
            "Редагувати" -> {
                val note = notes[position]
                val editText = EditText(this)
                editText.setText(note)
                
                AlertDialog.Builder(this)
                    .setTitle("Редагувати нотатку")
                    .setView(editText)
                    .setPositiveButton("Зберегти") { _, _ ->
                        val newText = editText.text.toString()
                        if (newText.isNotBlank()) {
                            dbHelper.updateNote(
                                noteId = id,
                                title = newText,
                                content = newText,
                                categoryId = null
                            )
                            loadNotes()
                        }
                    }
                    .setNegativeButton("Скасувати", null)
                    .show()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }
}