package com.grimsteel.clearpasswifi.onboard

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
import java.util.Date

class CredentialParseError(message: String, cause: Throwable?) : Exception(message, cause)

class CredentialParser(private val parser: XmlPullParser) {
    private var caCert: String? = null
    private var clientCertKey: String? = null
    private var clientKeyPassword: String? = null
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

                caCert = payloadContent
            }
            "com.apple.security.pkcs12" -> {

                clientCertKey = payloadContent
                clientKeyPassword = password
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

    fun toNetwork(organizationLogo: String?): Network {
        // include method to add private key to cert store

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
            logoUrl = organizationLogo,
            landingPage = landingPage
        )

        // parse the CA
        val parsedCaCert = CertificateFactory.getInstance("X.509")
            .generateCertificate(
                ByteArrayInputStream(
                    Base64.decode(caCert ?: throw CredentialParseError(
                        "Missing CA cert",
                        null
                    ), Base64.DEFAULT)
                )
            ) as X509Certificate

        // decode/parse the client cert/key bundle
        val pkcsKeyStore = KeyStore.getInstance("PKCS12")
        pkcsKeyStore.load(
            ByteArrayInputStream(
                Base64.decode(clientCertKey ?: throw CredentialParseError(
                    "Missing client cert/key bundle",
                    null
                ), Base64.DEFAULT)
            ),
            clientKeyPassword?.toCharArray() ?: throw CredentialParseError(
                "Missing client key encryption password",
                null
            )
        )

        val aliases = pkcsKeyStore.aliases()

        return Network(
            ssid = ssid ?: throw CredentialParseError(
                "Could not find SSID",
                null
            ),
            createdAt = Date(),
            wpaMethod = wpaMethod,
            domainSuffixMatch = domainNames.joinToString(";"),
            organization = organization,
            // password not needed for EAP-TLS
            password = null,
            identity = userName ?: throw CredentialParseError(
                "Could not find EAP identity",
                null
            ),
            caCertificate = parsedCaCert,
            // TODO: figure out how to parse encrypted PKCS#12
            clientCertificate = null
        )
    }
}