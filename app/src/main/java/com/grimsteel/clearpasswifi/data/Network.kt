package com.grimsteel.clearpasswifi.data

import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiManager
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
import java.util.BitSet
import java.util.Date
import java.util.UUID
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

    private fun createEapConfig(): WifiEnterpriseConfig? {
        val pk = getPrivateKey()

        return if (pk != null && clientCertificate != null) {
            WifiEnterpriseConfig().also {
                it.identity = identity
                it.domainSuffixMatch = domainSuffixMatch
                it.caCertificate = caCertificate
                it.eapMethod = when (wpaMethod) {
                    WpaMethod.EapTls -> WifiEnterpriseConfig.Eap.TLS
                }
                it.setClientKeyEntry(pk, clientCertificate)
            }
        } else {
            null
        }
    }

    /**
     * Create a WifiConfiguration for this network
     * Only works on P and lower
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("DEPRECATION")
    fun toWifiConfig(): WifiConfiguration {
        return createEapConfig().let {
            val wifiConfig = WifiConfiguration()
            wifiConfig.enterpriseConfig = it
            wifiConfig.SSID = ssid.toByteArray().toHexString()
            //wifiConfig.networkId = UUID.fromString(id).hashCode()
            wifiConfig.status = WifiConfiguration.Status.ENABLED
            wifiConfig
        }
    }

    /**
     * Create a WifiNetworkSuggestion for this network
     * Only works on Q and greater
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun toWifiSuggestion(): WifiNetworkSuggestion? {
        return createEapConfig()?.let {
            val suggestion = WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setWpa2EnterpriseConfig(it)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                suggestion.setMacRandomizationSetting(WifiNetworkSuggestion.RANDOMIZATION_PERSISTENT)
            }
            return suggestion.build()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun findExistingWifiSuggestion(wifiManager: WifiManager): WifiNetworkSuggestion? {
        return wifiManager.networkSuggestions.find {
            // make sure ssids are equal
            if (it.ssid != this.ssid) return@find false

            // compare enterprise configs if applicable
            it.enterpriseConfig?.let { eap ->
                // check identity and enterprise config
                eap.identity == this.identity && eap.eapMethod == when (this.wpaMethod) {
                    WpaMethod.EapTls -> WifiEnterpriseConfig.Eap.TLS
                }
            } ?: true // return true if there is no enterprise config to check
        }
    }

    companion object {
        const val KEYSTORE_SECRET_KEY = "%s-encryption-key"
    }
}