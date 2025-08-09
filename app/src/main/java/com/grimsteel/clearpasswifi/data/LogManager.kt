package com.grimsteel.clearpasswifi.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogManager(private val prefs: UserPreferencesRepository, private val context: Context){
    companion object {
        private const val DEBUG_LOG_FILENAME = "debug-log.txt"
    }

    @Volatile
    private var enableDebugLogging: Boolean = false

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // we can't actually use coroutines in most cases for the log() function
        scope.launch {
            prefs.prefsFlow.collect { userPrefs ->
                enableDebugLogging = userPrefs.enableDebugLogging
            }
        }
    }

    private fun getDateString(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun log(tag: String, message: String) {
        Log.w(tag, message)

        // only log to file if debugLog is set to true
        if (!enableDebugLogging) return

        val fullMessage = "[$tag ${getDateString()}] $message\n"

        // append to file
        context.openFileOutput(DEBUG_LOG_FILENAME, Context.MODE_APPEND).use {
            it.write(fullMessage.toByteArray())
        }
    }

    /**
     * copy all logs to the `file` content uri
     */
    fun saveLogsTo(file: Uri) {
        context.contentResolver.openOutputStream(file, "w")?.use { out ->
            context.openFileInput(DEBUG_LOG_FILENAME).use { input ->
                input.copyTo(out)
            }
        }
    }

    /**
     * delete all stored debug logs
     */
    fun delete() {
        context.deleteFile(DEBUG_LOG_FILENAME)
    }
}