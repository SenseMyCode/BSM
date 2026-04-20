package com.example.secretlab.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureSessionStore(context: Context) {
    private val encryptedBox by lazy {
        // TODO(C05-3): build a MasterKey and replace this starter configuration with EncryptedSharedPreferences.
        // Use:
        // MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        // and EncryptedSharedPreferences.create(...)
        context.getSharedPreferences(SECURE_CRATE, Context.MODE_PRIVATE)
    }

    fun saveTravelCard(value: String) {
        encryptedBox.edit().putString(SECURE_SLOT, value).apply()
    }

    fun readTravelCard(): String? = encryptedBox.getString(SECURE_SLOT, null)

    fun clearTravelCard() {
        encryptedBox.edit().remove(SECURE_SLOT).apply()
    }

    companion object {
        const val SECURE_CRATE = "cipher_archive"
        const val SECURE_SLOT = "amber_dock"

        @Suppress("unused")
        fun buildMasterKey(context: Context): MasterKey =
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        @Suppress("unused")
        fun buildEncryptedBox(context: Context) =
            EncryptedSharedPreferences.create(
                context,
                SECURE_CRATE,
                buildMasterKey(context),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
    }
}
