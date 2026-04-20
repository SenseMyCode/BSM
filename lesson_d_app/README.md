# Lesson D App

Starter Android app for lesson D: mobile MFA with TOTP, rate limiting, replay and fallback.

What is intentionally unfinished or weak:
- `TotpEngine` returns a placeholder code until the student implements RFC 6238 logic
- `AttemptLimiter` does not enforce cooldown yet
- `MfaLoginGateway` does not bind verification to the exact challenge and does not reject replayed OTP steps
- `SecurityLogger` logs the full provisioning URI and fallback code
- fallback recovery bypasses the main TOTP path by design and should be assessed as weaker

Main files used by the notebook:
- `app/src/main/java/com/example/secretlab/mfa/TotpEngine.kt`
- `app/src/main/java/com/example/secretlab/mfa/AttemptLimiter.kt`
- `app/src/main/java/com/example/secretlab/mfa/MfaLoginGateway.kt`
- `app/src/main/java/com/example/secretlab/data/SecureSessionStore.kt`
- `app/src/main/java/com/example/secretlab/debug/SecurityLogger.kt`
- `app/src/main/java/com/example/secretlab/MainActivity.kt`

Student-facing test suite:
- `app/src/test/java/com/example/secretlab/mfa/TotpEngineStudentTest.kt`
- `app/src/test/java/com/example/secretlab/mfa/AttemptLimiterStudentTest.kt`
- `app/src/test/java/com/example/secretlab/mfa/MfaLoginGatewayStudentTest.kt`

Notes:
- The project is scaffolded for Android Studio.
- The Gradle wrapper JAR is not included in this workspace snapshot.
- The MFA screen now renders a QR code for the provisioning URI to reduce setup friction during class.
- To generate a TOTP code during manual testing, configure any authenticator app with the
  `otpauth://...` URI returned by `LabMfaFixture.provisioningUri()` and stored by `SecureSessionStore.ensureProvisioned()`.
