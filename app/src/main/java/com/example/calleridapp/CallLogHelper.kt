package com.example.calleridapp

import android.content.Context
import android.provider.CallLog

data class CallEntry(val number: String, val type: Int, val date: Long)

object CallLogHelper {

    fun getRecentCalls(context: Context, limit: Int = 15): List<CallEntry> {
        val entries = mutableListOf<CallEntry>()
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE),
            null, null,
            CallLog.Calls.DATE + " DESC"
        )
        cursor?.use {
            val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
            val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
            val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
            var count = 0
            while (it.moveToNext() && count < limit) {
                val number = it.getString(numberIdx) ?: continue
                val type = it.getInt(typeIdx)
                val date = it.getLong(dateIdx)
                entries.add(CallEntry(number, type, date))
                count++
            }
        }
        return entries
    }
}
