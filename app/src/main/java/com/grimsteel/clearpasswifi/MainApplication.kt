package com.grimsteel.clearpasswifi

import android.app.Application
import com.grimsteel.clearpasswifi.data.NetworkDao
import com.grimsteel.clearpasswifi.data.NetworkDatabase

class MainApplication : Application() {
    lateinit var networkDao: NetworkDao

    override fun onCreate() {
        super.onCreate()
        networkDao = NetworkDatabase.getDatabase(this).networkDao()
    }
}