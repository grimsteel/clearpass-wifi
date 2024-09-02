package com.grimsteel.clearpasswifi.data

import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.KeyFactory
import java.security.KeyStore
import java.security.KeyStore.SecretKeyEntry
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

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
    val clientPrivateKey: ByteArray?,

    val identity: String?,
    val password: String?,
    val domainSuffixMatch: String?
) {
    /**
     * Decrypt the stored private key
     */
    fun getPrivateKey(): PrivateKey? {
        // load the encryption key
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val encryptionKeyEntry = keyStore.getEntry(
            KEYSTORE_SECRET_KEY.format(id),
            null
        ) as? SecretKeyEntry ?: return null

        // split the iv/data buffer
        val iv = clientPrivateKey?.slice(0..<12) ?: return null
        val data = clientPrivateKey.slice(12..<clientPrivateKey.size)

        // decrypt
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, encryptionKeyEntry.secretKey, GCMParameterSpec(128, iv.toByteArray()))
        val decoded = cipher.doFinal(data.toByteArray())

        // parse
        val factory = KeyFactory.getInstance("RSA")
        return factory.generatePrivate(PKCS8EncodedKeySpec(decoded))
    }

    /**
     * Create a WifiNetworkSuggestion for this network
     * Only works on S and greater
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun toWifiSuggestion(): WifiNetworkSuggestion? {
        // retrieve pk from secure storage
        val pk = getPrivateKey()

        if (pk != null && clientCertificate != null) {
            val eapConfig = WifiEnterpriseConfig().also {
                it.identity = identity
                it.domainSuffixMatch = domainSuffixMatch
                it.caCertificate = caCertificate
                it.eapMethod = when (wpaMethod) {
                    WpaMethod.EapTls -> WifiEnterpriseConfig.Eap.TLS
                }
                it.setClientKeyEntry(pk, clientCertificate)
            }
            val suggestion = WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setMacRandomizationSetting(WifiNetworkSuggestion.RANDOMIZATION_PERSISTENT)
                .setWpa2EnterpriseConfig(eapConfig)
                .build()
            return suggestion
        } else {
            return null
        }
    }

    companion object {
        const val KEYSTORE_SECRET_KEY = "%s-encryption-key"
    }
}