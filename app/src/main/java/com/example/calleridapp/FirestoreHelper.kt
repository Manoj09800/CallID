package com.example.calleridapp

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Handles all database read/write operations.
 * Collection structure in Firestore:
 *
 * numbers/{phoneNumber}
 *    - names: List<String>        (all the names different users saved this number as)
 *    - reportCount: Int           (how many people marked it as spam)
 *    - totalContacts: Int         (how many people have this number saved, for confidence)
 */
object FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    fun lookupNumber(number: String, callback: (name: String?, isSpam: Boolean) -> Unit) {
        db.collection("numbers").document(number)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val names = doc.get("names") as? List<*>
                    val mostCommonName = names?.firstOrNull()?.toString()
                    val reportCount = (doc.getLong("reportCount") ?: 0L).toInt()
                    val isSpam = reportCount >= 5 // threshold — tune this later
                    callback(mostCommonName, isSpam)
                } else {
                    callback(null, false)
                }
            }
            .addOnFailureListener {
                callback(null, false)
            }
    }

    /** Called when user uploads their contacts, to build the crowdsourced database */
    fun uploadContact(number: String, name: String) {
        val docRef = db.collection("numbers").document(number)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val names = (snapshot.get("names") as? MutableList<String>) ?: mutableListOf()
            if (!names.contains(name)) names.add(0, name) // newest/most relevant first
            transaction.set(docRef, mapOf("names" to names), com.google.firebase.firestore.SetOptions.merge())
        }
    }

    /** Called when user taps "Report as Spam" */
    fun reportSpam(number: String) {
        val docRef = db.collection("numbers").document(number)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val current = (snapshot.getLong("reportCount") ?: 0L)
            transaction.set(docRef, mapOf("reportCount" to current + 1), com.google.firebase.firestore.SetOptions.merge())
        }
    }
}
