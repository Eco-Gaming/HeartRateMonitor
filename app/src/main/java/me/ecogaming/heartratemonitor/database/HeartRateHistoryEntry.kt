package me.ecogaming.heartratemonitor.database

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

class HeartRateHistoryEntry(val dateTime: Date, val heartRateValue: Int) {

    companion object {
        @SuppressLint("SimpleDateFormat")
        fun createWithString(dateTimeString: String, heartRateValue: Int): HeartRateHistoryEntry {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val date: Date = format.parse(dateTimeString) as Date
            return HeartRateHistoryEntry(date, heartRateValue)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateTimeString(): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return format.format(dateTime)
    }
}