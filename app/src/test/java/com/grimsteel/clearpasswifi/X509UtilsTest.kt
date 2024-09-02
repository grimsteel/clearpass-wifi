package com.grimsteel.clearpasswifi

import android.os.Build
import com.grimsteel.clearpasswifi.data.base64PEMEncode
import org.junit.Test

import org.junit.Assert.*
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class X509UtilsTest {
    @OptIn(ExperimentalUnsignedTypes::class)
    @Test
    fun test_base64PEMEncode() {
        val lineEnd = Regex("\\r\\n|\\n")

        val data = ubyteArrayOf(48u, 130u, 7u, 211u, 48u, 130u, 5u, 187u, 160u, 3u, 2u, 1u, 2u, 2u, 8u, 94u, 195u, 183u, 166u, 67u, 127u, 164u, 224u, 48u, 13u, 6u, 9u, 42u, 134u, 72u, 134u, 247u, 13u, 1u, 1u, 5u, 5u, 0u, 48u, 66u, 49u, 18u, 48u, 16u, 6u, 3u, 85u, 4u, 3u, 12u, 9u, 65u, 67u, 67u, 86u, 82u, 65u, 73u, 90u, 49u, 49u, 16u, 48u, 14u, 6u, 3u, 85u, 4u, 11u, 12u, 7u, 80u, 75u, 73u, 65u, 67u, 67u, 86u, 49u, 13u, 48u, 11u, 6u, 3u, 85u, 4u, 10u, 12u, 4u, 65u, 67u, 67u, 86u, 49u, 11u, 48u, 9u, 6u, 3u, 85u, 4u, 6u, 19u, 2u, 69u, 83u, 48u, 30u, 23u, 13u, 49u, 49u, 48u, 53u, 48u, 53u, 48u, 57u, 51u, 55u, 51u, 55u, 90u, 23u, 13u, 51u, 48u, 49u, 50u, 51u, 49u, 48u, 57u, 51u, 55u, 51u, 55u, 90u, 48u, 66u, 49u, 18u, 48u, 16u, 6u, 3u, 85u, 4u, 3u, 12u, 9u, 65u, 67u, 67u, 86u, 82u, 65u, 73u, 90u, 49u, 49u, 16u, 48u, 14u, 6u, 3u, 85u, 4u, 11u, 12u, 7u, 80u, 75u, 73u, 65u, 67u, 67u, 86u, 49u, 13u, 48u, 11u, 6u, 3u, 85u, 4u, 10u, 12u, 4u, 65u, 67u, 67u, 86u, 49u, 11u, 48u, 9u, 6u, 3u, 85u, 4u, 6u, 19u, 2u, 69u, 83u, 48u, 130u, 2u, 34u, 48u, 13u, 6u, 9u, 42u, 134u, 72u, 134u, 247u, 13u, 1u, 1u, 1u, 5u, 0u, 3u, 130u, 2u, 15u, 0u, 48u, 130u, 2u, 10u, 2u, 130u, 2u, 1u, 0u, 155u, 169u, 171u, 191u, 97u, 74u, 151u, 175u, 47u, 151u, 102u, 154u, 116u, 95u, 208u, 217u, 150u, 253u, 207u, 226u, 228u, 102u, 239u, 31u, 31u, 71u, 51u, 194u, 68u, 163u, 223u, 154u, 222u, 31u, 181u, 84u, 221u, 21u, 124u, 105u, 53u, 17u, 111u, 187u, 200u, 12u, 142u, 106u, 24u)
            .toByteArray()

        val result = """
            MIIH0zCCBbugAwIBAgIIXsO3pkN/pOAwDQYJKoZIhvcNAQEFBQAwQjESMBAGA1UE
            AwwJQUNDVlJBSVoxMRAwDgYDVQQLDAdQS0lBQ0NWMQ0wCwYDVQQKDARBQ0NWMQsw
            CQYDVQQGEwJFUzAeFw0xMTA1MDUwOTM3MzdaFw0zMDEyMzEwOTM3MzdaMEIxEjAQ
            BgNVBAMMCUFDQ1ZSQUlaMTEQMA4GA1UECwwHUEtJQUNDVjENMAsGA1UECgwEQUND
            VjELMAkGA1UEBhMCRVMwggIiMA0GCSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCb
            qau/YUqXry+XZpp0X9DZlv3P4uRm7x8fRzPCRKPfmt4ftVTdFXxpNRFvu8gMjmoY"""
            .trimIndent()
            .replace(lineEnd, System.lineSeparator())

        assertEquals(base64PEMEncode(data), result)
    }
}