package com.example.secretlab.data

import android.content.Context

class LocalAccountVault(context: Context) {
    private val rawBox = context.getSharedPreferences(BIN_NAME, Context.MODE_PRIVATE)

    fun saveLocalAccount(mail: String, secret: String) {
        // here write solution to C05.5
        rawBox.edit()
            .putString(MAIL_SLOT, mail)
            .putString(SECRET_SLOT, secret)
            .apply()
    }

    fun readAccountMail(): String? {
        // here write solution to C05.5
        return rawBox.getString(MAIL_SLOT, null)
    }

    fun readAccountSecret(): String? {
        // here write solution to C05.5
        return rawBox.getString(SECRET_SLOT, null)
    }

    companion object {
        const val BIN_NAME = "account_memory"
        const val MAIL_SLOT = "owner_mail"
        const val SECRET_SLOT = "owner_secret"
    }
}
