package com.grimsteel.clearpasswifi

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.grimsteel.clearpasswifi.data.LogManager
import com.grimsteel.clearpasswifi.data.NetworkDao
import com.grimsteel.clearpasswifi.data.NetworkDatabase
import com.grimsteel.clearpasswifi.data.UserPreferencesRepository

class MainApplication : Application() {
    lateinit var networkDao: NetworkDao
    lateinit var prefs: UserPreferencesRepository
    lateinit var logger: LogManager
    val dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

    override fun onCreate() {
        super.onCreate()
        networkDao = NetworkDatabase.getDatabase(this).networkDao()
        prefs = UserPreferencesRepository(dataStore)
        logger = LogManager(prefs, this)
    }
}