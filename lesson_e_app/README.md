# Lesson E App (Lab 5)

Starter Android app for lesson E (Lab 5): biometrics as a *local gate* to a secret used by an MFA flow.

What is intentionally unfinished or weak:
- `SecretBox` has TODOs for authenticated encryption (AES-GCM)
- `BiometricBoundSecretStore` has TODOs for enforcing the "local gate" policy

Main files used by the notebook:
- `app/src/main/java/com/example/secretlab/MainActivity.kt`
- `app/src/main/java/com/example/secretlab/secure/SecretBox.kt`
- `app/src/main/java/com/example/secretlab/secure/BiometricBoundSecretStore.kt`
- `app/src/main/java/com/example/secretlab/secure/InMemoryKeyProvider.kt`

Student-facing test suite:
- `app/src/test/java/com/example/secretlab/secure/SecretBoxStudentTest.kt`
- `app/src/test/java/com/example/secretlab/secure/BiometricBoundSecretStoreStudentTest.kt`

Notes:
- The project is scaffolded for Android Studio.
- The Gradle wrapper JAR is not included in this workspace snapshot.
- This lab is graded via JVM unit tests only (no emulator required).
