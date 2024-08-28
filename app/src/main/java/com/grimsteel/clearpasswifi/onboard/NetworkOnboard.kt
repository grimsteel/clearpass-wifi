package com.grimsteel.clearpasswifi.onboard

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.json.JSONArray
import org.json.JSONObject
import java.io.Reader
import java.net.URL
import java.util.Date

/// Class to get network credentials from onboarding parameters
suspend fun getCredentials(onboardUrl: URL, onboardOtp: String): Reader {
    val client = HttpClient(CIO)

    // prepare the request body
    val body = JSONObject()
    body.put("timestamp", Date().time / 1000)
    body.put("auth_username", null)
    body.put("auth_password", null)
    body.put("mac_address", "02:00:00:00:00:00")
    val interfaces = JSONArray()
    interfaces.put(JSONObject())
    body.put("network_interfaces", interfaces)
    body.put("device_type", "Android")
    body.put("product_name", "Better Clearpass")
    body.put("product_version", 2)
    body.put("otp", onboardOtp)
    body.put("certificate", 1)

    val bodyString = body.toString()

    val response = client.post(onboardUrl) {
        contentType(ContentType.Application.Json)
        setBody(bodyString)
    }

    return response.body()
}