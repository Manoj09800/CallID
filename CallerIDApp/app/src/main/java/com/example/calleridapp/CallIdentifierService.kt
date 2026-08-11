package com.example.calleridapp

import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * This runs automatically for EVERY incoming call once the user sets this app
 * as the "Caller ID & Spam" app in Android Settings > Apps > Default Apps.
 *
 * Steps:
 * 1. Get the incoming phone number
 * 2. Look it up in Firestore (our crowdsourced database)
 * 3. Launch the overlay popup with the name/spam-status
 */
class CallIdentifierService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: return

        // Look up the number in our database
        FirestoreHelper.lookupNumber(phoneNumber) { name, isSpam ->
            val displayName = name ?: "Unknown"

            val overlayIntent = Intent(this, OverlayService::class.java).apply {
                putExtra("number", phoneNumber)
                putExtra("name", displayName)
                putExtra("isSpam", isSpam)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startService(overlayIntent)
        }

        // We are NOT blocking the call automatically here — just identifying it.
        // (You can extend this later to auto-reject known spam numbers.)
        val response = CallResponse.Builder().build()
        respondToCall(callDetails, response)
    }
}
