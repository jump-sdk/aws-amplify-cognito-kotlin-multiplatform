package com.jump.sdk.amplifyframework

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class SrpProductionArithmeticTests {
    // Expected residues were generated independently with Python pow(base, exponent, N).
    // Production reduces a random 1024-byte private value modulo this 3072-bit prime.

    @Test
    fun generatorWithProductionExponent() {
        val modulus = BigInteger.parseString(HEX_N, 16)
        val base = BigInteger.TWO
        val exponent = modulus - BigInteger.ONE
        val expected = BigInteger.ONE
        assertEquals(expected, srpModPow(base, exponent, modulus))
    }

    @Test
    fun nontrivialBaseWithProductionExponent() {
        val modulus = BigInteger.parseString(HEX_N, 16)
        val base = BigInteger.parseString("a5".repeat(384), 16)
        val exponent = BigInteger.parseString("d3".repeat(384), 16)
        val expected =
            BigInteger.parseString(
                "559015dcb14979349e1a668cabbbbc1331aafc8db5a40825dfb4c720869901c818d7b1ca79683001bac27a1ddd11d35f" +
                    "50f9dde196c06c44a5198491af618af8cfb423f2a919f65ab3b1feac005aea86fe226aa76a05311025ea96884ee016a3" +
                    "8eba481117c161346b6f5fe87c36746fafcd287fbbc3abcc321fab78a07a437b0cbfad78619aa6d129662b061985354e" +
                    "040afcfa5a173af65dfa5f875f5ba1ecebc9ab2a79ea4bc29d16a1fd07151d2ecf526bfaf983e3553552ec978ad62950" +
                    "6bb8bd053e3308fc6684e037dfd59fd5e5dab3b11465edebc55dd2d5d367add57c316158978bf9496656adeb45d22420" +
                    "72ddfa1030331a72846ed9977ac9edbdd5ce9f24069846aac9f42dd9e025a695faec69df6658916beec2ad52a8dc8eba" +
                    "f8c398b89aaf595717baa7a8d18ada2b21fd3aa90489b1631d85824a03965108c4108e65313d91a164ffebc87678e04c" +
                    "28ea87d614183f82679fc5160584931cb46c683e185750e2a347377cc1c29fceee846b59b71951d94e11bbed72216287",
                16,
            )
        assertEquals(expected, srpModPow(base, exponent, modulus))
    }

    @Test
    fun baseAboveModulusWithProductionExponent() {
        val modulus = BigInteger.parseString(HEX_N, 16)
        val base = modulus + BigInteger.parseString("9b".repeat(128), 16)
        val exponent = BigInteger.parseString("81".repeat(384), 16)
        val expected =
            BigInteger.parseString(
                "2ab013663e2b419c8efba231a2a098b01002d72235f869bed232363a5ad107e311556a3852b19ee555b4c607a15acff2" +
                    "bafd72fec62d0285ccef237c7c8a05027cda74001ce54265b717e0a954eb6b48c68d1b6f7adb6fd71a68dca19fded22d" +
                    "e80fc54126ad7cf4b9eced074344060a57b15e38105e118345e6ebe028c26523f4e951d56bca18200c2aadb07cae1645" +
                    "20716f3b6517d51c6f95b380051bc4af4b5e4d750de78cff616e77997dff130561a4ed47b38cb09c9ad858883b4a1c39" +
                    "2ace9dea6ca178f7eeac4ae8f27cc27bac06b0daa1b042acb72f8d1d07851c4427445a9e47c0744ef2ba68223c1bfa9e" +
                    "5d9667d8835c1ed6a7889cfa601a3501d8f4779b807013e67f4014e878b6b0abb05ebc327fdfe99497123a7cd7861b6a" +
                    "7cb3a82e7b4f79048964dc6901fa6821e4ea5e41ad96704969aee996898ec6d25c70cdb9ff77420d1234897ca478499d" +
                    "9961bfcd8743805e13848ca47fd69e579b518a566015e464cfd83170d860c232e2b707d86e80e43926e831abc9aaa540",
                16,
            )
        assertEquals(expected, srpModPow(base, exponent, modulus))
    }
}
