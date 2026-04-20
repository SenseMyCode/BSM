package com.example.secretlab.debug

import android.util.Log

class SecurityLogger {
    fun reportGateOpen(openSecret: String, travelCard: String) {
        // TODO(C05-4): stop printing full secrets after login.
        Log.d("NorthGate", "gate=open secret=$openSecret card=$travelCard")
    }
}
