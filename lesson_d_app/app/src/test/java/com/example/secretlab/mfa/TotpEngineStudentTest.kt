package com.example.secretlab.mfa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TotpEngineStudentTest {
    private val engine = TotpEngine(window = 1)

    @Test
    fun matchesReferenceVectorAtKnownTimestamp() {
        val epochSeconds = 60L
        val expected = TotpEngine.generateReference(LabMfaFixture.TotpSecretBase32, epochSeconds)
        val actual = engine.generate(LabMfaFixture.TotpSecretBase32, epochSeconds)
        assertEquals(expected, actual)
    }

    @Test
    fun acceptsCodeFromAdjacentTimeStepWhenWindowIsOne() {
        val previousSlotCode = TotpEngine.generateReference(LabMfaFixture.TotpSecretBase32, 30L)
        assertTrue(engine.verify(LabMfaFixture.TotpSecretBase32, previousSlotCode, 60L))
    }
}
