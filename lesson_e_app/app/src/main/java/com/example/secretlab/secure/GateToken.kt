package com.example.secretlab.secure

/**
 * An authentication artifact proving that a local user gate (e.g., biometrics / device credential)
 * has just been satisfied.
 *
 * In this lab it is intentionally modeled as a simple value object so the policy can be verified
 * via JVM unit tests (without requiring an emulator).
 */
data class GateToken(
    val issuedAtEpochSeconds: Long,
)

