package com.example.secretlab.data

import java.util.UUID

data class SignInResult(
    val displayName: String,
    val travelCard: String,
)

class AuthGateway {
    fun signIn(mail: String, secret: String): SignInResult? {
        if (mail == "student@lab.invalid" && secret == "student123!") {
            return SignInResult(
                displayName = "Student Demo",
                travelCard = "sid-${UUID.randomUUID().toString().replace("-", "").take(18)}",
            )
        }
        return null
    }
}
