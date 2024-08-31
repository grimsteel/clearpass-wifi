package com.grimsteel.clearpasswifi.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.cert.X509Certificate
import java.util.Date

@Entity(tableName = "networks")
data class Network(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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
        const val EAP_TLS_PK_ALIAS = "%d-eap-tls-private-key"
    }
}
