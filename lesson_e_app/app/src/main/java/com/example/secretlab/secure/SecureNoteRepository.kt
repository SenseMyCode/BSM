package com.example.secretlab.secure

import android.content.Context

class SecureNoteRepository(
    context: Context,
) {
    private val prefs = SecurePrefs.open(context)

    fun readEncryptedNote(): ByteArray? {
        val b64 = prefs.getString(KEY_NOTE, null) ?: return null
        return Base64Codec.decode(b64)
    }

    fun writeEncryptedNote(message: ByteArray) {
        prefs.edit().putString(KEY_NOTE, Base64Codec.encode(message)).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_NOTE).apply()
    }

    companion object {
        private const val KEY_NOTE = "secure_note_message_b64"
    }
}

