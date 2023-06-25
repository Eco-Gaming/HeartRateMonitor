package me.ecogaming.heartratemonitor.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context

class HeartRateHistory(context: Context) {
    private var database: Storage

    init {
        database = Storage(context)
    }

    fun pushValue(heartRateHistoryEntry: HeartRateHistoryEntry){
        val db = database.writableDatabase
        val values = ContentValues()
        values.put("dateTime", heartRateHistoryEntry.getDateTimeString())
        values.put("heartRateValue", heartRateHistoryEntry.heartRateValue)
        db.insert("heartRateHistory", null, values)
        db.close()
    }

    @SuppressLint("Range")
    fun queryList(): ArrayList<HeartRateHistoryEntry> {
        val entries = ArrayList<HeartRateHistoryEntry>()
        val db = database.readableDatabase
        // pull history by id descending (newest added first)
        val cursor = db.query("heartRateHistory", null, null, null, null, null, "id DESC")
        // gets entries and iterates through them
        while (cursor.moveToNext()) {
            val dateTimeString = cursor.getString(cursor.getColumnIndex("dateTime"))
            val heartRateValue = cursor.getString(cursor.getColumnIndex("heartRateValue"))
            entries.add(HeartRateHistoryEntry.createWithString(dateTimeString, heartRateValue.toInt()))
        }
        cursor.close()
        db.close()
        return entries
    }

    fun clearList() {
        val db = database.writableDatabase
        // there may very well be a better way of doing this but it seems to work fine
        db.delete("heartRateHistory", null, null)
    }
}