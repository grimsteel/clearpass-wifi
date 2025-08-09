package com.grimsteel.clearpasswifi.onboard

import android.util.Log
import com.grimsteel.clearpasswifi.data.LogManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.json.JSONObject
import java.net.ConnectException
import java.net.URL
import java.util.Date

class OnboardError(message: String, cause: Throwable?) : RuntimeException(message, cause)

/// Class to get network credentials XML from onboarding parameters
suspend fun getCredentials(onboardUrl: URL, onboardOtp: String, logger: LogManager): String {
    val client = HttpClient(CIO)

    // prepare the request body
    val body = JSONObject()
    body.put("timestamp", Date().time / 1000)
    body.put("device_type", "Android")
    body.put("otp", onboardOtp)
    body.put("certificate", 1)

    val bodyString = body.toString()

    try {
        logger.log("NetworkOnboard", "Making request to $onboardUrl. Body: $body")

        val response = client.post(onboardUrl) {
            contentType(ContentType.Application.Json)
            setBody(bodyString)
        }

        val contents = response.bodyAsText()

        logger.log("NetworkOnboard", "Received status ${response.status}, body $body")

        // check the response status code
        if (response.status != HttpStatusCode.OK) {
            Log.e("NetworkOnboard", "Status code is ${response.status}")
            throw OnboardError(
                "Onboarding server returned an error: ${response.status} - (\"$contents\")",
                null
            )
        }

        return contents
    } catch (e: RuntimeException) {
        if (e is OnboardError) throw e
        Log.e("NetworkOnboard", "Runtime exception while fetching data", e)
        throw OnboardError("${e.javaClass.simpleName}: ${e.message ?: "No message"}", e)
    } catch (e: ConnectException) {
        Log.e("NetworkOnboard", "Connect exception while fetching data", e)
        throw OnboardError("${e.javaClass.simpleName}: ${e.message ?: "No message"}", e)
    } catch (e: Exception) {
        Log.e("NetworkOnboard", "Unknown error", e)
        throw OnboardError("Unknown error: ${e.message}.", e)
    }
}