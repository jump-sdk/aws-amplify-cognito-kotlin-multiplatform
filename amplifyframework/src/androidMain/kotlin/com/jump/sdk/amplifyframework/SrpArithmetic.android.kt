package com.jump.sdk.amplifyframework

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import java.math.BigInteger as PlatformBigInteger

internal actual fun srpModPow(base: BigInteger, exponent: BigInteger, modulus: BigInteger): BigInteger {
    require(modulus > BigInteger.ZERO) { "Modulus must be positive" }
    require(exponent >= BigInteger.ZERO) { "Exponent must be nonnegative" }
    val baseBytes = base.mod(modulus).toByteArray()
    val exponentBytes = exponent.toByteArray()
    return try {
        val result =
            PlatformBigInteger(1, baseBytes).modPow(
                PlatformBigInteger(1, exponentBytes),
                PlatformBigInteger(1, modulus.toByteArray()),
            ).toByteArray()
        try {
            BigInteger.fromByteArray(result, Sign.POSITIVE)
        } finally {
            result.fill(0)
        }
    } finally {
        baseBytes.fill(0)
        exponentBytes.fill(0)
    }
}
