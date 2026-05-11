package com.example.secretlab.mfa

fun interface EpochClock {
    fun nowEpochSeconds(): Long
}

object SystemEpochClock : EpochClock {
    override fun nowEpochSeconds(): Long = System.currentTimeMillis() / 1000L
}
