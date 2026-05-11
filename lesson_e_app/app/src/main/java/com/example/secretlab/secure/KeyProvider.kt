package com.example.secretlab.secure

/**
 * Supplies a symmetric key used for local authenticated encryption.
 *
 * In a production app this would typically be backed by Android Keystore and (optionally)
 * protected with a user authentication policy. In this lab we keep the interface tiny and
 * provide an in-memory implementation so everything is testable on the JVM.
 */
fun interface KeyProvider {
    fun getOrCreateAesKey(): ByteArray
}

