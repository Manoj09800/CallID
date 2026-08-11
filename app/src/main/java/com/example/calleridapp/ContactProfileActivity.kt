package com.example.calleridapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ContactProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_profile)

        val number = intent.getStringExtra("number") ?: ""
        val nameText = findViewById<TextView>(R.id.profileName)
        val numberText = findViewById<TextView>(R.id.profileNumber)
        val spamText = findViewById<TextView>(R.id.profileSpamStatus)

        numberText.text = number
        nameText.text = "Loading..."

        FirestoreHelper.lookupNumber(number) { name, isSpam ->
            runOnUiThread {
                nameText.text = name ?: "Unknown"
                spamText.text = if (isSpam) "⚠️ Likely Spam" else "✅ Not marked as spam"
            }
        }

        findViewById<Button>(R.id.btnReportSpam).setOnClickListener {
            FirestoreHelper.reportSpam(number)
            Toast.makeText(this, "Reported as spam", Toast.LENGTH_SHORT).show()
        }
    }
}
