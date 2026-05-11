package com.example.secretlab.data

import com.example.secretlab.mfa.LabMfaFixture
import java.util.UUID

data class PasswordSignInResult(
    val displayName: String,
    val normalizedMail: String,
)

class AuthGateway {
    fun signIn(mail: String, secret: String): PasswordSignInResult? {
        if (mail == LabMfaFixture.DemoEmail && secret == LabMfaFixture.DemoPassword) {
            return PasswordSignInResult(
                displayName = LabMfaFixture.DisplayName,
                normalizedMail = mail.lowercase(),
            )
        }
        return null
    }

    fun issueTravelCard(mail: String): String {
        val suffix = UUID.randomUUID().toString().replace("-", "").take(18)
        return "sid-${mail.substringBefore('@')}-${suffix}"
    }
}
