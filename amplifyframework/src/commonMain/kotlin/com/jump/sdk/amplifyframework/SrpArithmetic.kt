package com.jump.sdk.amplifyframework

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign

/** Modular exponentiation for SRP with a positive modulus and nonnegative exponent. */
internal fun srpModPow(base: BigInteger, exponent: BigInteger, modulus: BigInteger): BigInteger {
    require(modulus > BigInteger.ZERO) { "Modulus must be positive" }
    require(exponent >= BigInteger.ZERO) { "Exponent must be nonnegative" }
    if (modulus == BigInteger.ONE) return BigInteger.ZERO
    val result = platformModPow(base.mod(modulus).toByteArray(), exponent.toByteArray(), modulus.toByteArray())
    return if (result.isEmpty()) BigInteger.ZERO else BigInteger.fromByteArray(result, Sign.POSITIVE)
}

/** Unsigned, big-endian (base ^ exponent) % modulus for a modulus above one. An empty array is zero. */
internal expect fun platformModPow(base: ByteArray, exponent: ByteArray, modulus: ByteArray): ByteArray
