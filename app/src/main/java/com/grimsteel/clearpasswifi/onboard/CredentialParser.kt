package com.grimsteel.clearpasswifi.onboard

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class CredentialParser(val parser: XmlPullParser) {
    private var caCert: String? = null
    private var clientCertKey: String? = null
    private var clientKeyPassword: String? = null
    private var ssid: String? = null
    private var hidden = false
    private var domainNames: List<String> = listOf()
    private var userName: String? = null
    private var userPassword: String? = null
    private var landingPage: String? = null

    constructor(contents: InputStream) : this(Xml.newPullParser()){
        this.parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        this.parser.setInput(contents, null)
    }

    private fun skipCurrentTag() {
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

    fun parse() {
        nextRequire("plist")
        nextRequire("dict")

        // move into the first <key> item
        parser.nextTag()

        // Start iterating through the key-value pairs
        while (parser.name == "key") {
            val key = parser.nextText()
            when (key) {
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
                        // move to first <key
                        parser.nextTag()
                        while (parser.name == "key") {

                        }

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