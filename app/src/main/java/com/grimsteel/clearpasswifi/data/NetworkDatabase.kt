package com.grimsteel.clearpasswifi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Network::class], version = 1, exportSchema = false)
abstract class NetworkDatabase : RoomDatabase() {
    abstract fun networkDao(): NetworkDao

    companion object {
        @Volatile
        private var Instance: NetworkDatabase? = null

        fun getDatabase(context: Context): NetworkDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, NetworkDatabase::class.java, "network_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}