package com.grimsteel.clearpasswifi.onboard

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Xml
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Date

/// Class to get network credentials from onboarding parameters
class NetworkOnboard {
    private val onboardUrl: String
    private val onboardOtp: String

    constructor(onboardUrl: String, onboardOtp: String) {
        this.onboardOtp = onboardOtp;
        this.onboardUrl = onboardUrl;
    }

    /// construct from a file
    constructor(fileUri: Uri, context: Context) {
        // read the file
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(fileUri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }

        // parse it as json
        val parsedJson = JSONObject(stringBuilder.toString())
        val onboardUrl = parsedJson.getString("network.url")
        val onboardOtp = parsedJson.getString("network.otp")

        this.onboardUrl = onboardUrl
        this.onboardOtp = onboardOtp
    }

    suspend fun getCredentials() {
        val client = HttpClient(CIO)

        // prepare the request body
        val body = JSONObject()
        body.put("timestamp", Date().time / 1000)
        body.put("auth_username", null)
        body.put("auth_password", null)
        body.put("mac_address", "")
        body.append("network_interfaces", JSONObject())
        body.put("device_type", "Android")
        body.put("product_name", "Better Clearpass")
        body.put("product_version", 2)
        body.put("otp", onboardOtp)
        body.put("certificate", 1)

        val bodyString = body.toString()

        Log.d("HI", bodyString)

        val response = client.post(onboardUrl) {
            contentType(ContentType.Application.Json)
            setBody(bodyString)
        }

        // parse the response as xml
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(response.body())
    }
}