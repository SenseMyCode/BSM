package com.example.secretlab.mfa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttemptLimiterStudentTest {
    @Test
    fun blocksAfterConfiguredNumberOfFailures() {
        val limiter = AttemptLimiter(maxFailures = 3, cooldownSeconds = 30)

        limiter.recordFailure(100L)
        limiter.recordFailure(101L)
        limiter.recordFailure(102L)

        assertTrue(limiter.isBlocked(102L))
        assertEquals(132L, limiter.snapshot().blockedUntilEpochSeconds)
    }

    @Test
    fun cooldownExpiresAfterConfiguredTime() {
        val limiter = AttemptLimiter(maxFailures = 3, cooldownSeconds = 30)

        limiter.recordFailure(200L)
        limiter.recordFailure(201L)
        limiter.recordFailure(202L)

        assertFalse(limiter.isBlocked(233L))
    }
}
