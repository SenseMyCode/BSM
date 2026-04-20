package com.example.secretlab.mfa

import com.example.secretlab.data.AuthGateway
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MfaLoginGatewayStudentTest {
    @Test
    fun rejectsOtpForDifferentChallengeId() {
        val gateway = MfaLoginGateway(
            authGateway = AuthGateway(),
            totpEngine = TotpEngine(window = 1),
            limiter = AttemptLimiter(maxFailures = 3, cooldownSeconds = 30),
            secretBase32 = LabMfaFixture.TotpSecretBase32,
        )

        gateway.beginPasswordSignIn(LabMfaFixture.DemoEmail, LabMfaFixture.DemoPassword)
        val validChallenge = gateway.beginPasswordSignIn(LabMfaFixture.DemoEmail, LabMfaFixture.DemoPassword)
        val validCode = TotpEngine.generateReference(LabMfaFixture.TotpSecretBase32, 60L)

        val result = gateway.verifyTotp(
            challengeId = "chal-wrong0000",
            code = validCode,
            epochSeconds = 60L,
        )

        assertFalse(result.granted)
        assertTrue(validChallenge != null)
    }

    @Test
    fun rejectsReplayOfCodeFromAlreadyAcceptedTimeStep() {
        val gateway = MfaLoginGateway(
            authGateway = AuthGateway(),
            totpEngine = TotpEngine(window = 1),
            limiter = AttemptLimiter(maxFailures = 3, cooldownSeconds = 30),
            secretBase32 = LabMfaFixture.TotpSecretBase32,
        )

        val firstChallenge = gateway.beginPasswordSignIn(LabMfaFixture.DemoEmail, LabMfaFixture.DemoPassword)
        val validCode = TotpEngine.generateReference(LabMfaFixture.TotpSecretBase32, 60L)
        val firstResult = gateway.verifyTotp(firstChallenge!!.challengeId, validCode, 60L)
        assertTrue(firstResult.granted)

        val secondChallenge = gateway.beginPasswordSignIn(LabMfaFixture.DemoEmail, LabMfaFixture.DemoPassword)
        val replayResult = gateway.verifyTotp(secondChallenge!!.challengeId, validCode, 60L)

        assertFalse(replayResult.granted)
    }
}
