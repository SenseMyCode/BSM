package com.example.secretlab.secure

import java.security.SecureRandom

class InMemoryKeyProvider(
    private val random: SecureRandom = SecureRandom(),
) : KeyProvider {
    private var key: ByteArray? = null

    override fun getOrCreateAesKey(): ByteArray {
        val existing = key
        if (existing != null) {
            return existing
        }
        val created = ByteArray(32)
        random.nextBytes(created)
        key = created
        return created
    }
}

