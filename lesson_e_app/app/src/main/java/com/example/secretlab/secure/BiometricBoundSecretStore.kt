package com.example.secretlab.secure

/**
 * A small policy wrapper: you may only reveal the protected secret if a fresh `GateToken`
 * is provided.
 *
 * In a real app the gate would come from BiometricPrompt / device credential. Here we model
 * it as a token so the policy is testable and deterministic.
 */
class BiometricBoundSecretStore(
    private val secretBox: SecretBox,
    private val clock: () -> Long,
    private val maxTokenAgeSeconds: Long = 30,
) {
    private var encryptedSecret: ByteArray? = null

    fun setSecret(plaintextSecret: ByteArray, iv: ByteArray) {
        // TODO(L05-4): bind the secret to a fixed context using AAD, so ciphertext can't be reused
        // in a different place/purpose.
        encryptedSecret = secretBox.encryptBound(plaintextSecret, iv, SECRET_CONTEXT)
    }

    fun revealSecret(token: GateToken?): ByteArray? {
        val message = encryptedSecret ?: return null
        // TODO(L05-3): enforce the gate policy:
        // - token must be non-null
        // - token age must satisfy: 0 <= age <= maxTokenAgeSeconds (based on `clock()` and token epoch seconds)
        // - return null when gate conditions are not met
        if (token == null) return null

        val now = clock()
        val age = now - token.issuedAtEpochSeconds

        if (age < 0 || age > maxTokenAgeSeconds) {
            return null
        }

        return secretBox.decryptBound(message, SECRET_CONTEXT)
    }

    companion object {
        private val SECRET_CONTEXT: ByteArray = "bsm:l05e:seed:v1".encodeToByteArray()
    }
}