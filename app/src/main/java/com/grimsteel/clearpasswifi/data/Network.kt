package com.grimsteel.clearpasswifi.data

import android.os.Build
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProtection
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.cert.X509Certificate
import java.util.Date

@Entity(tableName = "networks")
data class Network(
    @PrimaryKey
    val id: String,

    val displayName: String,
    val ssid: String,

    @Embedded
    val organization: Organization?,
    val createdAt: Date,

    val wpaMethod: WpaMethod,

    val caCertificate: X509Certificate?,
    val clientCertificate: X509Certificate?,

    val identity: String?,
    val password: String?,
    val domainSuffixMatch: String?
) {
    companion object {
        const val EAP_TLS_PK_ALIAS = "%s-eap-tls-private-key"
        val PRIVATE_KEY_PROTECTION by lazy {
            val protectionBuilder = KeyProtection.Builder(0)
                .setUserAuthenticationRequired(true)

            // use setAuthParams when we can, otherwise use the deprecated method
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                protectionBuilder.setUserAuthenticationParameters(
                    0,
                    KeyProperties.AUTH_DEVICE_CREDENTIAL or KeyProperties.AUTH_DEVICE_CREDENTIAL
                )
            } else {
                @Suppress("DEPRECATION")
                protectionBuilder
                    .setUserAuthenticationValidityDurationSeconds(-1)
            }

            protectionBuilder.build()
        }
    }
}

fun X509Certificate.commonName() = subjectX500Principal.name
    .split(",")
    .map { it.split("=") }
    .find { it[0] == "CN" }
    ?.get(1)