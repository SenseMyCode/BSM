package com.example.secretlab.mfa

import com.example.secretlab.data.AuthGateway
import java.util.UUID

data class PasswordStepState(
    val displayName: String,
    val challengeId: String,
)

data class MfaDecision(
    val granted: Boolean,
    val reason: String,
    val travelCard: String? = null,
)

class MfaLoginGateway(
    private val authGateway: AuthGateway = AuthGateway(),
    private val totpEngine: TotpEngine = TotpEngine(),
    private val limiter: AttemptLimiter = AttemptLimiter(),
    private val clock: EpochClock = SystemEpochClock,
    private val secretBase32: String = LabMfaFixture.TotpSecretBase32,
) {
    private val activeChallenges = linkedMapOf<String, String>()
    private val usedTimeSteps = mutableSetOf<Long>()

    fun beginPasswordSignIn(mail: String, secret: String): PasswordStepState? {
        val passwordResult = authGateway.signIn(mail, secret) ?: return null
        val challengeId = "chal-${UUID.randomUUID().toString().replace("-", "").take(12)}"
        activeChallenges[challengeId] = passwordResult.normalizedMail
        return PasswordStepState(
            displayName = passwordResult.displayName,
            challengeId = challengeId,
        )
    }

    fun verifyTotp(challengeId: String, code: String, epochSeconds: Long = clock.nowEpochSeconds()): MfaDecision {
        if (limiter.isBlocked(epochSeconds)) {
            return MfaDecision(granted = false, reason = "cooldown")
        }

        val mail = activeChallenges.values.firstOrNull()
            ?: return MfaDecision(granted = false, reason = "missing_challenge")

        // TODO(D06-1): bind verification to the exact `challengeId` instead of accepting any live challenge.
        if (!totpEngine.verify(secretBase32, code, epochSeconds)) {
            limiter.recordFailure(epochSeconds)
            return MfaDecision(granted = false, reason = "bad_code")
        }

        val currentStep = totpEngine.timeStep(epochSeconds)
        // TODO(D06-2): reject codes that reuse a TOTP value already accepted in the same time step.
        usedTimeSteps.add(currentStep)

        limiter.recordSuccess()
        activeChallenges.remove(challengeId)
        return MfaDecision(
            granted = true,
            reason = "granted",
            travelCard = authGateway.issueTravelCard(mail),
        )
    }

    fun fallbackRecover(mail: String, emailCode: String): MfaDecision {
        val granted = mail == LabMfaFixture.DemoEmail && emailCode == LabMfaFixture.FallbackEmailCode
        return if (granted) {
            MfaDecision(
                granted = true,
                reason = "fallback_granted",
                travelCard = authGateway.issueTravelCard(mail),
            )
        } else {
            MfaDecision(granted = false, reason = "fallback_denied")
        }
    }

    fun activeChallengeCount(): Int = activeChallenges.size
}
