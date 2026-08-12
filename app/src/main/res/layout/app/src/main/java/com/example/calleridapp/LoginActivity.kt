package com.example.calleridapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("CallerIDPrefs", Context.MODE_PRIVATE)
        val alreadyLoggedIn = prefs.getBoolean("loggedIn", false)

        if (alreadyLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val nameInput = findViewById<EditText>(R.id.loginName)
        val numberInput = findViewById<EditText>(R.id.loginNumber)

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            val name = nameInput.text.toString().trim()
            val number = numberInput.text.toString().trim()

            if (name.isEmpty() || number.isEmpty()) {
                Toast.makeText(this, "Dono field bharo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirestoreHelper.uploadContact(number, name)

            prefs.edit()
                .putBoolean("loggedIn", true)
                .putString("userName", name)
                .putString("userNumber", number)
                .apply()

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
