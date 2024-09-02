package com.grimsteel.clearpasswifi.data

import android.os.Build
import io.ktor.util.encodeBase64
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.Base64

private const val BEGIN_CERT = "-----BEGIN CERTIFICATE-----"
private const val END_CERT = "-----END CERTIFICATE-----"
private const val BEGIN_KEY = "-----BEGIN PRIVATE KEY-----"
private const val END_KEY = "-----END PRIVATE KEY-----"
private const val PEM_LINE_LENGTH = 64

fun X509Certificate.commonName() = subjectX500Principal.name
    .split(",")
    .map { it.split("=") }
    .find { it[0] == "CN" }
    ?.get(1)

/**
 * Encode base64 with a line length of 64 chars
 */
fun base64PEMEncode(data: ByteArray): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Base64.getMimeEncoder(PEM_LINE_LENGTH, System.lineSeparator().toByteArray())
            .encode(data)
            .decodeToString()
    } else {
        data.encodeBase64()
            .chunked(PEM_LINE_LENGTH)
            .joinToString(System.lineSeparator())
    }
}

/**
 * Return the PEM-encoded form of this certificate
 */
fun X509Certificate.toPEM(): String {
    val sb = StringBuilder()
    sb.append(BEGIN_CERT)
    sb.append(System.lineSeparator())
    sb.append(base64PEMEncode(this.encoded))
    sb.append(System.lineSeparator())
    sb.append(END_CERT)
    sb.append(System.lineSeparator())
    return sb.toString()
}

fun PrivateKey.toPEM(): String {
    val sb = StringBuilder()
    sb.append(BEGIN_KEY)
    sb.append(System.lineSeparator())
    sb.append(base64PEMEncode(this.encoded))
    sb.append(System.lineSeparator())
    sb.append(END_KEY)
    sb.append(System.lineSeparator())
    return sb.toString()
}