package com.example.secretlab.debug

import android.util.Log

class SecurityLogger {
    fun reportProvisioning(provisioningUri: String) {
        // TODO(D02-LOG): stop logging the full otpauth URI because it exposes the long-term seed.
        Log.d("NorthGate", "mfa_provisioning_uri=$provisioningUri")
    }

    fun reportMfaSuccess(challengeId: String, travelCard: String) {
        Log.i("NorthGate", "mfa_ok challenge=${challengeId.take(12)} card=${travelCard.take(10)}")
    }

    fun reportFallback(mail: String, emailCode: String) {
        // TODO(D07-LOG): do not print recovery codes in logs.
        Log.w("NorthGate", "fallback mail=$mail code=$emailCode")
    }
}
