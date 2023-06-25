package me.ecogaming.heartratemonitor.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Storage(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "database.db"
        private const val DATABASE_VERSION = 1
    }


    override fun onCreate(db: SQLiteDatabase) {
        // All the tables
        db.execSQL("CREATE TABLE IF NOT EXISTS heartRateHistory (id INTEGER PRIMARY KEY, dateTime DATETIME, heartRateValue INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // nothing to do, filler because sqlite needs this function...
    }
}