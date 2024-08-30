package com.grimsteel.clearpasswifi.onboard

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.json.JSONObject
import java.io.InputStream
import java.net.ConnectException
import java.net.URL
import java.util.Date

class OnboardError(message: String, cause: Throwable?) : RuntimeException(message, cause)

/// Class to get network credentials from onboarding parameters
suspend fun getCredentials(onboardUrl: URL, onboardOtp: String): InputStream {
    val client = HttpClient(CIO)

    // prepare the request body
    val body = JSONObject()
    body.put("timestamp", Date().time / 1000)
    body.put("device_type", "Android")
    body.put("otp", onboardOtp)
    body.put("certificate", 1)

    val bodyString = body.toString()

    try {
        val response = client.post(onboardUrl) {
            contentType(ContentType.Application.Json)
            setBody(bodyString)
        }

        // check the response status code
        if (response.status != HttpStatusCode.OK) {
            Log.e("NetworkOnboard", "Status code is ${response.status}")
            val text = response.bodyAsText()
            throw OnboardError(
                "Onboarding server returned an error: ${response.status} - (\"$text\")",
                null
            )
        }

        return response.body()
    } catch (e: RuntimeException) {
        Log.e("NetworkOnboard", "Runtime exception while fetching data: $e")
        throw OnboardError("${e.javaClass.simpleName}: ${e.message}", e)
    } catch (e: ConnectException) {
        Log.e("NetworkOnboard", "Connect exception while fetching data: $e")
        throw OnboardError("${e.javaClass.simpleName}: ${e.message}", e)
    }
}