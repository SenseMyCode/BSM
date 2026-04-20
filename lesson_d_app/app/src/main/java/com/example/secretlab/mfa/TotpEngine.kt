package com.example.secretlab.mfa

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class TotpEngine(
    private val stepSeconds: Long = 30,
    private val digits: Int = 6,
    val window: Int = 1,
    private val clock: EpochClock = SystemEpochClock,
) {
    fun generate(secretBase32: String, epochSeconds: Long = clock.nowEpochSeconds()): String {
        // TODO(D03-1): decode the Base32 secret and generate the RFC 6238 code for the current step.
        // Starter returns a fixed placeholder so the student tests fail until the implementation is complete.
        return "000000"
    }

    fun verify(secretBase32: String, code: String, epochSeconds: Long = clock.nowEpochSeconds()): Boolean {
        val normalizedCode = code.trim()
        val currentStep = timeStep(epochSeconds)

        // TODO(D03-2): compare the provided code against the current time step and the
        // neighbouring steps according to `window`.
        return normalizedCode == generate(secretBase32, currentStep * stepSeconds)
    }

    fun timeStep(epochSeconds: Long = clock.nowEpochSeconds()): Long = epochSeconds / stepSeconds

    companion object {
        fun generateReference(
            secretBase32: String,
            epochSeconds: Long,
            stepSeconds: Long = 30,
            digits: Int = 6,
        ): String {
            val counter = epochSeconds / stepSeconds
            return hotpFromDecodedSecret(Base32Codec.decode(secretBase32), counter, digits)
        }

        fun verifyReference(
            secretBase32: String,
            code: String,
            epochSeconds: Long,
            stepSeconds: Long = 30,
            digits: Int = 6,
            window: Int = 1,
        ): Boolean {
            val currentCounter = epochSeconds / stepSeconds
            for (offset in -window..window) {
                val counter = currentCounter + offset
                if (counter < 0) {
                    continue
                }
                if (hotpFromDecodedSecret(Base32Codec.decode(secretBase32), counter, digits) == code.trim()) {
                    return true
                }
            }
            return false
        }

        private fun hotpFromDecodedSecret(secret: ByteArray, counter: Long, digits: Int): String {
            val buffer = ByteArray(8)
            for (index in 7 downTo 0) {
                buffer[index] = ((counter ushr ((7 - index) * 8)) and 0xFF).toByte()
            }

            val mac = Mac.getInstance("HmacSHA1")
            mac.init(SecretKeySpec(secret, "HmacSHA1"))
            val digest = mac.doFinal(buffer)
            val offset = digest.last().toInt() and 0x0F
            val binary =
                ((digest[offset].toInt() and 0x7F) shl 24) or
                    ((digest[offset + 1].toInt() and 0xFF) shl 16) or
                    ((digest[offset + 2].toInt() and 0xFF) shl 8) or
                    (digest[offset + 3].toInt() and 0xFF)

            val otp = binary % pow10(digits)
            return otp.toString().padStart(digits, '0')
        }

        private fun pow10(digits: Int): Int {
            var result = 1
            repeat(digits) {
                result *= 10
            }
            return result
        }
    }
}
