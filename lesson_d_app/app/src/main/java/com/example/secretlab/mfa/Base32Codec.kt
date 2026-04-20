package com.example.secretlab.mfa

import java.io.ByteArrayOutputStream

object Base32Codec {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun decode(input: String): ByteArray {
        val normalized = input
            .uppercase()
            .filter { it in ALPHABET || it == '=' }
            .trimEnd('=')

        val output = ByteArrayOutputStream()
        var buffer = 0
        var bitsLeft = 0

        for (char in normalized) {
            val value = ALPHABET.indexOf(char)
            require(value >= 0) { "Invalid Base32 char: $char" }
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            while (bitsLeft >= 8) {
                bitsLeft -= 8
                output.write((buffer shr bitsLeft) and 0xFF)
            }
        }

        return output.toByteArray()
    }
}
