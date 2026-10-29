package com.jump.sdk.amplifyframework

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.modular.ModularBigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SrpArithmeticTests {
    @Test
    fun rejectsInvalidModulusAndExponent() {
        for (modulus in listOf(BigInteger.ZERO, -BigInteger.ONE)) {
            assertFailsWith<IllegalArgumentException> {
                srpModPow(BigInteger.TWO, BigInteger.ONE, modulus)
            }
        }
        assertFailsWith<IllegalArgumentException> {
            srpModPow(BigInteger.TWO, -BigInteger.ONE, BigInteger.fromInt(257))
        }
    }

    @Test
    fun unsignedOutputAndZeroCases() {
        fun check(base: Int, exponent: Int, modulus: Int, expected: Int) {
            assertEquals(
                BigInteger.fromInt(expected),
                srpModPow(BigInteger.fromInt(base), BigInteger.fromInt(exponent), BigInteger.fromInt(modulus)),
            )
        }
        check(2, 7, 257, 128)
        check(2, 8, 257, 256)
        check(0, 10, 257, 0)
        check(2, 0, 257, 1)
        check(0, 0, 257, 1)
        check(0, 0, 1, 0)
        check(2, 10, 1, 0)
        check(512, 2, 257, 4)
        check(-2, 3, 257, 249)
    }

    @Test
    fun matchesKotlinArithmeticForDifferentModuliAndExponents() {
        for (modulus in listOf(256, 257, 32749, 65537)) {
            val m = BigInteger.fromInt(modulus)
            val creator = ModularBigInteger.creatorForModulo(m)
            for (base in listOf(0, 1, 2, 128, 256, 65538)) {
                for (exponent in listOf(0, 1, 2, 17, 255)) {
                    val b = BigInteger.fromInt(base)
                    val e = BigInteger.fromInt(exponent)
                    assertEquals(creator.fromBigInteger(b).pow(e).toBigInteger(), srpModPow(b, e, m))
                }
            }
        }
    }
}
