package com.grimsteel.clearpasswifi.onboard

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Xml
import com.grimsteel.clearpasswifi.data.Network
import com.grimsteel.clearpasswifi.data.Organization
import com.grimsteel.clearpasswifi.data.WpaMethod
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import android.util.Base64
import java.security.KeyStore
import java.security.PrivateKey
import java.util.Date
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class CredentialParseError(message: String, cause: Throwable?) : Exception(message, cause)

class CredentialParser(private val parser: XmlPullParser) {
    private var caCert: X509Certificate? = null
    private var clientCert: X509Certificate? = null
    private var clientKey: PrivateKey? = null
    private var ssid: String? = null
    private var organizationName: String? = null
    private var hidden = false
    private var domainNames: MutableList<String> = mutableListOf()
    private var userName: String? = null
    private var userPassword: String? = null
    private var landingPage: String? = null
    private var encryptionType: String? = null
    // 13 = EAP-TLS, 21 = EAP-TTLS, 25 = PEAP
    private var eapType: Int? = null

    constructor(contents: InputStream) : this(Xml.newPullParser()){
        this.parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        this.parser.setInput(contents, null)
    }

    private fun skipCurrentTag() {
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> {
                    depth++
                }
                XmlPullParser.END_TAG -> depth--
            }
        }
    }

    private fun nextRequire(tagName: String) {
        parser.nextTag()
        parser.require(XmlPullParser.START_TAG, null, tagName)
    }

    private fun parseEapConfig() {
        // move to first <key>
        parser.nextTag()
        while (parser.name == "key") {
            when (parser.nextText()) {
                "AcceptEAPTypes" -> {
                    // parse an <array> of <integer>s

                    nextRequire("array")

                    // move to the first <integer>
                    parser.nextTag()
                    while (parser.name == "integer") {
                        // just use the last item
                        eapType = parser.nextText().toIntOrNull()
                        parser.nextTag()
                    }
                }
                "UserName" -> {
                    nextRequire("string")
                    userName = parser.nextText()
                }
                "UserPassword" -> {
                    nextRequire("string")
                    userPassword = parser.nextText()
                }
                "TLSTrustedServerNames" -> {
                    nextRequire("array")

                    parser.nextTag()
                    while (parser.name == "string") {
                        domainNames.add(parser.nextText())
                        parser.nextTag()
                    }
                }
                else -> {
                    parser.nextTag()
                    skipCurrentTag()
                }
            }
            parser.nextTag()
        }
    }

    private fun parsePayloadDict() {
        // PayloadContent/PayloadType shows up in multiple
        var payloadContent: String? = null
        var payloadType: String? = null
        var password: String? = null

        // move to first <key>
        parser.nextTag()
        while (parser.name == "key") {
            when (parser.nextText()) {
                "PayloadContent" -> {
                    nextRequire("data")
                    payloadContent = parser.nextText()
                }
                "PayloadType" -> {
                    nextRequire("string")
                    payloadType = parser.nextText()
                }
                "PayloadOrganization" -> {
                    nextRequire("string")
                    organizationName = parser.nextText()
                }
                "Password" -> {
                    nextRequire("string")
                    password = parser.nextText()
                }
                "SSID_STR" -> {
                    nextRequire("string")
                    ssid = parser.nextText()
                }
                "EncryptionType" -> {
                    nextRequire("string")
                    encryptionType = parser.nextText()
                }
                "HIDDEN_NETWORK" -> {
                    parser.nextTag()
                    // <true/> or <false/>
                    hidden = parser.name == "true"
                    // move to end tag
                    parser.nextTag()
                }
                "EAPClientConfiguration" -> {
                    nextRequire("dict")
                    parseEapConfig()
                }
                else -> {
                    parser.nextTag()
                    skipCurrentTag()
                }
            }
            // move to next <key>
            parser.nextTag()
        }

        // analyze the payload type and content for this specific one
        when (payloadType) {
            "com.apple.security.root" -> {
                // CA Certificate

                // parse the CA
                caCert = CertificateFactory.getInstance("X.509")
                    .generateCertificate(
                        ByteArrayInputStream(
                            Base64.decode(payloadContent ?: throw CredentialParseError(
                                "Missing CA certificate",
                                null
                            ), Base64.DEFAULT)
                        )
                    ) as? X509Certificate
            }
            "com.apple.security.pkcs12" -> {
                // client cert/key bundle

                val keyPassword = password?.toCharArray() ?: throw CredentialParseError(
                    "Missing client key encryption password",
                    null
                )

                // decode/parse the client cert/key bundle
                val pkcsKeyStore = KeyStore.getInstance("PKCS12")
                pkcsKeyStore.load(
                    ByteArrayInputStream(
                        Base64.decode(payloadContent ?: throw CredentialParseError(
                            "Missing client cert/key bundle",
                            null
                        ), Base64.DEFAULT)
                    ),
                    keyPassword
                )

                // try getting a cert/key out of each alias
                pkcsKeyStore.aliases().iterator().forEach { alias ->
                    (pkcsKeyStore.getCertificate(alias) as? X509Certificate).let { clientCert = it }
                    (pkcsKeyStore.getKey(alias, keyPassword) as? PrivateKey).let { clientKey = it }
                }
            }
        }
    }

    // parse with error wrapping
    fun parse() {
        try {
            rawParse()
        } catch (e: XmlPullParserException) {
            throw CredentialParseError("XML parse error: ${e.message}", e)
        }
    }

    private fun rawParse() {
        nextRequire("plist")
        nextRequire("dict")

        // move into the first <key> item
        parser.nextTag()

        // Start iterating through the key-value pairs
        while (parser.name == "key") {
            when (parser.nextText()) {
                "LandingPage" -> {
                    nextRequire("string")
                    this.landingPage = parser.nextText()
                }
                "PayloadContent" -> {
                    // array of <dict>s
                    nextRequire("array")

                    // move to first <dict>
                    parser.nextTag()

                    while (parser.name == "dict") {
                        parsePayloadDict()

                        // move to next <dict>
                        parser.nextTag()
                    }
                }
                else -> {
                    // just skip this tag
                    parser.nextTag()
                    skipCurrentTag()
                }
            }

            // move to next <key>
            parser.nextTag()
        }
    }

    fun toNetwork(): Network {
        val id = UUID.randomUUID().toString()

        val wpaMethod = when (encryptionType) {
            "WPA2" -> {
                // right now we only support EAP-TLS
                when (eapType) {
                    13 -> WpaMethod.EapTls
                    else -> throw CredentialParseError(
                    "Unsupported WPA-EAP type: $eapType",
                        null
                    )
                }
            }
            else -> throw CredentialParseError(
                "Unsupported Wi-Fi encryption method: $encryptionType",
                null
            )
        }

        val organization = Organization(
            name = organizationName ?: "Unknown organization",
            landingPage = landingPage
        )

        val ssid = this.ssid ?: throw CredentialParseError(
            "Could not find SSID",
            null
        )
        val identity = this.userName ?: throw CredentialParseError(
            "Could not find EAP identity",
            null
        )

        val encodedPrivateKey = clientKey?.let {
            // generate a secret key for encrypting the private key (stores in AndroidKeyStore)
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val spec = KeyGenParameterSpec
                .Builder(
                    Network.KEYSTORE_SECRET_KEY.format(id),
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(spec)
            val encryptionKey = keyGenerator.generateKey()

            // encrypt the private key
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)
            val encoded = cipher.doFinal(it.encoded)
            cipher.iv + encoded
        } ?: throw CredentialParseError(
            "Missing client private key",
            null
        )

        return Network(
            ssid = ssid,
            createdAt = Date(),
            wpaMethod = wpaMethod,
            domainSuffixMatch = domainNames.joinToString(";"),
            organization = organization,
            // password not needed for EAP-TLS
            password = null,
            identity = identity,
            caCertificate = caCert ?: throw CredentialParseError(
                "Missing CA certificate",
                null
            ),
            clientCertificate = clientCert ?: throw CredentialParseError(
                "Missing client certificate",
                null
            ),
            id = id,
            // by default, ssid + identity
            displayName = "$ssid ($identity)",
            clientPrivateKey = encodedPrivateKey
        )
    }
}