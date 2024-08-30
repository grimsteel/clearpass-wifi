package com.grimsteel.clearpasswifi.onboard

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class CredentialParser(val parser: XmlPullParser) {
    private var caCert: String? = null
    private var clientCertKey: String? = null
    private var clientKeyPassword: String? = null
    private var ssid: String? = null
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
        if (parser.isEmptyElementTag) return

        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                XmlPullParser.START_TAG -> {
                    if (!parser.isEmptyElementTag)
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
                "AcceptEapTypes" -> {
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
                }
                "EAPClientConfiguration" -> {
                    nextRequire("dict")
                    parseEapConfig()
                }
                else -> {
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
                Log.w("CredParser", "PKCS#12 ${payloadContent ?: ""}")
            }
        }
    }

    fun parse() {
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
                    skipCurrentTag()
                }
            }

            // move to next <key>
            parser.nextTag()
        }
    }
}