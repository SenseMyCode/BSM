package com.example.secretlab.mfa

data class AttemptSnapshot(
    val failures: Int,
    val blockedUntilEpochSeconds: Long,
)

class AttemptLimiter(
    private val maxFailures: Int = 3,
    private val cooldownSeconds: Long = 30,
    private val clock: EpochClock = SystemEpochClock,
) {
    private var failures = 0
    private var blockedUntilEpochSeconds = 0L

    fun isBlocked(nowEpochSeconds: Long = clock.nowEpochSeconds()): Boolean {
        // TODO(D05-1): return true while the cooldown window is active.
        return false
    }

    fun recordFailure(nowEpochSeconds: Long = clock.nowEpochSeconds()) {
        // TODO(D05-2): increment failures and arm the cooldown after reaching maxFailures.
    }

    fun recordSuccess() {
        failures = 0
        blockedUntilEpochSeconds = 0L
    }

    fun snapshot(): AttemptSnapshot = AttemptSnapshot(
        failures = failures,
        blockedUntilEpochSeconds = blockedUntilEpochSeconds,
    )

    companion object {
        fun expectedBlockedUntil(
            startEpochSeconds: Long,
            maxFailures: Int = 3,
            cooldownSeconds: Long = 30,
        ): Long = startEpochSeconds + cooldownSeconds
    }
}
