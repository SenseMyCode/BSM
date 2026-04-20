package com.example.secretlab.debug

import org.junit.Test

class LocalHashDemoProbeTest {
    @Test
    fun probeSha256() {
        println("sha256(student123!)=" + LocalHashDemo.sha256("student123!"))
    }

    @Test
    fun probeLocalHashSessionRecovery() {
        val digest = LocalHashDemo.sha256("student123!")
        println("localHashCanRestoreSession=" + LocalHashDemo.localHashCanRestoreSession(digest))
    }
}
