package com.grimsteel.clearpasswifi.onboard

import android.util.Log
import android.util.Xml
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.util.Date

/// Class to get network credentials from onboarding parameters
class NetworkOnboard(val onboardUrl: String, val onboardOtp: String) {
    suspend fun getCredentials() {
        val client = HttpClient(CIO)

        // prepare the request body
        val body = JSONObject()
        body.put("timestamp", Date().time / 1000)
        body.put("auth_username", null)
        body.put("auth_password", null)
        body.put("mac_address", "")
        body.put("network_interfaces", JSONArray({ JSONObject() }))
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

        val credentialParser = CredentialParser(parser)
        credentialParser.parse()
    }
}