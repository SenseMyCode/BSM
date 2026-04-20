package com.example.secretlab.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.secretlab.mfa.LabMfaFixture

class SecureSessionStore(context: Context) {
    private val encryptedBox = buildEncryptedBox(context)

    fun saveTravelCard(value: String) {
        encryptedBox.edit().putString(SECURE_SLOT, value).apply()
    }

    fun readTravelCard(): String? = encryptedBox.getString(SECURE_SLOT, null)

    fun clearTravelCard() {
        encryptedBox.edit().remove(SECURE_SLOT).apply()
    }

    fun saveTotpSecret(value: String) {
        encryptedBox.edit().putString(TOTP_SECRET_SLOT, value).apply()
    }

    fun readTotpSecret(): String? = encryptedBox.getString(TOTP_SECRET_SLOT, null)

    fun saveProvisioningUri(value: String) {
        encryptedBox.edit().putString(TOTP_URI_SLOT, value).apply()
    }

    fun readProvisioningUri(): String? = encryptedBox.getString(TOTP_URI_SLOT, null)

    fun ensureProvisioned() {
        if (readTotpSecret() == null) {
            saveTotpSecret(LabMfaFixture.TotpSecretBase32)
        }
        if (readProvisioningUri() == null) {
            saveProvisioningUri(LabMfaFixture.provisioningUri())
        }
    }

    companion object {
        const val SECURE_CRATE = "cipher_archive"
        const val SECURE_SLOT = "amber_dock"
        const val TOTP_SECRET_SLOT = "totp_seed"
        const val TOTP_URI_SLOT = "totp_uri"

        fun buildMasterKey(context: Context): MasterKey =
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

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
