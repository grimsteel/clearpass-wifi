package com.grimsteel.clearpasswifi.data

import androidx.room.TypeConverter
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Date

class Converters {
    @TypeConverter
    fun toDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun fromDate(value: Date?): Long? {
        return value?.time
    }

    @TypeConverter
    fun toWpaMethod(value: Int?): WpaMethod? {
        return value?.let { enumValues<WpaMethod>()[it] }
    }

    @TypeConverter
    fun fromWpaMethod(value: WpaMethod?): Int? {
        return value?.ordinal
    }

    @TypeConverter
    fun toX509Cert(value: ByteArray?): X509Certificate? {
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificate(ByteArrayInputStream(value)) as? X509Certificate
    }

    @TypeConverter
    fun fromX509Cert(value: X509Certificate?): ByteArray? {
        return value?.encoded
    }
}