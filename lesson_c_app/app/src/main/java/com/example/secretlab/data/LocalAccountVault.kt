package com.example.secretlab.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class LocalAccountVault(context: Context) {
    private val rawBox = context.getSharedPreferences(BIN_NAME, Context.MODE_PRIVATE)

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val secureBox = EncryptedSharedPreferences.create(
        context,
        "encrypted_prefs", // Nazwa nowego, bezpiecznego pliku
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveLocalAccount(mail: String, secret: String) {
        // here write solution to C05.5
        secureBox.edit()
            .putString(MAIL_SLOT, mail)
            .putString(SECRET_SLOT, secret)
            .apply()
    }

    fun readAccountMail(): String? {
        // here write solution to C05.5
        return secureBox.getString(MAIL_SLOT, null)
    }

    fun readAccountSecret(): String? {
        // here write solution to C05.5
        return secureBox.getString(SECRET_SLOT, null)
    }

    fun saveTravelCard(token: String) {
        secureBox.edit()
            .putString("travel_card", token)
            .apply()
    }

    companion object {
        const val BIN_NAME = "account_memory"
        const val MAIL_SLOT = "owner_mail"
        const val SECRET_SLOT = "owner_secret"
    }
}
