package com.jump.sdk.amplifyframework

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign

/** Modular exponentiation for SRP with a positive modulus and nonnegative exponent. */
internal fun srpModPow(base: BigInteger, exponent: BigInteger, modulus: BigInteger): BigInteger {
    require(modulus > BigInteger.ZERO) { "Modulus must be positive" }
    require(exponent >= BigInteger.ZERO) { "Exponent must be nonnegative" }
    val baseBytes = base.mod(modulus).toByteArray()
    val exponentBytes = exponent.toByteArray()
    val result = try {
        platformModPow(baseBytes, exponentBytes, modulus.toByteArray())
    } finally {
        baseBytes.fill(0)
        exponentBytes.fill(0)
    }
    return try {
        if (result.isEmpty()) BigInteger.ZERO else BigInteger.fromByteArray(result, Sign.POSITIVE)
    } finally {
        result.fill(0)
    }
}

/** Unsigned, big-endian (base ^ exponent) % modulus. An empty array is zero. */
internal expect fun platformModPow(base: ByteArray, exponent: ByteArray, modulus: ByteArray): ByteArray
