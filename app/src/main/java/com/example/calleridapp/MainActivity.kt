package com.example.calleridapp

import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.Settings
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.ANSWER_PHONE_CALLS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchInput = findViewById<EditText>(R.id.searchInput)
        val searchResult = findViewById<TextView>(R.id.searchResult)

        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            val number = searchInput.text.toString().trim()
            if (number.isEmpty()) {
                Toast.makeText(this, "Number daalo pehle", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchResult.text = "Searching..."
            FirestoreHelper.lookupNumber(number) { name, isSpam ->
                runOnUiThread {
                    if (name != null) {
                        searchResult.text = if (isSpam) "$name ⚠️ (Likely Spam)" else "✅ $name"
                    } else {
                        searchResult.text = "Koi record nahi mila is number ka"
                    }
                }
            }
        }

        findViewById<Button>(R.id.btnGrantPermissions).setOnClickListener {
            requestAllPermissions()
        }

        findViewById<Button>(R.id.btnSetDefaultApp).setOnClickListener {
            requestDefaultCallScreeningRole()
        }

        findViewById<Button>(R.id.btnOverlayPermission).setOnClickListener {
            requestOverlayPermission()
        }

        findViewById<Button>(R.id.btnSyncContacts).setOnClickListener {
            syncContactsToDatabase()
        }

        loadRecentCalls()
    }

    private fun loadRecentCalls() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val container = findViewById<LinearLayout>(R.id.recentCallsContainer)
        container.removeAllViews()

        val calls = CallLogHelper.getRecentCalls(this)
        for (call in calls) {
            val row = TextView(this)
            row.text = call.number
            row.textSize = 16f
            row.setPadding(16, 24, 16, 24)
            row.gravity = Gravity.CENTER_VERTICAL
            row.setOnClickListener {
                val intent = Intent(this, ContactProfileActivity::class.java)
                intent.putExtra("number", call.number)
                startActivity(intent)
            }
            container.addView(row)

            FirestoreHelper.lookupNumber(call.number) { name, isSpam ->
                runOnUiThread {
                    val label = if (name != null) {
                        if (isSpam) "$name ⚠️ Spam — ${call.number}" else "$name — ${call.number}"
                    } else {
                        call.number
                    }
                    row.text = label
                }
            }
        }
    }

    private fun requestAllPermissions() {
        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
        } else {
            Toast.makeText(this, "All permissions already granted", Toast.LENGTH_SHORT).show()
            loadRecentCalls()
        }
    }

    private fun requestDefaultCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                    startActivityForResult(intent, 200)
                } else {
                    Toast.makeText(this, "Already the default Caller ID app", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Not needed on this Android version", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            Toast.makeText(this, "Overlay permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun syncContactsToDatabase() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Contacts permission needed first", Toast.LENGTH_SHORT).show()
            return
        }

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null, null
        )

        var count = 0
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx) ?: continue
                val number = it.getString(numberIdx)?.replace(" ", "")?.replace("-", "") ?: continue
                FirestoreHelper.uploadContact(number, name)
                count++
            }
        }
        Toast.makeText(this, "$count contacts synced", Toast.LENGTH_SHORT).show()
    }
}
