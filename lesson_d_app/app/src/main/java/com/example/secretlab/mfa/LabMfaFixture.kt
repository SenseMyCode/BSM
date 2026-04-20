package com.example.secretlab.mfa

object LabMfaFixture {
    const val DemoEmail = "student@lab.invalid"
    const val DemoPassword = "student123!"
    const val DisplayName = "Student Demo"
    const val Issuer = "SecretLab MFA"
    const val AccountLabel = "student@lab.invalid"
    const val TotpSecretBase32 = "JBSWY3DPEHPK3PXP"
    const val FallbackEmailCode = "154903"

    fun provisioningUri(): String {
        val encodedIssuer = Issuer.replace(" ", "%20")
        return "otpauth://totp/$encodedIssuer:$AccountLabel?secret=$TotpSecretBase32&issuer=$encodedIssuer&digits=6&period=30"
    }
}
