package com.example.secretlab.debug

import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object LocalPasswordHasher {
    const val ALGORITHM_NAME = "PBKDF2WithHmacSHA256"
    const val ITERATION_COUNT = 120_000
    const val KEY_LENGTH_BITS = 256
    const val RECORD_PREFIX = "pbkdf2_sha256"

    // TODO(C07-1): derive a PBKDF2 hash using password chars, salt bytes, iteration count and key length.
    fun derive(password: String, saltText: String): String {
        val saltBytes = utf8Bytes(saltText)
        val keySpec: KeySpec = PBEKeySpec(
            password.toCharArray(),
            saltBytes,
            ITERATION_COUNT,
            KEY_LENGTH_BITS
        )
        val factory = SecretKeyFactory.getInstance(ALGORITHM_NAME)
        val resultBytes = factory.generateSecret(keySpec).encoded
        return toHex(resultBytes)
    }

    // TODO(C07-2): encode the verifier as:
    // pbkdf2_sha256$<iterations>$<saltHex>$<hashHex>
    fun buildRecord(password: String, saltText: String): String {
        val hashHex = derive(password, saltText)
        val saltHex = toHex(utf8Bytes(saltText))

        // Budujemy rekord: prefix$iteracje$saltHex$hashHex
        return "$RECORD_PREFIX$$ITERATION_COUNT$$saltHex$$hashHex"
    }

    // TODO(C07-3): parse the record, recompute the hash and compare with the stored hash.
    fun verify(password: String, record: String): Boolean {
        try {
            val parts = record.split("$")
            if (parts.size != 4) return false

            val prefix = parts[0]
            val iterations = parts[1].toInt()
            val saltHex = parts[2]
            val storedHashHex = parts[3]

            if (prefix != RECORD_PREFIX) return false

            // Aby zweryfikować, musimy odtworzyć salt tekstowy z hex
            val saltText = String(fromHex(saltHex), Charsets.UTF_8)

            // Ponownie liczymy hash dla podanego hasła
            val computedHashHex = derive(password, saltText)

            // Porównujemy wynik
            return computedHashHex == storedHashHex
        } catch (e: Exception) {
            return false
        }
    }

    fun utf8Bytes(text: String): ByteArray = text.toByteArray(Charsets.UTF_8)

    fun toHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }

    fun fromHex(text: String): ByteArray {
        require(text.length % 2 == 0)
        return text.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    @Suppress("unused")
    fun deriveReference(password: String, saltText: String): String {
        val keySpec: KeySpec = PBEKeySpec(
            password.toCharArray(),
            utf8Bytes(saltText),
            ITERATION_COUNT,
            KEY_LENGTH_BITS,
        )
        val factory = SecretKeyFactory.getInstance(ALGORITHM_NAME)
        return toHex(factory.generateSecret(keySpec).encoded)
    }
}