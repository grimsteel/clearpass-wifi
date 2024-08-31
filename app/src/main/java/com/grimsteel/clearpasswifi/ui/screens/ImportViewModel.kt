package com.grimsteel.clearpasswifi.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.grimsteel.clearpasswifi.R
import com.grimsteel.clearpasswifi.data.Network
import com.grimsteel.clearpasswifi.data.NetworkDao
import com.grimsteel.clearpasswifi.onboard.CredentialParser
import com.grimsteel.clearpasswifi.onboard.getCredentials
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

data class ImportScreenUiState(
    val networkOtp: String = "",
    val networkUrl: String = "",
    val dialogErrorMessage: String = "",
    val loading: Boolean = false
)

class ImportViewModel(private val networkDao: NetworkDao) : ViewModel() {
    private val _importScreenState = MutableStateFlow(ImportScreenUiState())
    val importScreenState: StateFlow<ImportScreenUiState> = _importScreenState.asStateFlow()

    private fun readFileString(context: Context, uri: Uri): String {
        // read the file
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    private suspend fun handleParser(parser: CredentialParser) {
        // parse the credentials file and store data

        parser.parse()
        val network = parser.toNetwork()
        parser.storePrivateKeys()
        networkDao.insert(network)
    }

    fun useQuick1xFile(context: Context, fileUri: Uri) {
        setLoading(true)

        val contents = readFileString(context, fileUri)

        // parse it as json
        try {
            val parsedJson = JSONObject(contents)
            val networkUrl = parsedJson.getString("network.url")
            val networkOtp = parsedJson.getString("network.otp")
            _importScreenState.update {
                it.copy(networkOtp = networkOtp, networkUrl = networkUrl)
            }
        } catch (e: JSONException) {
            Log.w("ImportViewModel", "Invalid JSON: $e")
            // failed to parse file
            Toast.makeText(
                context,
                context.getString(R.string.fail_parse_json),
                Toast.LENGTH_LONG
            )
                .show()
        }

        setLoading(false)
    }

    suspend fun useXmlCredentialsFile(context: Context, fileUri: Uri) {
        setLoading(true)

        try {
            context.contentResolver.openInputStream(fileUri)?.use {
                val parser = CredentialParser(it)
                handleParser(parser)
            }
        } finally {
            setLoading(false)
        }
    }

    fun updateNetworkUrl(url: String) {
        _importScreenState.update {
            it.copy(networkUrl = url)
        }
    }

    fun updateNetworkOtp(otp: String) {
        _importScreenState.update {
            it.copy(networkOtp = otp)
        }
    }

    fun updateDialogErrorMessage(message: String) {
        _importScreenState.update {
            it.copy(dialogErrorMessage = message)
        }
    }

    private fun setLoading(loading: Boolean) {
        _importScreenState.update {
            it.copy(loading = loading)
        }
    }

    // load credentials from the set URL/OTP
    suspend fun loadCredentials(context: Context) {
        setLoading(true)

        val state = _importScreenState.value
        if (state.networkOtp.isNotEmpty() && state.networkUrl.isNotEmpty()) {
            // make sure the url is actually a URL
            try {
                val url = URL(state.networkUrl)
                val response = getCredentials(
                    url,
                    state.networkOtp
                )
                val parser = CredentialParser(response)
                handleParser(parser)
            } catch (e: MalformedURLException) {
                Log.w("ImportViewModel", "Invalid URL: $e")
                Toast.makeText(
                    context,
                    context.getString(R.string.invalid_url),
                    Toast.LENGTH_SHORT
                )
                    .show()
            } finally {
                setLoading(false)
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.missing_url_otp),
                Toast.LENGTH_LONG
            )
                .show()
        }

        setLoading(false)
    }
}