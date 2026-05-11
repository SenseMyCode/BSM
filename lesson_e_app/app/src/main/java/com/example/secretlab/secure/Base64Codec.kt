package com.example.secretlab.secure

import android.util.Base64

object Base64Codec {
    fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    fun decode(text: String): ByteArray =
        Base64.decode(text, Base64.NO_WRAP)
}

