package com.example.secretlab.debug

import org.junit.Test

class LocalPasswordHasherProbeTest {
    @Test
    fun probeDerivedHashes() {
        val first = LocalPasswordHasher.derive("vault-c05-01!", "mint-01")
        val second = LocalPasswordHasher.derive("vault-c05-01!", "mint-02")
        println("mint-01=" + first)
        println("mint-02=" + second)
        println("sameResult=" + (first == second))
    }

    @Test
    fun probeRecordFormat() {
        val record = LocalPasswordHasher.buildRecord("vault-c05-01!", "mint-01")
        println("record=" + record)
    }

    @Test
    fun probeVerify() {
        val record = LocalPasswordHasher.buildRecord("vault-c05-01!", "mint-01")
        println("verifyGood=" + LocalPasswordHasher.verify("vault-c05-01!", record))
        println("verifyBad=" + LocalPasswordHasher.verify("wrong-pass", record))
    }
}
