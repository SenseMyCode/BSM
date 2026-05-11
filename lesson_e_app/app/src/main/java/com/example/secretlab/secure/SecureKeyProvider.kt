package com.example.secretlab.secure

import android.content.Context
import java.security.SecureRandom

class SecureKeyProvider(
    context: Context,
    private val random: SecureRandom = SecureRandom(),
) : KeyProvider {
    private val prefs = SecurePrefs.open(context)

    override fun getOrCreateAesKey(): ByteArray {
        val existing = prefs.getString(KEY, null)
        if (existing != null) {
            return Base64Codec.decode(existing)
        }
        val created = ByteArray(32)
        random.nextBytes(created)
        prefs.edit().putString(KEY, Base64Codec.encode(created)).apply()
        return created
    }

    companion object {
        private const val KEY = "local_aes_key_b64"
    }
}

