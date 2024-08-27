package com.grimsteel.clearpasswifi.onboard

import org.xmlpull.v1.XmlPullParser

class CredentialParser(val parser: XmlPullParser) {
    private var caCert: String? = null
    private var clientCertKey: String? = null
    private var clientKeyPassword: String? = null
    private var ssid: String? = null
    private var hidden = false
    private var domainNames: List<String> = listOf()
    private var userName: String? = null
    private var userPassword: String? = null

    fun parse() {
        // plist
        parser.nextTag()
        // dict
        parser.nextTag()
    }
}