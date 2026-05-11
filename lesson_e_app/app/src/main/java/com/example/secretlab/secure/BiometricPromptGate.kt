package com.example.secretlab.secure

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Real local user-auth gate using AndroidX BiometricPrompt.
 *
 * In this lab it produces a GateToken on success. It does NOT decrypt anything by itself.
 */
class BiometricPromptGate(
    private val clock: () -> Long,
) {
    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onResult: (GateToken?) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(GateToken(issuedAtEpochSeconds = clock()))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(null)
                }

                override fun onAuthenticationFailed() {
                    onResult(null)
                }
            },
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock secret")
            .setSubtitle("Verify biometrics to issue a GateToken")
            .setAllowedAuthenticators(AUTHENTICATORS)
            .setNegativeButtonText("Cancel")
            .build()

        prompt.authenticate(info)
    }

    companion object {
        val AUTHENTICATORS: Int = BiometricManager.Authenticators.BIOMETRIC_STRONG
    }
}
