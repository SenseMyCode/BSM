# Lesson C App

Starter Android app for lesson C.

What is intentionally insecure:
- a locally persisted user secret
- a locally persisted session artifact
- a debug flow that restores session state from local storage
- a log entry that prints full secrets

Files used by the notebook:
- `app/src/main/java/com/example/secretlab/data/InsecureSessionStore.kt`
- `app/src/main/java/com/example/secretlab/data/SecureSessionStore.kt`
- `app/src/main/java/com/example/secretlab/debug/SecurityLogger.kt`
- `app/src/main/java/com/example/secretlab/debug/LocalHashDemo.kt`

Notes:
- The project is scaffolded for Android Studio.
- The Gradle wrapper JAR is not included in this workspace snapshot.
