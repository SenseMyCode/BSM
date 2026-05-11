package com.example.secretlab.secure

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.security.SecureRandom

class BiometricBoundSecretStoreStudentTest {
    private val random = SecureRandom()

    @Test
    fun refusesToRevealSecretWithoutToken() {
        val now = 1_000L
        val store = buildStore(nowEpochSeconds = now)
        store.setSecret("seed".encodeToByteArray(), iv())

        assertNull(store.revealSecret(token = null))
    }

    @Test
    fun refusesToRevealSecretWhenTokenIsTooOld() {
        val now = 1_000L
        val store = buildStore(nowEpochSeconds = now, maxAge = 30)
        store.setSecret("seed".encodeToByteArray(), iv())

        val oldToken = GateToken(issuedAtEpochSeconds = now - 31)
        assertNull(store.revealSecret(oldToken))
    }

    @Test
    fun revealsSecretWhenTokenIsFreshEnough() {
        val now = 1_000L
        val store = buildStore(nowEpochSeconds = now, maxAge = 30)
        val expected = "seed".encodeToByteArray()
        store.setSecret(expected, iv())

        val freshToken = GateToken(issuedAtEpochSeconds = now - 30)
        val revealed = store.revealSecret(freshToken)
        assertArrayEquals(expected, revealed)
    }

    @Test
    fun refusesToRevealSecretWhenTokenIsFromFuture() {
        val now = 1_000L
        val store = buildStore(nowEpochSeconds = now, maxAge = 30)
        store.setSecret("seed".encodeToByteArray(), iv())

        val futureToken = GateToken(issuedAtEpochSeconds = now + 1)
        assertNull(store.revealSecret(futureToken))
    }

    private fun buildStore(nowEpochSeconds: Long, maxAge: Long = 30): BiometricBoundSecretStore {
        val keyProvider = InMemoryKeyProvider(random)
        val box = SecretBox(keyProvider)
        return BiometricBoundSecretStore(
            secretBox = box,
            clock = { nowEpochSeconds },
            maxTokenAgeSeconds = maxAge,
        )
    }

    private fun iv(): ByteArray = ByteArray(SecretBox.IV_BYTES).also(random::nextBytes)
}
